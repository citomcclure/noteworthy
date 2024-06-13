package com.nashss.se.noteworthy.dynamodb;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.nashss.se.noteworthy.dynamodb.models.Note;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

    // TODO: add unit test for update?

    @Test
    public void deleteNote_callsMapperWithNote() {
        // GIVEN
        String expectedEmail = "email@test.com";
        LocalDateTime expectedDate = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

        Note note = new Note();
        note.setEmail(expectedEmail);
        note.setDateCreated(expectedDate);

        // WHEN
        Note result = noteDao.deleteNote(note);

        // THEN
        verify(dynamoDBMapper).delete(note);
        assertEquals(note, result);
    }

    @Test
    public void getNote_callsMapperWithCompositeKey() {
        // GIVEN
        String expectedEmail = "email";
        LocalDateTime expectedDateCreated = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

        Note note = new Note();
        note.setEmail(expectedEmail);
        note.setDateCreated(expectedDateCreated);

        when(dynamoDBMapper.load(Note.class, expectedEmail, expectedDateCreated)).thenReturn(note);

        // WHEN
        Note result = noteDao.getNote(expectedEmail, expectedDateCreated);

        // THEN
        assertEquals(expectedEmail, result.getEmail(), "Expected emails to match.");
        assertEquals(expectedDateCreated, result.getDateCreated(), "Expected creation dates to match.");
    }
}
