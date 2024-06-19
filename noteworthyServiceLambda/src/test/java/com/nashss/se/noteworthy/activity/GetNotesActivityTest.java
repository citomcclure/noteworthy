package com.nashss.se.noteworthy.activity;

import com.nashss.se.noteworthy.activity.requests.GetNotesRequest;
import com.nashss.se.noteworthy.activity.results.GetNotesResult;
import com.nashss.se.noteworthy.exceptions.InvalidAttributeValueException;
import com.nashss.se.noteworthy.services.dynamodb.NoteDao;
import com.nashss.se.noteworthy.services.dynamodb.models.Note;
import com.nashss.se.noteworthy.models.NoteModel;

import com.nashss.se.noteworthy.models.NoteOrder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
        String expectedTitle = "expectedTitle";
        String expectedContent = "expectedContent";
        LocalDateTime expectedDate = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        String expectedEmail = "email@test.com";

        Note note = new Note();
        note.setTitle(expectedTitle);
        note.setContent(expectedContent);
        note.setDateCreated(expectedDate);
        note.setDateUpdated(expectedDate);
        note.setEmail(expectedEmail);

        when(noteDao.getAllNotes(expectedEmail)).thenReturn(List.of(note));

        GetNotesRequest request = GetNotesRequest.builder()
                .withEmail(expectedEmail)
                .build();

        // WHEN
        GetNotesResult result = getNotesActivity.handleRequest(request);
        NoteModel resultNote = result.getNoteList().get(0);

        // THEN
        assertEquals(expectedTitle, resultNote.getTitle());
        assertEquals(expectedContent, resultNote.getContent());
        assertEquals(expectedDate, resultNote.getDateCreated());
        assertEquals(expectedDate, resultNote.getDateUpdated());
        assertEquals(expectedEmail, resultNote.getEmail());
    }

    @Test
    public void handleRequest_noNotesForUser_returnsEmptyListOfNoteModelsInResult() {
        // GIVEN
        String expectedEmail = "email@test.com";

        when(noteDao.getAllNotes(expectedEmail)).thenReturn(Collections.emptyList());

        GetNotesRequest request = GetNotesRequest.builder()
                .withEmail(expectedEmail)
                .build();

        // WHEN
        GetNotesResult result = getNotesActivity.handleRequest(request);

        // THEN
        assertEquals(0, result.getNoteList().size());
    }

    @Test
    public void handleRequest_noOrder_returnsDefaultOrder() {
        // GIVEN
        String expectedEmail = "email@test.com";

        Note note1 = new Note();
        LocalDateTime olderDate = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        note1.setDateCreated(olderDate);
        note1.setEmail(expectedEmail);

        Note note2 = new Note();
        LocalDateTime newerDate = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        note2.setDateCreated(newerDate);
        note2.setEmail(expectedEmail);

        when(noteDao.getAllNotes(expectedEmail)).thenReturn(List.of(note2, note1));

        GetNotesRequest request = GetNotesRequest.builder()
                .withEmail(expectedEmail)
                .build();

        // WHEN
        GetNotesResult result = getNotesActivity.handleRequest(request);
        NoteModel firstNoteResult = result.getNoteList().get(0);
        NoteModel secondNoteResult = result.getNoteList().get(1);

        // THEN
        assertEquals(firstNoteResult.getDateCreated(), note2.getDateCreated(),
                "Expected newest note to be first in list.");
        assertEquals(secondNoteResult.getDateCreated(), note1.getDateCreated(),
                "Expected oldest note to be last in list.");
    }

    @Test
    public void handleRequest_defaultReversedOrder_returnsCorrectOrder() {
        // GIVEN
        String expectedEmail = "email@test.com";

        Note note1 = new Note();
        LocalDateTime olderDate = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        note1.setDateCreated(olderDate);
        note1.setEmail(expectedEmail);

        Note note2 = new Note();
        LocalDateTime newerDate = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        note2.setDateCreated(newerDate);
        note2.setEmail(expectedEmail);

        when(noteDao.getAllNotes(expectedEmail)).thenReturn(List.of(note1, note2));

        GetNotesRequest request = GetNotesRequest.builder()
                .withEmail(expectedEmail)
                .withOrder(NoteOrder.DEFAULT_REVERSED)
                .build();

        // WHEN
        GetNotesResult result = getNotesActivity.handleRequest(request);
        NoteModel firstNoteResult = result.getNoteList().get(0);
        NoteModel secondNoteResult = result.getNoteList().get(1);

        // THEN
        assertEquals(firstNoteResult.getDateCreated(), note1.getDateCreated(),
                "Expected oldest note to be last in list.");
        assertEquals(secondNoteResult.getDateCreated(), note2.getDateCreated(),
                "Expected newest note to be first in list.");
    }
    
    @Test
    public void handleRequest_invalidOrder_throwsInvalidAttributeValueException() {
        // GIVEN
        String email = "email@test.com";
        String invalidOrder = "random";

        when(noteDao.getAllNotes(email)).thenReturn(Collections.emptyList());

        GetNotesRequest request = GetNotesRequest.builder()
                .withEmail(email)
                .withOrder(invalidOrder)
                .build();

        // WHEN + THEN
        assertThrows(InvalidAttributeValueException.class, () -> getNotesActivity.handleRequest(request));
    }
}

