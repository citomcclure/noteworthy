package com.nashss.se.noteworthy.activity;

import com.nashss.se.noteworthy.activity.requests.CreateNoteRequest;
import com.nashss.se.noteworthy.activity.results.CreateNoteResult;
import com.nashss.se.noteworthy.dynamodb.NoteDao;
import com.nashss.se.noteworthy.dynamodb.models.Note;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Inject;

/**
 * Implementation of the CreateNoteActivity for the NoteworthyService's CreateNote API.
 * This API allows the user to save or update an existing note.
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

    public CreateNoteResult handleRequest(CreateNoteRequest createNoteRequest) {
        return null;
    }
}
