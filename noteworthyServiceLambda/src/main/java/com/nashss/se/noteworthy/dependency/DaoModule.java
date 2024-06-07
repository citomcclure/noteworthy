package com.nashss.se.noteworthy.dependency;

import com.nashss.se.noteworthy.dynamodb.DynamoDbClientProvider;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.transcribe.AmazonTranscribe;

import com.nashss.se.noteworthy.transcribe.AmazonTranscribeClientProvider;
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
        return new DynamoDBMapper(DynamoDbClientProvider.getDynamoDBClient(Regions.US_EAST_2));
    }

    @Singleton
    @Provides
    public AmazonTranscribe provideAmazonTranscribeClient() {
        return AmazonTranscribeClientProvider.getAmazonTranscribeClient();
    }
}
