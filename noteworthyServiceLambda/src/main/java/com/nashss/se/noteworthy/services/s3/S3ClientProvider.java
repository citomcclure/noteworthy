package com.nashss.se.noteworthy.services.s3;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

public class S3ClientProvider {
    /**
     * Returns Amazon S3 client using default region.
     * @return AmazonS3
     */
    public static AmazonS3 getS3Client() {
        return getS3Client(Regions.US_EAST_2);
    }

    /**
     * Returns Amazon S3 client using default region.
     * @param region If present, will be used as the region for the Amazon S3 client
     * @return AmazonS3
     */
    public static AmazonS3 getS3Client(Regions region) {
        if (null == region) {
            throw new IllegalArgumentException("region cannot be null");
        }

        return AmazonS3ClientBuilder
                .standard()
                .withRegion(region)
                .withClientConfiguration(new ClientConfiguration())
                .withCredentials(new DefaultAWSCredentialsProviderChain())
                .build();


    }
}
