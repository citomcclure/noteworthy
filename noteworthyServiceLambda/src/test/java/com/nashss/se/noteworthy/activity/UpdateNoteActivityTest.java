package com.nashss.se.noteworthy.activity;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.nashss.se.noteworthy.activity.requests.UpdateNoteRequest;
import com.nashss.se.noteworthy.activity.results.UpdateNoteResult;
import com.nashss.se.noteworthy.dynamodb.NoteDao;
import com.nashss.se.noteworthy.dynamodb.models.Note;
import com.nashss.se.noteworthy.models.NoteModel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class UpdateNoteActivityTest {
    @Mock
    private NoteDao noteDao;

    private UpdateNoteActivity updateNoteActivity;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        updateNoteActivity = new UpdateNoteActivity(noteDao);
    }

    @Test
    public void handleRequest_updatedTitleAndContent_updatesNote() {
        // GIVEN
        LocalDateTime dateCreated = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        String email = "email@test.com";
        String expectedTranscriptionId = "12345";

        // Original note
        Note originalNote = new Note();

        originalNote.setTitle("Existing Title");
        originalNote.setContent("Existing content");
        originalNote.setDateUpdated(dateCreated);
        originalNote.setDateCreated(dateCreated);
        originalNote.setEmail(email);

        // Updated note
        String expectedTitle = "New Title";
        String expectedContent = "New content";

        UpdateNoteRequest request = UpdateNoteRequest.builder()
                .withTitle(expectedTitle)
                .withContent(expectedContent)
                .withDateCreated(dateCreated)
                .withEmail(email)
                .build();

        when(noteDao.getNote(email, dateCreated)).thenReturn(originalNote);
        when(noteDao.saveNote(originalNote)).thenReturn(originalNote);

        // WHEN
        UpdateNoteResult result = updateNoteActivity.handleRequest(request);
        NoteModel resultNote = result.getNote();

        // THEN
        assertEquals(expectedTitle, resultNote.getTitle());
        assertEquals(expectedContent, resultNote.getContent());
        assertNotNull(resultNote.getDateCreated());
        assertNotNull(resultNote.getDateUpdated());
        assertEquals(email, resultNote.getEmail());
    }
}