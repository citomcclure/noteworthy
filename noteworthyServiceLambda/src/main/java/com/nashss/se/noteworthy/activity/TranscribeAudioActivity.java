package com.nashss.se.noteworthy.activity;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.transcribe.AmazonTranscribe;
import com.amazonaws.services.transcribe.AmazonTranscribeClient;
import com.amazonaws.services.transcribe.model.LanguageCode;
import com.amazonaws.services.transcribe.model.Media;
import com.amazonaws.services.transcribe.model.StartTranscriptionJobRequest;
import com.amazonaws.services.transcribe.model.StartTranscriptionJobResult;
import com.nashss.se.noteworthy.activity.requests.TranscribeAudioRequest;
import com.nashss.se.noteworthy.activity.results.TranscribeAudioResult;
import com.nashss.se.noteworthy.dynamodb.NoteDao;
import com.nashss.se.noteworthy.models.NoteModel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Inject;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Implementation of the TranscribeAudioActivity for the NoteworthyService's TranscribeAudio API.
 * This API allows the user to update an existing note.
 */
public class TranscribeAudioActivity {
    private final Logger log = LogManager.getLogger();
    private AmazonTranscribe amazonTranscribeClient;
    private static final String BUCKET = "nss-s3-c04-u7-noteworthy-cito.mcclure";
    private static final String KEY = "key_transcription_gettysburg_new_1";

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
    public TranscribeAudioResult handleRequest(TranscribeAudioRequest transcribeAudioRequest) {
        log.info("Received TranscribeAudioRequest");
        log.info("Length of audio in bytes: {}", transcribeAudioRequest.getAudio().length);

        AmazonS3 s3 = AmazonS3ClientBuilder
                .standard()
                .withRegion(Regions.US_EAST_2)
                .withClientConfiguration(new ClientConfiguration())
                .withCredentials(new DefaultAWSCredentialsProviderChain())
                .build();

        // TODO: Update key, file, and job naming structure
        InputStream inputStream = new ByteArrayInputStream(transcribeAudioRequest.getAudio());
        try {
            File file = File.createTempFile("transcription_gettysburg_new_1", ".wav");
            Files.copy(inputStream, file.toPath(), StandardCopyOption.REPLACE_EXISTING);

            System.out.println("docker breakpoint");
            PutObjectRequest putObjectRequest = new PutObjectRequest(BUCKET, KEY, file);

            s3.putObject(putObjectRequest);
            log.info("Media file successfully put into s3 bucket.");

        } catch (Exception e) {
            throw new RuntimeException("Could not save file or upload to s3.", e);
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
        StartTranscriptionJobRequest transcriptionJobRequest = new StartTranscriptionJobRequest();
        transcriptionJobRequest.withLanguageCode(LanguageCode.EnUS);

        Media media = new Media();
        media.setMediaFileUri(s3.getUrl(BUCKET, KEY).toString());
        transcriptionJobRequest.withMedia(media);

        String transcriptionJobName = "job_transcription_gettysburg_new_1";
        transcriptionJobRequest.setTranscriptionJobName(transcriptionJobName);

        transcriptionJobRequest.withMediaFormat("wav");
        transcriptionJobRequest.withMediaSampleRateHertz(22050);

        log.info("TranscriptionJobRequest sent to client. Starting transcription job.");
        StartTranscriptionJobResult result = amazonTranscribeClient.startTranscriptionJob(transcriptionJobRequest);
        log.info("Job successful. Output: {}", result.getTranscriptionJob().getTranscript());

        // TODO: access results of job after some amount of time
//        while (true) {
//            try {
//                this.wait(1000);
//            } catch (InterruptedException e) {
//                throw new RuntimeException(
//                        String.format("1s wait was interrupted."), e);
//            }

        // TODO: save results to new database table, update note content, and return transcription to FE

        return null;
    }
}

















