package com.nashss.se.noteworthy.dependency;

import com.nashss.se.noteworthy.services.dynamodb.DynamoDbClientProvider;
import com.nashss.se.noteworthy.services.s3.S3ClientProvider;
import com.nashss.se.noteworthy.services.transcribe.TranscribeClientProvider;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.transcribe.AmazonTranscribe;

import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;

/**
 * Dagger Module providing dependencies for DAO classes.
 */
@Module
public class DaoModule {
    /**
     * Provides a DynamoDBMapper singleton instance.
     *
     * @return DynamoDBMapper object
     */
    @Singleton
    @Provides
    public DynamoDBMapper provideDynamoDBMapper() {
        return new DynamoDBMapper(DynamoDbClientProvider.getDynamoDBClient());
    }

    /**
     * Provides an AmazonTranscribe singleton instance.
     * @return AmazonTranscribe object
     */
    @Singleton
    @Provides
    public AmazonTranscribe provideTranscribeClient() {
        return TranscribeClientProvider.getTranscribeClient();
    }

    /**
     * Provides an AmazonS3 singleton instance.
     * @return AmazonS3 object
     */
    @Singleton
    @Provides
    public AmazonS3 provideAmazonS3Client() {
        return S3ClientProvider.getS3Client();
    }
}
