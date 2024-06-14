package com.nashss.se.noteworthy.dynamodb;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.nashss.se.noteworthy.dynamodb.models.Note;
import com.nashss.se.noteworthy.dynamodb.models.Transcription;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

public class TranscriptionDaoTest {
    @Mock
    DynamoDBMapper dynamoDBMapper;

    private TranscriptionDao transcriptionDao;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        transcriptionDao = new TranscriptionDao(dynamoDBMapper);
    }


    @Test
    public void saveNote_callsMapperWithNote() {
        // GIVEN
        Transcription transcription = new Transcription();

        // WHEN
        Transcription result = transcriptionDao.saveTranscription(transcription);

        // THEN
        verify(dynamoDBMapper).save(transcription);
        assertEquals(transcription, result);
    }
}
