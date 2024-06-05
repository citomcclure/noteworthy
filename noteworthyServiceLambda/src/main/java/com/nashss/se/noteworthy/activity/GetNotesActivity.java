package com.nashss.se.noteworthy.activity;

import com.nashss.se.noteworthy.activity.requests.GetNotesRequest;
import com.nashss.se.noteworthy.activity.results.GetNotesResult;
import com.nashss.se.noteworthy.converters.ModelConverter;
import com.nashss.se.noteworthy.dynamodb.NoteDao;

import com.nashss.se.noteworthy.dynamodb.models.Note;
import com.nashss.se.noteworthy.exceptions.InvalidAttributeValueException;
import com.nashss.se.noteworthy.models.NoteModel;
import com.nashss.se.noteworthy.models.NoteOrder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.inject.Inject;


/**
 * Implementation of the GetNotesActivity for the NoteworthyService's GetNotes API.
 * This API allows the user to get all of their saved notes.
 */
public class GetNotesActivity {
    private final Logger log = LogManager.getLogger();
    private final NoteDao noteDao;
    private String noteOrder;

    /**
     * Instantiates a new GetNotesActivity object.
     * @param noteDao NoteDao used to access the notes table.
     */
    @Inject
    public GetNotesActivity(NoteDao noteDao) {
        this.noteDao = noteDao;
    }

    /**
     * This method handles the incoming request by retrieving the notes from the database.
     * Retrieves all notes made by the user with that email.
     * @param getNotesRequest request object containing the email
     * @return GetNotesResult result object containing the API defined by {@link NoteModel}
     */
    public GetNotesResult handleRequest(final GetNotesRequest getNotesRequest) {
        log.info("Received GetNotesRequest {}", getNotesRequest);
        String email = getNotesRequest.getEmail();
        List<Note> notes = noteDao.getAllNotes(email);

        noteOrder = getNotesRequest.getOrder();
        List<Note> orderedNotes = orderNotes(notes);

        List<NoteModel> noteModels = new ArrayList<>();
        for (Note note : orderedNotes) {
            NoteModel noteModel = ModelConverter.toNoteModel(note);
            noteModels.add(noteModel);
        }

        return GetNotesResult.builder()
                .withNoteList(noteModels)
                .build();
    }

    private List<Note> orderNotes(List<Note> notes) {
        if (noteOrder == null) {
            noteOrder = NoteOrder.DEFAULT;
        } else if (!Arrays.asList(NoteOrder.values()).contains(noteOrder)) {
            throw new InvalidAttributeValueException(String.format("Unrecognized sort order: '%s'", noteOrder));
        }

        if (noteOrder.equals(NoteOrder.DEFAULT_REVERSED)) {
            Collections.reverse(notes);
        }

        return notes;
    }
}
