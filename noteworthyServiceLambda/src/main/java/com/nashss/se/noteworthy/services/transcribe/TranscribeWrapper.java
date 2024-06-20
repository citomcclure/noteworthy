package com.nashss.se.noteworthy.services.transcribe;

import com.nashss.se.noteworthy.activity.TranscribeAudioActivity;
import com.nashss.se.noteworthy.exceptions.TranscriptionException;
import com.nashss.se.noteworthy.services.s3.S3Wrapper;

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

import javax.inject.Inject;
import javax.inject.Singleton;

import static java.lang.Thread.sleep;

/**
 * Wrapper class for Transcribe Client to abstract Amazon Transcribe related operations away
 * from {@link TranscribeAudioActivity}.
 */
@Singleton
public class TranscribeWrapper {
    private final Logger log = LogManager.getLogger();
    private final AmazonTranscribe transcribeClient;

    /**
     * Instantiates a new TranscribeWrapper.
     * @param transcribeClient the Amazon Transcribe client
     */
    @Inject
    public TranscribeWrapper(AmazonTranscribe transcribeClient) {
        this.transcribeClient = transcribeClient;
    }

    /**
     * Create a StartTranscriptionJobRequest with corresponding parameters and start transcription batch job.
     * @param transcriptionId transcriptionId
     * @param s3InputLocation the location of the uploaded audio file in the S3 input bucket
     * @param sampleRate the sample rate of the media file
     */
    public void startTranscriptionJob(String transcriptionId, String s3InputLocation, int sampleRate) {
        // Create transcription job request with parameters and start transcription job
        log.info("Creating StartTranscriptionJobRequest...");
        StartTranscriptionJobRequest startTranscriptionJobRequest = new StartTranscriptionJobRequest();
        startTranscriptionJobRequest.setTranscriptionJobName(transcriptionId);

        // Point media object to S3 input object location
        Media media = new Media();
        media.setMediaFileUri(s3InputLocation);

        startTranscriptionJobRequest.withMedia(media)
                .withMediaFormat(MediaFormat.Wav)
                .withMediaSampleRateHertz(sampleRate)
                .withLanguageCode(LanguageCode.EnUS)
                .withOutputBucketName(S3Wrapper.OUTPUT_BUCKET);

        transcribeClient.startTranscriptionJob(startTranscriptionJobRequest);
        log.info("Job sent as StartTranscriptionJobRequest to transcribe client. Job request details: " +
                        "Job name: '{}', Media format: '{}', Sample rate: '{} Hz', Language code: '{}'.",
                transcriptionId, MediaFormat.Wav, sampleRate, LanguageCode.EnUS);
    }

    /**
     * Create a getTranscriptionJobRequest, and poll the transcription job until it has completed or failed.
     * @param transcriptionId transcriptionId
     */
    public void pollTranscriptionJob(String transcriptionId) {
        GetTranscriptionJobRequest getTranscriptionJobRequest = new GetTranscriptionJobRequest()
                .withTranscriptionJobName(transcriptionId);

        // Set poll frequency and count every poll
        int pollCount = 0;
        int pollFrequency = 1_000;
        log.info("Polling job for job status every {} ms...", pollFrequency);

        while (true) {
            // Get current job status
            GetTranscriptionJobResult jobResult = transcribeClient.getTranscriptionJob(getTranscriptionJobRequest);
            String jobStatus = jobResult.getTranscriptionJob().getTranscriptionJobStatus();

            // If job is in progress or queued, continue to loop until completed or failed
            if (jobStatus.equals(TranscriptionJobStatus.COMPLETED.toString())) {
                log.info("Job '{}' completed. Took {} seconds.", transcriptionId, pollCount);
                break;
            } else if (jobStatus.equals(TranscriptionJobStatus.FAILED.toString())) {
                throw new TranscriptionException(String.format(
                        "Transcription failed due to the following reason: '%s'",
                        jobResult.getTranscriptionJob().getFailureReason()));
            }

            // Wait before polling job again
            try {
                sleep(pollFrequency);
            } catch (InterruptedException e) {
                throw new TranscriptionException("Thread was interrupted while waiting for next poll.", e);
            }

            pollCount++;
        }
    }
}
