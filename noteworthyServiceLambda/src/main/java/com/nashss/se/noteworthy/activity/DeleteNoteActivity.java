package com.nashss.se.noteworthy.activity;

import com.nashss.se.noteworthy.activity.requests.DeleteNoteRequest;
import com.nashss.se.noteworthy.activity.results.DeleteNoteResult;
import com.nashss.se.noteworthy.converters.ModelConverter;
import com.nashss.se.noteworthy.services.dynamodb.NoteDao;
import com.nashss.se.noteworthy.services.dynamodb.models.Note;
import com.nashss.se.noteworthy.models.NoteModel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Inject;

/**
 * Implementation of the DeleteNoteActivity for the NoteworthyService's DeleteNote API.
 * This API allows the user to hard delete a note.
 */
public class DeleteNoteActivity {
    private final Logger log = LogManager.getLogger();
    private NoteDao noteDao;

    /**
     * Instantiates a new DeleteNoteActivity object.
     * @param noteDao NoteDao used to access the notes table.
     */
    @Inject
    public DeleteNoteActivity(NoteDao noteDao) {
        this.noteDao = noteDao;
    }

    /**
     * This method handles the incoming request by performing a hard delete on
     * a note using the user's email and creation date of the note.
     * @param deleteNoteRequest request object containing the email and dateCreated.
     * @return result object containing the API defined by {@link NoteModel}
     */
    public DeleteNoteResult handleRequest(DeleteNoteRequest deleteNoteRequest) {
        log.info("Received DeleteNoteRequest {}", deleteNoteRequest);

        Note note = new Note();
        note.setDateCreated(deleteNoteRequest.getDateCreated());
        note.setEmail(deleteNoteRequest.getEmail());

        noteDao.deleteNote(note);

        NoteModel noteModel = ModelConverter.toNoteModel(note);
        return DeleteNoteResult.builder()
                .withNote(noteModel)
                .build();
    }
}

