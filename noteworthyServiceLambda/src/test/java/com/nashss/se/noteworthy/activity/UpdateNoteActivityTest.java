package com.nashss.se.noteworthy.activity;

import com.nashss.se.noteworthy.activity.requests.UpdateNoteRequest;
import com.nashss.se.noteworthy.activity.results.UpdateNoteResult;
import com.nashss.se.noteworthy.dynamodb.NoteDao;
import com.nashss.se.noteworthy.dynamodb.models.Note;
import com.nashss.se.noteworthy.models.NoteModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
    public void handleRequest_goodRequest_updatesNote() {
        // GIVEN
        String expectedTitle = "New Title";
        String expectedContent = "new content";
        LocalDateTime expectedDateCreated = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        String expectedEmail = "email@test.com";

        UpdateNoteRequest request = UpdateNoteRequest.builder()
                .withTitle(expectedTitle)
                .withContent(expectedContent)
                .withDateCreated(expectedDateCreated)
                .withEmail(expectedEmail)
                .build();

        // WHEN
        UpdateNoteResult result = updateNoteActivity.handleRequest(request);
        NoteModel resultNote = result.getNote();

        // THEN
        assertEquals(expectedTitle, resultNote.getTitle());
        assertEquals(expectedContent, resultNote.getContent());
        assertNotNull(resultNote.getDateCreated());
        assertNotNull(resultNote.getDateUpdated());
        assertEquals(expectedEmail, resultNote.getEmail());
    }
}
