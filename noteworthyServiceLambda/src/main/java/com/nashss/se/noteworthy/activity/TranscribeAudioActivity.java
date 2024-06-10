package com.nashss.se.noteworthy.activity;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.AmazonS3URI;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.services.transcribe.AmazonTranscribe;
import com.amazonaws.services.transcribe.model.*;
import com.nashss.se.noteworthy.activity.requests.TranscribeAudioRequest;
import com.nashss.se.noteworthy.activity.results.TranscribeAudioResult;
import com.nashss.se.noteworthy.exceptions.TranscriptionException;
import com.nashss.se.noteworthy.models.NoteModel;

import com.nashss.se.noteworthy.utils.TranscriptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Inject;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static java.lang.Thread.sleep;

/**
 * Implementation of the TranscribeAudioActivity for the NoteworthyService's TranscribeAudio API.
 * This API allows the user to update an existing note.
 */
public class TranscribeAudioActivity {
    private final Logger log = LogManager.getLogger();
    private AmazonTranscribe amazonTranscribeClient;
    private static final String INPUT_BUCKET = "nss-s3-c04-u7-noteworthy-cito.mcclure";
    private static final String OUTPUT_BUCKET = "nss-s3-c04-u7-noteworthy-cito.mcclure-transcribe-output";

    /**
     * Instantiates a new TranscribeAudioActivity object.
     * @param amazonTranscribeClient AmazonTranscribe client.
     */
    @Inject
    public TranscribeAudioActivity(AmazonTranscribe amazonTranscribeClient) {
        this.amazonTranscribeClient = amazonTranscribeClient;
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

        // TODO: instantiate using Dagger
        AmazonS3 s3 = AmazonS3ClientBuilder
                .standard()
                .withRegion(Regions.US_EAST_2)
                .withClientConfiguration(new ClientConfiguration())
                .withCredentials(new DefaultAWSCredentialsProviderChain())
                .build();

        // TODO: investigate TTL for bucket and job?
        String id = TranscriptionUtils.generateID();
        LocalDateTime currentTime = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        String transcriptionName = currentTime + "_" + id;
        // transcription jobs do not allow colon character
        transcriptionName = transcriptionName.replace(":", ".");

        // TODO: better exception handling + check notes on when to catch vs. throw
        InputStream inputStream = new ByteArrayInputStream(transcribeAudioRequest.getAudio());
        String transcriptionKey = transcriptionName + "_key";
        try {
            log.info("Attempting to save wav file to temp and put into S3 bucket as '{}'.", transcriptionKey);
            File file = File.createTempFile(transcriptionName, ".wav");
            Files.copy(inputStream, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
            PutObjectRequest putObjectRequest = new PutObjectRequest(INPUT_BUCKET, transcriptionKey, file);
            s3.putObject(putObjectRequest);

            inputStream.close();
            log.info("Media file successfully saved to temp and put into S3 bucket as '{}'.", transcriptionKey);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }

        // TODO: try to put in S3 as stream instead of file
//        ByteArrayInputStream bais = new ByteArrayInputStream(transcribeAudioRequest.getAudio());
//        AudioInputStream stream;
//        try {
//            stream = AudioSystem.getAudioInputStream(bais);
//        } catch (Exception e) {
//            throw new RuntimeException("Exception during AudioInputStream creation", e);
//        }

//        log.info("Attempting to put media file into s3 bucket {} ...", BUCKET);
//        try {
//            s3.putObject(BUCKET, KEY, file2.toPath());
//            log.info("Media file successfully put into s3 bucket.");
//        } catch (AmazonServiceException e) {
//            throw new RuntimeException(
//                    String.format("Unable to upload media file to s3 bucket"), e);
//        }

        // TODO: set up media and strings, chain commands together using 'with' methods
        String transcriptionJob = transcriptionName + "_job";
        StartTranscriptionJobRequest startTranscriptionJobRequest = new StartTranscriptionJobRequest();
        startTranscriptionJobRequest.withLanguageCode(LanguageCode.EnUS);

        Media media = new Media();
        media.setMediaFileUri(s3.getUrl(INPUT_BUCKET, transcriptionKey).toString());
        startTranscriptionJobRequest.withMedia(media);
        startTranscriptionJobRequest.withOutputBucketName(OUTPUT_BUCKET);
        startTranscriptionJobRequest.setTranscriptionJobName(transcriptionJob);

        // TODO: need way to detect sample rate
        startTranscriptionJobRequest.withMediaFormat("wav");
        startTranscriptionJobRequest.withMediaSampleRateHertz(22050);

        log.info("TranscriptionJobRequest sent to client. Starting transcription job '{}'.", transcriptionJob);
        amazonTranscribeClient.startTranscriptionJob(startTranscriptionJobRequest);

        // Poll the transcription job status until it has completed or failed
//        Transcript transcript = new Transcript();
        GetTranscriptionJobRequest transcriptionJobRequest = new GetTranscriptionJobRequest()
                .withTranscriptionJobName(transcriptionJob);
        boolean continuePolling = true;
        while (continuePolling) {
            GetTranscriptionJobResult jobResult = amazonTranscribeClient.getTranscriptionJob(transcriptionJobRequest);
            String jobStatus = jobResult.getTranscriptionJob().getTranscriptionJobStatus();

            // TODO: change to if-elif
            switch (TranscriptionJobStatus.valueOf(jobStatus)) {
                case COMPLETED:
//                    transcript = jobResult.getTranscriptionJob().getTranscript();
                    continuePolling = false;
                    break;
                case FAILED:
                    String reason = jobResult.getTranscriptionJob().getFailureReason();
                    throw new TranscriptionException(reason);
            }

            // Wait 1s before checking job status again
            try {
                sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException("Transcription process was interrupted.", e);
            }
        }

        // TODO: Retrieve results from output S3 bucket
        // TODO: Updated todo - create new S3 bucket, set permissions, and parse resulting json data
        S3Object object = s3.getObject(OUTPUT_BUCKET, transcriptionJob + ".json");

        String transcriptionResult = null;
        try {
            S3ObjectInputStream stream = object.getObjectContent();
            transcriptionResult = new String(stream.readAllBytes());
            stream.close();
        } catch (IOException e) {
            log.info("IOException.");
        }

        // TODO: save results to new database table, update note content, and return transcription to FE
//        log.info("Job successful. Transcription output: '{}'.", transcript);

        return null;
    }
}

















