package com.nashss.se.noteworthy.activity;

import com.nashss.se.noteworthy.activity.requests.TranscribeAudioRequest;
import com.nashss.se.noteworthy.activity.results.TranscribeAudioResult;
import com.nashss.se.noteworthy.converters.ModelConverter;
import com.nashss.se.noteworthy.exceptions.TranscriptionException;
import com.nashss.se.noteworthy.models.NoteModel;
import com.nashss.se.noteworthy.services.dynamodb.NoteDao;
import com.nashss.se.noteworthy.services.dynamodb.TranscriptionDao;
import com.nashss.se.noteworthy.services.dynamodb.models.Note;
import com.nashss.se.noteworthy.services.dynamodb.models.Transcription;
import com.nashss.se.noteworthy.services.s3.S3Wrapper;
import com.nashss.se.noteworthy.services.transcribe.TranscriptionUtils;

import com.amazonaws.services.transcribe.AmazonTranscribe;
import com.amazonaws.services.transcribe.model.GetTranscriptionJobRequest;
import com.amazonaws.services.transcribe.model.GetTranscriptionJobResult;
import com.amazonaws.services.transcribe.model.LanguageCode;
import com.amazonaws.services.transcribe.model.Media;
import com.amazonaws.services.transcribe.model.MediaFormat;
import com.amazonaws.services.transcribe.model.StartTranscriptionJobRequest;
import com.amazonaws.services.transcribe.model.TranscriptionJobStatus;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import javax.inject.Inject;

import static java.lang.Thread.sleep;

/**
 * Implementation of the TranscribeAudioActivity for the NoteworthyService's TranscribeAudio API.
 * This API allows the user to update an existing note.
 */
public class TranscribeAudioActivity {
    private final Logger log = LogManager.getLogger();
    private final NoteDao noteDao;
    private final TranscriptionDao transcriptionDao;
    private final S3Wrapper s3Wrapper;
    private final AmazonTranscribe transcribeClient;
    private String transcriptionId;

    /**
     * Instantiates a new TranscribeAudioActivity object.
     * @param noteDao NoteDao used to access notes table
     * @param transcriptionDao TranscriptionDao used to access transcriptions table
     * @param s3Wrapper AmazonS3 client
     * @param transcribeClient AmazonTranscribe client
     */
    @Inject
    public TranscribeAudioActivity(NoteDao noteDao, TranscriptionDao transcriptionDao,
                                   S3Wrapper s3Wrapper, AmazonTranscribe transcribeClient) {
        this.noteDao = noteDao;
        this.transcriptionDao = transcriptionDao;
        this.s3Wrapper = s3Wrapper;
        this.transcribeClient = transcribeClient;
    }

