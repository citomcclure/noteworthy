package com.nashss.se.noteworthy.activity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.nashss.se.noteworthy.activity.requests.TranscribeAudioRequest;
import com.nashss.se.noteworthy.activity.results.TranscribeAudioResult;
import com.nashss.se.noteworthy.converters.ModelConverter;
import com.nashss.se.noteworthy.services.dynamodb.NoteDao;
import com.nashss.se.noteworthy.services.dynamodb.TranscriptionDao;
import com.nashss.se.noteworthy.services.dynamodb.models.Note;
import com.nashss.se.noteworthy.services.dynamodb.models.Transcription;
import com.nashss.se.noteworthy.exceptions.TranscriptionException;
import com.nashss.se.noteworthy.models.NoteModel;
import com.nashss.se.noteworthy.utils.S3Utils;
import com.nashss.se.noteworthy.utils.TranscriptionUtils;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.services.transcribe.AmazonTranscribe;
import com.amazonaws.services.transcribe.model.*;

import javax.inject.Inject;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static java.lang.Thread.sleep;

/**
 * Implementation of the TranscribeAudioActivity for the NoteworthyService's TranscribeAudio API.
 * This API allows the user to update an existing note.
 */
public class TranscribeAudioActivity {
    private final Logger log = LogManager.getLogger();
    private final NoteDao noteDao;
    private final TranscriptionDao transcriptionDao;
    private final AmazonS3 s3Client;
    private final AmazonTranscribe transcribeClient;

    /**
     * Instantiates a new TranscribeAudioActivity object.
     * @param transcribeClient AmazonTranscribe client.
     */
    @Inject
    public TranscribeAudioActivity(NoteDao noteDao, TranscriptionDao transcriptionDao,
                                   AmazonS3 s3Client,
                                   AmazonTranscribe transcribeClient) {
        this.noteDao = noteDao;
        this.transcriptionDao = transcriptionDao;
        this.s3Client = s3Client;
        this.transcribeClient = transcribeClient;
    }

    /**
     * This method handles the incoming request by putting the audio into an
     * S3 bucket and running a transcription job on it. It then accesses the output file
     * and stores in the database before return the result.
     * @param transcribeAudioRequest request object containing the note keys and audio..
     * @return result object containing the API defined by {@link NoteModel}
     */
    // TODO: comment code and add/remove logs
    public TranscribeAudioResult handleRequest(TranscribeAudioRequest transcribeAudioRequest) {
        log.info("Received TranscribeAudioRequest for user '{}' with media file of size {}KB.",
                transcribeAudioRequest.getEmail(), transcribeAudioRequest.getAudio().length / 1_000);

        // Create unique transcription identifier for bucket keys, transcription job, and transcription output
        LocalDateTime currentTime = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        String transcriptionId = TranscriptionUtils.generateTranscriptionId(currentTime.toString());

        // Recreate audio file, store in S3, and create media object pointing to it
        InputStream inputStream = new ByteArrayInputStream(transcribeAudioRequest.getAudio());
        String transcriptionKey = transcriptionId + "_key";
        try {
            log.info("Attempting to save wav file to temp and put into S3 bucket as '{}'.", transcriptionKey);
            File file = File.createTempFile(transcriptionId, ".wav");
            Files.copy(inputStream, file.toPath(), StandardCopyOption.REPLACE_EXISTING);

            // Put new temp file into S3 bucket
            PutObjectRequest putObjectRequest = new PutObjectRequest(S3Utils.INPUT_BUCKET, transcriptionKey, file);
            s3Client.putObject(putObjectRequest);

            // Close stream and cleanup lambda execution environment
            inputStream.close();
            Files.delete(file.toPath());
            log.info("Media file successfully saved to temp and put into S3 bucket as '{}'.", transcriptionKey);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }

        Media media = new Media();
        media.setMediaFileUri(s3Client.getUrl(S3Utils.INPUT_BUCKET, transcriptionKey).toString());

        // Identify sample rate of wav file
        AudioFormat audioFormat;
        int sampleRate = 0;
        try {
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(
                    new ByteArrayInputStream(transcribeAudioRequest.getAudio()
                    ));
            audioFormat = audioInputStream.getFormat();

            inputStream.close();
            audioInputStream.close();

            if (audioFormat.getSampleRate() != AudioSystem.NOT_SPECIFIED) {
                sampleRate = (int) audioFormat.getSampleRate();
            }
            else {
                // If sampleRate is not specified, we will default to 48 kHz sample rate expected from the frontend
                sampleRate = 48_000;
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }

        // Create transcription job request with media, media details, model language, and output location
        // and start transcription job
        StartTranscriptionJobRequest startTranscriptionJobRequest = new StartTranscriptionJobRequest();
        String transcriptionJob = transcriptionId + "_job";
        startTranscriptionJobRequest.setTranscriptionJobName(transcriptionJob);

        startTranscriptionJobRequest.withMedia(media)
                .withMediaFormat(MediaFormat.Wav)
                .withMediaSampleRateHertz(sampleRate)
                .withLanguageCode(LanguageCode.EnUS)
                .withOutputBucketName(S3Utils.OUTPUT_BUCKET);

        transcribeClient.startTranscriptionJob(startTranscriptionJobRequest);
        log.info("TranscriptionJobRequest sent to client. Starting transcription job '{}'.", transcriptionJob);

        // Create a getTranscriptionJobRequest, which is used to check ongoing or finished job
        GetTranscriptionJobRequest getTranscriptionJobRequest = new GetTranscriptionJobRequest()
                .withTranscriptionJobName(transcriptionJob);

        // Poll the transcription job until it has completed or failed
        while (true) {
            // Get current job status
            GetTranscriptionJobResult jobResult = transcribeClient.getTranscriptionJob(getTranscriptionJobRequest);
            String jobStatus = jobResult.getTranscriptionJob().getTranscriptionJobStatus();

            // If job is in progress or queued, continue to loop until completed or failed
            if (jobStatus.equals(TranscriptionJobStatus.COMPLETED.toString())) {
                break;
            } else if (jobStatus.equals(TranscriptionJobStatus.FAILED.toString())) {
                String reason = jobResult.getTranscriptionJob().getFailureReason();
                throw new TranscriptionException(reason);
            }

            // Wait one second before polling job again
            try {
                sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException("Transcription process was interrupted.", e);
            }
        }

        // Stream transcription json from output S3 bucket
        S3Object object = s3Client.getObject(S3Utils.OUTPUT_BUCKET, transcriptionJob + ".json");
        String transcriptionResultJson = null;
        try {
            S3ObjectInputStream stream = object.getObjectContent();
            transcriptionResultJson = new String(stream.readAllBytes());
            stream.close();
        } catch (IOException e) {
            log.info("IOException.");
        }

        // Parse transcription json
        String transcript;
        try {
            transcript = TranscriptionUtils.parseJsonForTranscript(transcriptionResultJson);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Unable to parse transcription job result json.", e);
        }

        // Save transcription data to transcriptions table
        Transcription transcription = new Transcription();
        transcription.setTranscriptionId(transcriptionId);
        transcription.setJson(transcriptionResultJson);
        transcriptionDao.saveTranscription(transcription);

        // Create new note using transcript as note content
        Note note = new Note();
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