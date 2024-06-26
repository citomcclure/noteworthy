package com.nashss.se.noteworthy.services.transcribe;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.transcribe.AmazonTranscribe;
import com.amazonaws.services.transcribe.AmazonTranscribeClientBuilder;

public class TranscribeClientProvider {
    /**
     * Returns Amazon Transcribe client using default region.
     * @return AmazonTranscribe
     */
    public static AmazonTranscribe getTranscribeClient() {
        return getTranscribeClient(Regions.US_EAST_2);
    }

    /**
     * Returns Amazon Transcribe client using default region.
     * @param region If present, will be used as the region for the Amazon Transcribe client
     * @return AmazonTranscribe
     */
    public static AmazonTranscribe getTranscribeClient(Regions region) {
        if (region == null) {
            throw new IllegalArgumentException("Region cannot be null");
        }

        return AmazonTranscribeClientBuilder
                .standard()
                .withCredentials(DefaultAWSCredentialsProviderChain.getInstance())
                .withRegion(region)
                .build();
    }
}
