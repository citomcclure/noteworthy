package com.nashss.se.noteworthy.dynamodb;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.nashss.se.noteworthy.dynamodb.models.Note;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

public class NoteDaoTest {
    @Mock
    DynamoDBMapper dynamoDBMapper;

    private NoteDao noteDao;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        noteDao = new NoteDao(dynamoDBMapper);
    }

    @Test
    public void getAllNotes_withEmail_callsMapperWithPartitionKey() {
        // GIVEN
        String email = "email";
        // instantiating captor
        ArgumentCaptor<DynamoDBQueryExpression<Note>> captor = ArgumentCaptor.forClass(DynamoDBQueryExpression.class);

        // WHEN
        noteDao.getAllNotes(email);

        // THEN
        // capturing the argument in a verification
        verify(dynamoDBMapper).query(eq(Note.class), captor.capture());
        // inspecting the captured value
        Note noteQueried = captor.getValue().getHashKeyValues();
        assertEquals(email, noteQueried.getEmail(), "Expected query expression to query for " +
                "partition key: " + email);
    }

    @Test
    public void saveNote_callsMapperWithNote() {
        // GIVEN
        Note note = new Note();

        // WHEN
        Note result = noteDao.saveNote(note);

        // THEN
        verify(dynamoDBMapper).save(note);
        assertEquals(note, result);
    }
}
