package com.nashss.se.noteworthy.activity;

import com.nashss.se.noteworthy.activity.requests.UpdateNoteRequest;
import com.nashss.se.noteworthy.activity.results.UpdateNoteResult;
import com.nashss.se.noteworthy.converters.ModelConverter;
import com.nashss.se.noteworthy.dynamodb.NoteDao;
import com.nashss.se.noteworthy.dynamodb.models.Note;
import com.nashss.se.noteworthy.models.NoteModel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDateTime;
import javax.inject.Inject;

/**
 * Implementation of the UpdateNoteActivity for the NoteworthyService's UpdateNote API.
 * This API allows the user to update an existing note.
 */
public class UpdateNoteActivity {
    private final Logger log = LogManager.getLogger();
    private NoteDao noteDao;

    /**
     * Instantiates a new UpdateNoteActivity object.
     * @param noteDao NoteDao used to access the notes table.
     */
    @Inject
    public UpdateNoteActivity(NoteDao noteDao) {
        this.noteDao = noteDao;
    }

    /**
     * This method handles the incoming request by creating a new note
     * using the same noteId and updated note title and content.
     * User will see the newer version of their note as well as
     * the older one.
     * @param updateNoteRequest request object containing the note ID, note title,
     *                          content, and user email
     * @return result object containing the API defined by {@link NoteModel}
     */
    public UpdateNoteResult handleRequest(UpdateNoteRequest updateNoteRequest) {
        log.info("Received UpdateNoteRequest {}", updateNoteRequest);

        //TODO: check if illegal characters from project template affect endpoint

        Note note = new Note();
        note.setTitle(updateNoteRequest.getTitle());
        note.setContent(updateNoteRequest.getContent());
        note.setDateCreated(updateNoteRequest.getDateCreated());
        note.setDateUpdated(LocalDateTime.now());
        note.setEmail(updateNoteRequest.getEmail());

        noteDao.saveNote(note);

        NoteModel noteModel = ModelConverter.toNoteModel(note);
        return UpdateNoteResult.builder()
                .withNote(noteModel)
                .build();
    }
}

