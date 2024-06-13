package com.nashss.se.noteworthy.dynamodb;

import com.nashss.se.noteworthy.dynamodb.models.Transcription;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Accesses data for a transcription using {@link Transcription} to represent the model in DynamoDB.
 */
@Singleton
public class TranscriptionDao {
    private final DynamoDBMapper dynamoDBMapper;

    /**
     * Instantiates a TranscriptionDao object.
     *
     * @param dynamoDBMapper the {@link DynamoDBMapper} used to interact with the transcriptions table
     */
    @Inject
    public TranscriptionDao(DynamoDBMapper dynamoDBMapper) {
        this.dynamoDBMapper = dynamoDBMapper;
    }

    /**
     * Saves the transcription.
     *
     * @param transcription The transcription to save.
     * @return the Transcription object that was saved.
     */
    public Transcription saveTranscription(Transcription transcription) {
        this.dynamoDBMapper.save(transcription);
        return transcription;
    }
}
