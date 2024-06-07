package com.nashss.se.noteworthy.transcribe;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.transcribe.AmazonTranscribe;
import com.amazonaws.services.transcribe.AmazonTranscribeClientBuilder;

public class AmazonTranscribeClientProvider {
    /**
     * Returns Amazon Transcribe client using default region.
     * @return AmazonTranscribe
     */
    public static AmazonTranscribe getAmazonTranscribeClient() {
        return getAmazonTranscribeClient(Regions.US_EAST_2);
    }

    /**
     * Returns Amazon Transcribe client using default region.
     * @param region If present, will be used as the region for the Amazon Transcribe client
     * @return AmazonTranscribe
     */
    public static AmazonTranscribe getAmazonTranscribeClient(Regions region) {
        if (null == region) {
            throw new IllegalArgumentException("region cannot be null");
        }

        return AmazonTranscribeClientBuilder
                .standard()
                .withCredentials(DefaultAWSCredentialsProviderChain.getInstance())
                .withRegion(region)
                .build();
    }
}
