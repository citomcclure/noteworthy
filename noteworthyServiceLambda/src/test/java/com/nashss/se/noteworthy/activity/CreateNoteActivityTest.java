package com.nashss.se.noteworthy.activity;

import com.nashss.se.noteworthy.activity.requests.CreateNoteRequest;
import com.nashss.se.noteworthy.activity.results.CreateNoteResult;
import com.nashss.se.noteworthy.services.dynamodb.NoteDao;
import com.nashss.se.noteworthy.models.NoteModel;

import com.nashss.se.noteworthy.services.dynamodb.models.Note;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

public class CreateNoteActivityTest {
    @Mock
    private NoteDao noteDao;

    private CreateNoteActivity createNoteActivity;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        createNoteActivity = new CreateNoteActivity(noteDao);
    }

    @Test
    public void handleRequest_createsAndSavesNote() {
        // GIVEN
        String expectedTitle = "expectedTitle";
        String expectedContent = "expectedContent";
        String expectedEmail = "email@test.com";

        CreateNoteRequest request = CreateNoteRequest.builder()
                .withTitle(expectedTitle)
                .withContent(expectedContent)
                .withEmail(expectedEmail)
                .build();

        ArgumentCaptor<Note> captor = ArgumentCaptor.forClass(Note.class);

        // WHEN
        CreateNoteResult result = createNoteActivity.handleRequest(request);
        NoteModel resultNote = result.getNote();

        verify(noteDao).saveNote(captor.capture());

        Note note = new Note();
        note.setTitle(expectedTitle);
        note.setContent(expectedContent);
        note.setDateCreated(captor.getValue().getDateCreated());
        note.setDateUpdated(captor.getValue().getDateUpdated());
        note.setEmail(expectedEmail);

        // THEN
        assertEquals(expectedTitle, resultNote.getTitle());
        assertEquals(expectedContent, resultNote.getContent());
        assertNotNull(resultNote.getDateCreated());
        assertNotNull(resultNote.getDateUpdated());
        assertEquals(expectedEmail, resultNote.getEmail());

        // Test Note POJO methods
        assertTrue(note.equals(captor.getValue()));
        assertEquals(note.toString(), captor.getValue().toString());
        assertEquals(note.hashCode(), captor.getValue().hashCode());
    }
}




