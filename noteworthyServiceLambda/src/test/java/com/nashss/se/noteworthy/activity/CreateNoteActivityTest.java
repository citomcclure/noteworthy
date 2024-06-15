package com.nashss.se.noteworthy.activity;

import com.nashss.se.noteworthy.activity.requests.CreateNoteRequest;
import com.nashss.se.noteworthy.activity.results.CreateNoteResult;
import com.nashss.se.noteworthy.services.dynamodb.NoteDao;
import com.nashss.se.noteworthy.models.NoteModel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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

        // WHEN
        CreateNoteResult result = createNoteActivity.handleRequest(request);
        NoteModel resultNote = result.getNote();

        // THEN
        assertEquals(expectedTitle, resultNote.getTitle());
        assertEquals(expectedContent, resultNote.getContent());
        assertNotNull(resultNote.getDateCreated());
        assertNotNull(resultNote.getDateUpdated());
        assertEquals(expectedEmail, resultNote.getEmail());
    }
}




