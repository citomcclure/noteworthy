package com.nashss.se.noteworthy.converters;

import com.nashss.se.noteworthy.models.NoteModel;
import com.nashss.se.noteworthy.services.dynamodb.models.Note;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ModelConverterTest {
    @Test
    public void toNoteModel_convertsNote() {
        // GIVEN
        Note note = new Note();
        note.setTitle("Title");
        note.setContent("Content");
        LocalDateTime ldt = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        note.setDateCreated(ldt);
        note.setDateUpdated(ldt);
        note.setEmail("email@test.com");

        // WHEN
        NoteModel result = ModelConverter.toNoteModel(note);

        // THEN
        assertEquals(note.getTitle(), result.getTitle());
        assertEquals(note.getContent(), result.getContent());
        assertEquals(note.getDateCreated(), result.getDateCreated());
        assertEquals(note.getDateUpdated(), result.getDateUpdated());
        assertEquals(note.getEmail(), result.getEmail());
    }
}