    /**
     * This method handles the incoming request by putting the audio into an
     * S3 bucket and running a transcription job on it. It then accesses the output file
     * and stores in the database before return the result.
     * @param transcribeAudioRequest request object containing the note keys and audio
     * @return result object containing the API defined by {@link NoteModel}
     */
    public TranscribeAudioResult handleRequest(TranscribeAudioRequest transcribeAudioRequest) {
        if (transcribeAudioRequest.getAudio() != null) {
            log.info("Received TranscribeAudioRequest for email '{}' with media file of size {} KB.",
                    transcribeAudioRequest.getEmail(), transcribeAudioRequest.getAudio().length / 1_000);
        } else {
            throw new TranscriptionException("Media cannot be null. Please include WAV file with request.");
        }

        // Create unique transcription identifier for input bucket key, transcription job, and output bucket key
        LocalDateTime currentTime = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        transcriptionId = TranscriptionUtils.generateTranscriptionId(currentTime.toString());

        // Put audio file into S3 input bucket
        s3Wrapper.putAudioInS3(transcriptionId, transcribeAudioRequest.getAudio());

        // Create transcription job request with parameters and start transcription job
        log.info("Creating StartTranscriptionJobRequest...");
        StartTranscriptionJobRequest startTranscriptionJobRequest = new StartTranscriptionJobRequest();
        startTranscriptionJobRequest.setTranscriptionJobName(transcriptionId);

        // Get sample rate
        log.info("Attempting to identify sample rate...");
        int sampleRate = TranscriptionUtils.getSampleRate(transcribeAudioRequest.getAudio());
        log.info("Using sample rate {} Hz.", sampleRate);

        // Point media object to S3 input object location
        Media media = new Media();
        media.setMediaFileUri(s3Wrapper.getAudioLocation(transcriptionId));

        startTranscriptionJobRequest.withMedia(media)
                .withMediaFormat(MediaFormat.Wav)
                .withMediaSampleRateHertz(sampleRate)
                .withLanguageCode(LanguageCode.EnUS)
                .withOutputBucketName(S3Wrapper.OUTPUT_BUCKET);

        transcribeClient.startTranscriptionJob(startTranscriptionJobRequest);
        log.info("Job sent as StartTranscriptionJobRequest to transcribe client. Job request details: " +
                        "Job name: '{}', Media format: '{}', Sample rate: '{} Hz', Language code: '{}'.",
                transcriptionId, MediaFormat.Wav, sampleRate, LanguageCode.EnUS);

        // Create a getTranscriptionJobRequest, and poll the transcription job until it has completed or failed
        GetTranscriptionJobRequest getTranscriptionJobRequest = new GetTranscriptionJobRequest()
                .withTranscriptionJobName(transcriptionId);
        int pollCount = 0;
        int pollFrequency = 1_000;
        log.info("Polling job for job status every {} ms...", pollFrequency);
        while (true) {
            // Get current job status
            GetTranscriptionJobResult jobResult = transcribeClient.getTranscriptionJob(getTranscriptionJobRequest);
            String jobStatus = jobResult.getTranscriptionJob().getTranscriptionJobStatus();
            pollCount++;

            // If job is in progress or queued, continue to loop until completed or failed
            if (jobStatus.equals(TranscriptionJobStatus.COMPLETED.toString())) {
                log.info("Job '{}' completed. Took {} seconds.", transcriptionId, pollCount);
                break;
            } else if (jobStatus.equals(TranscriptionJobStatus.FAILED.toString())) {
                throw new TranscriptionException(String.format(
                        "Transcription failed due to the following reason: '%s'",
                        jobResult.getTranscriptionJob().getFailureReason()));
            }

            // Wait one second before polling job again
            try {
                sleep(pollFrequency);
            } catch (InterruptedException e) {
                throw new TranscriptionException("Thread was interrupted while waiting for next poll.", e);
            }
        }

        // Stream transcription json from output S3 bucket
        String transcriptionResultJson = s3Wrapper.getTranscriptionJobResult(transcriptionId);

        // Save transcription data to transcriptions table
        Transcription transcription = new Transcription();
        transcription.setTranscriptionId(transcriptionId);
        transcription.setJson(transcriptionResultJson);
        transcriptionDao.saveTranscription(transcription);

        // Create new note using transcript as note content
        Note note = new Note();

        // Parse transcription json
        String transcript = TranscriptionUtils.parseJsonForTranscript(transcriptionResultJson);
        log.info("Transcription json successfully obtained and parsed. Transcript: '{}'", transcript);

        // Different from CreateNote where value is passed from FE. Due to future extensions of each feature.
        note.setTitle("Untitled Voice Note");
        note.setContent(transcript);
        note.setDateCreated(currentTime);
        note.setDateUpdated(currentTime);
        note.setEmail(transcribeAudioRequest.getEmail());
        note.setTranscriptionId(transcriptionId);

        // Save new voice note to notes table
        noteDao.saveNote(note);

        // Convert to note model, build result object, and return
        NoteModel noteModel = ModelConverter.toNoteModel(note);
        return TranscribeAudioResult.builder()
                .withNote(noteModel)
                .build();
    }
}
