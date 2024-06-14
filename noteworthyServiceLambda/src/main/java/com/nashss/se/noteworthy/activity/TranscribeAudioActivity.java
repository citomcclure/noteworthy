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
import javax.sound.sampled.UnsupportedAudioFileException;
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
    public TranscribeAudioResult handleRequest(TranscribeAudioRequest transcribeAudioRequest) {
        log.info("Received TranscribeAudioRequest for email '{}' with media file of size {} KB.",
                transcribeAudioRequest.getEmail(), transcribeAudioRequest.getAudio().length / 1_000);

        if (transcribeAudioRequest.getAudio() == null) {
            throw new TranscriptionException("Media cannot be null. Please include WAV file with request.");
        }

        // Create unique transcription identifier for bucket keys, transcription job, and transcription output
        LocalDateTime currentTime = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        String transcriptionId = TranscriptionUtils.generateTranscriptionId(currentTime.toString());

        // Ensure media file is valid WAV file and identify sample rate
        AudioFormat audioFormat;
        int sampleRate = 0;
        log.info("Attempting to identify sample rate...");
        try {
            // Create I/O stream of audio
            InputStream inputStream = new ByteArrayInputStream(transcribeAudioRequest.getAudio());

            // Stream as AudioInputStream to get audio format
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(inputStream);
            audioFormat = audioInputStream.getFormat();

            inputStream.close();
            audioInputStream.close();

            if (audioFormat.getSampleRate() != AudioSystem.NOT_SPECIFIED) {
                sampleRate = (int) audioFormat.getSampleRate();
                log.info("Sample rate correctly identified as {} Hz.", sampleRate);
            }
            else {
                // If sampleRate is not specified, we will default to 48 kHz sample rate expected from the frontend
                sampleRate = 48_000;
                log.info("Could not identify sample rate, setting to default.");
            }
        } catch (IOException e) {
            throw new TranscriptionException("Issue streaming WAV file.", e);
        } catch (UnsupportedAudioFileException e) {
            throw new TranscriptionException("Media file not recognized as valid WAV file.", e);
        }

        // Stream audio to temp file and store in S3
        String transcriptionKey = transcriptionId + "_key";
        log.info("Attempting to save WAV file to temp and put into S3 bucket as '{}'...", transcriptionKey);
        try {
            // Create I/O stream of audio and save to temp directory
            InputStream inputStream = new ByteArrayInputStream(transcribeAudioRequest.getAudio());
            File file = File.createTempFile(transcriptionId, ".wav");
            Files.copy(inputStream, file.toPath(), StandardCopyOption.REPLACE_EXISTING);

            // Put new temp file into S3 bucket
            PutObjectRequest putObjectRequest = new PutObjectRequest(S3Utils.INPUT_BUCKET, transcriptionKey, file);
            s3Client.putObject(putObjectRequest);
            log.info("Wav file put into S3 bucket as '{}'.", transcriptionKey);

            // Close stream and cleanup lambda execution environment
            inputStream.close();
            Files.delete(file.toPath());
        } catch (IOException e) {
            throw new TranscriptionException("Issue streaming WAV file.", e);
        }

        // Create transcription job request with media, media details, model language, and output location
        // and start transcription job
        log.info("Creating StartTranscriptionJobRequest...");

        Media media = new Media();
        media.setMediaFileUri(s3Client.getUrl(S3Utils.INPUT_BUCKET, transcriptionKey).toString());

        StartTranscriptionJobRequest startTranscriptionJobRequest = new StartTranscriptionJobRequest();
        String transcriptionJob = transcriptionId + "_job";
        startTranscriptionJobRequest.setTranscriptionJobName(transcriptionJob);

        startTranscriptionJobRequest.withMedia(media)
                .withMediaFormat(MediaFormat.Wav)
                .withMediaSampleRateHertz(sampleRate)
                .withLanguageCode(LanguageCode.EnUS)
                .withOutputBucketName(S3Utils.OUTPUT_BUCKET);

        transcribeClient.startTranscriptionJob(startTranscriptionJobRequest);
        log.info("Job sent as StartTranscriptionJobRequest to transcribe client. Job request details: " +
                        "Job name: '{}', Media format: '{}', Sample rate: '{} Hz', Language code: '{}'.",
                        transcriptionJob, MediaFormat.Wav, sampleRate, LanguageCode.EnUS);

        // Create a getTranscriptionJobRequest, and poll the transcription job until it has completed or failed
        GetTranscriptionJobRequest getTranscriptionJobRequest = new GetTranscriptionJobRequest()
                .withTranscriptionJobName(transcriptionJob);
        int pollFrequency = 1_000;
        log.info("Polling job for status every {} ms.", pollFrequency);
        while (true) {
            // Get current job status
            GetTranscriptionJobResult jobResult = transcribeClient.getTranscriptionJob(getTranscriptionJobRequest);
            String jobStatus = jobResult.getTranscriptionJob().getTranscriptionJobStatus();

            // If job is in progress or queued, continue to loop until completed or failed
            if (jobStatus.equals(TranscriptionJobStatus.COMPLETED.toString())) {
                log.info("Job '{}' completed.", transcriptionJob);
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
        log.info("Obtaining transcription json from completed job at output S3 bucket: '{}' ...",
                S3Utils.OUTPUT_BUCKET);
        S3Object object = s3Client.getObject(S3Utils.OUTPUT_BUCKET, transcriptionJob + ".json");
        String transcriptionResultJson = null;
        try {
            S3ObjectInputStream stream = object.getObjectContent();
            transcriptionResultJson = new String(stream.readAllBytes());
            stream.close();
        } catch (IOException e) {
            throw new TranscriptionException("Issue streaming transcription results.", e);
        }

        // Parse transcription json
        String transcript;
        try {
            transcript = TranscriptionUtils.parseJsonForTranscript(transcriptionResultJson);
            log.info("Transcription json successfully obtained and parsed. Transcript: '{}'", transcript);
        } catch (JsonProcessingException e) {
            throw new TranscriptionException("Unable to parse transcription job result json.", e);
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