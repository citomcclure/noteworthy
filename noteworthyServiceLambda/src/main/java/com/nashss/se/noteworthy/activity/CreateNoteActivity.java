package com.nashss.se.noteworthy.activity;

import com.nashss.se.noteworthy.activity.requests.CreateNoteRequest;
import com.nashss.se.noteworthy.activity.results.CreateNoteResult;
import com.nashss.se.noteworthy.converters.ModelConverter;
import com.nashss.se.noteworthy.dynamodb.NoteDao;
import com.nashss.se.noteworthy.dynamodb.models.Note;
import com.nashss.se.noteworthy.models.NoteModel;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDateTime;
import javax.inject.Inject;

/**
 * Implementation of the CreateNoteActivity for the NoteworthyService's CreateNote API.
 * This API allows the user to save an existing note.
 */
public class CreateNoteActivity {
    private final Logger log = LogManager.getLogger();
    private NoteDao noteDao;

    /**
     * Instantiates a new CreateNoteActivity object.
     * @param noteDao NoteDao used to access the notes table.
     */
    @Inject
    public CreateNoteActivity(NoteDao noteDao) {
        this.noteDao = noteDao;
    }

    /**
     * This method handles the incoming request by persisting a new note
     * with the provided note title and content.
     * Retrieves all notes made by the user with that email.
     * @param createNoteRequest request object containing the note title, content,
     *                          and user email
     * @return result object containing the API defined by {@link NoteModel}
     */
    public CreateNoteResult handleRequest(CreateNoteRequest createNoteRequest) {
        log.info("Received CreateNoteRequest {}", createNoteRequest);

        //TODO: check if illegal characters from project template affect endpoint

        Note note = new Note();
        note.setNoteId(RandomStringUtils.randomAlphanumeric(5));
        note.setTitle(createNoteRequest.getTitle());
        note.setContent(createNoteRequest.getContent());
        note.setDateUpdated(LocalDateTime.now());
        note.setEmail(createNoteRequest.getEmail());

        noteDao.saveNote(note);

        NoteModel noteModel = ModelConverter.toNoteModel(note);
        return CreateNoteResult.builder()
                .withNote(noteModel)
                .build();
    }
}
