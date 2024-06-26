package com.nashss.se.noteworthy.converters;

import com.nashss.se.noteworthy.models.NoteModel;
import com.nashss.se.noteworthy.services.dynamodb.models.Note;

/**
 * Converts between Data and API models.
 */
public class ModelConverter {
    /**
     * Converts a provided {@link Note} into a {@link NoteModel} representation.
     *
     * @param note the note to convert
     * @return the converted note
     */
    public static NoteModel toNoteModel(Note note) {
        return NoteModel.builder()
                .withTitle(note.getTitle())
                .withContent(note.getContent())
                .withDateCreated(note.getDateCreated())
                .withDateUpdated(note.getDateUpdated())
                .withEmail(note.getEmail())
                .build();
    }
}
