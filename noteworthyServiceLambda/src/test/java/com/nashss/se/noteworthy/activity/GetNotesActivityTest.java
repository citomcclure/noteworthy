package com.nashss.se.noteworthy.activity;

import com.nashss.se.noteworthy.activity.requests.GetNotesRequest;
import com.nashss.se.noteworthy.activity.results.GetNotesResult;
import com.nashss.se.noteworthy.dynamodb.NoteDao;
import com.nashss.se.noteworthy.dynamodb.models.Note;

import com.nashss.se.noteworthy.models.NoteModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

public class GetNotesActivityTest {
    @Mock
    private NoteDao noteDao;

    private GetNotesActivity getNotesActivity;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        getNotesActivity = new GetNotesActivity(noteDao);
    }

    @Test
    public void handleRequest_savedNotesFound_returnsListOfNoteModelsInResult() {
        // GIVEN
        String expectedNoteId = "12345";
        String expectedTitle = "expectedTitle";
        String expectedContent = "expectedContent";
        LocalDateTime expectedDateUpdated = LocalDateTime.now();
        String expectedEmail = "email@test.com";

        Note note = new Note();
        note.setNoteId(expectedNoteId);
        note.setTitle(expectedTitle);
        note.setContent(expectedContent);
        note.setDateUpdated(expectedDateUpdated);
        note.setEmail(expectedEmail);

        when(noteDao.getAllNotes(expectedEmail)).thenReturn(List.of(note));

        GetNotesRequest request = GetNotesRequest.builder()
                .withEmail(expectedEmail)
                .build();

        // WHEN
        GetNotesResult result = getNotesActivity.handleRequest(request);
        NoteModel resultNote = result.getNoteList().get(0);

        // THEN
        assertEquals(expectedNoteId, resultNote.getNoteId());
        assertEquals(expectedTitle, resultNote.getTitle());
        assertEquals(expectedContent, resultNote.getContent());
        assertEquals(expectedDateUpdated, resultNote.getDateUpdated());
        assertEquals(expectedEmail, resultNote.getEmail());
    }
}

