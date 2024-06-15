package com.nashss.se.noteworthy.activity;

import com.nashss.se.noteworthy.activity.requests.DeleteNoteRequest;
import com.nashss.se.noteworthy.activity.results.DeleteNoteResult;
import com.nashss.se.noteworthy.services.dynamodb.NoteDao;
import com.nashss.se.noteworthy.models.NoteModel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class DeleteNoteActivityTest {
    @Mock
    private NoteDao noteDao;

    private DeleteNoteActivity deleteNoteActivity;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        deleteNoteActivity = new DeleteNoteActivity(noteDao);
    }

    @Test
    public void handleRequest_goodRequest_updatesNote() {
        // GIVEN
        LocalDateTime expectedDateCreated = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        String expectedEmail = "email@test.com";

        DeleteNoteRequest request = DeleteNoteRequest.builder()
                .withDateCreated(expectedDateCreated)
                .withEmail(expectedEmail)
                .build();

        // WHEN
        DeleteNoteResult result = deleteNoteActivity.handleRequest(request);
        NoteModel resultNote = result.getNote();

        // THEN
        assertNotNull(resultNote.getDateCreated());
        assertEquals(expectedEmail, resultNote.getEmail());
    }
}
