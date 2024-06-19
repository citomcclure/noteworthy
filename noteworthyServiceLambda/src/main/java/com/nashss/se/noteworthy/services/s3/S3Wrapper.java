package com.nashss.se.noteworthy.services.s3;

import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.nashss.se.noteworthy.activity.TranscribeAudioActivity;
import com.nashss.se.noteworthy.exceptions.TranscriptionException;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Wrapper class for S3 Client to abstract S3 related operations away from {@link TranscribeAudioActivity}.
 */
@Singleton
public class S3Wrapper {
    public static final String INPUT_BUCKET = "nss-s3-c04-u7-noteworthy-cito.mcclure";
    public static final String OUTPUT_BUCKET = "nss-s3-c04-u7-noteworthy-cito.mcclure-transcribe-output";
    private final Logger log = LogManager.getLogger();
    private final AmazonS3 s3Client;

    /**
     * Instantiates a new S3Wrapper.
     * @param s3Client the Amazon S3 Client
     */
    @Inject
    public S3Wrapper(AmazonS3 s3Client) {
        this.s3Client = s3Client;
    }

    /**
     * I/O streaming of audio byte array to temporary file in execution environment. This file is uploaded to
     * S3 input bucket and then streams are closed after use.
     * @param transcriptionId transcriptionID
     * @param audio byte array of media file from request object
     */
    public void putAudioInS3(String transcriptionId, byte[] audio) {
        log.info("Attempting to save WAV file to temp and put into S3 bucket as '{}'...", transcriptionId);

        try {
            // Create I/O stream of audio and save to temp directory
            InputStream inputStream = new ByteArrayInputStream(audio);
            File file = File.createTempFile(transcriptionId, ".wav");
            Files.copy(inputStream, file.toPath(), StandardCopyOption.REPLACE_EXISTING);

            // Put new temp file into S3 bucket
            PutObjectRequest putObjectRequest = new PutObjectRequest(INPUT_BUCKET, transcriptionId, file);
            s3Client.putObject(putObjectRequest);

            // Close stream and cleanup lambda execution environment
            inputStream.close();
            Files.delete(file.toPath());
        } catch (IOException e) {
            throw new TranscriptionException("Issue streaming WAV file.", e);
        }

        log.info("Wav file put into S3 bucket as '{}'.", transcriptionId);
    }

    /**
     * After uploading to S3, obtain the url for future reference.
     * @param transcriptionId transcriptionID
     * @return url string of S3 object location
     */
    public String getAudioLocation(String transcriptionId) {
        return s3Client.getUrl(INPUT_BUCKET, transcriptionId).toString();
    }

    /**
     * After transcription job is finished, retrieve the transcription job results.
     * @param transcriptionId transcriptionId
     * @return the S3Object representation, which will be the json response of the transcription job
     */
    public String getTranscriptionJobResult(String transcriptionId) {
        log.info("Obtaining transcription json from completed job at output S3 bucket: '{}' ...",
                OUTPUT_BUCKET);

        // Get object from S3
        S3Object s3Object = s3Client.getObject(OUTPUT_BUCKET, transcriptionId + ".json");

        // Stream json from S3 object
        String transcriptionResultJson = null;
        try {
            S3ObjectInputStream stream = s3Object.getObjectContent();
            transcriptionResultJson = new String(stream.readAllBytes());
            stream.close();
        } catch (IOException e) {
            throw new TranscriptionException("Issue streaming transcription results.", e);
        }

        return transcriptionResultJson;
    }
}
