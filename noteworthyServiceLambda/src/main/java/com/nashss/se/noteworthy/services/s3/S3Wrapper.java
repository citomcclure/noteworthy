package com.nashss.se.noteworthy.services.s3;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.nashss.se.noteworthy.exceptions.TranscriptionException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

@Singleton
public class S3Wrapper {
    private final Logger log = LogManager.getLogger();
    private final AmazonS3 s3Client;

    // TODO: bring s3 utils over here
    // TODO: bring logs over here and add logger

    @Inject
    public S3Wrapper(AmazonS3 s3Client) {
        this.s3Client = s3Client;
    }

    // TODO: separate try for opening stream and closing stream
    public void putAudioInS3(String transcriptionKey, byte[] audio) {
        try {
            // Create I/O stream of audio and save to temp directory
            InputStream inputStream = new ByteArrayInputStream(audio);
            File file = File.createTempFile(transcriptionKey, ".wav");
            Files.copy(inputStream, file.toPath(), StandardCopyOption.REPLACE_EXISTING);

            // Put new temp file into S3 bucket
            PutObjectRequest putObjectRequest = new PutObjectRequest(S3Utils.INPUT_BUCKET, transcriptionKey, file);
            s3Client.putObject(putObjectRequest);

            // Close stream and cleanup lambda execution environment
            inputStream.close();
            Files.delete(file.toPath());
        } catch (IOException e) {
            throw new TranscriptionException("Issue streaming WAV file.", e);
        }
    }

    // TODO: get rid of key string
    public String getAudioLocation(String transcriptionKey) {
        return s3Client.getUrl(S3Utils.INPUT_BUCKET, transcriptionKey).toString();
    }

    public S3Object getTranscriptionJobResult(String transcriptionJob) {
        return s3Client.getObject(S3Utils.OUTPUT_BUCKET, transcriptionJob + ".json");
    }
}
