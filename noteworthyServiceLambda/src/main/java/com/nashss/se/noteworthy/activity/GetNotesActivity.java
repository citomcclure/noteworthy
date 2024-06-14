package com.nashss.se.noteworthy.activity;

import com.nashss.se.noteworthy.activity.requests.GetNotesRequest;
import com.nashss.se.noteworthy.activity.results.GetNotesResult;
import com.nashss.se.noteworthy.converters.ModelConverter;
import com.nashss.se.noteworthy.exceptions.InvalidAttributeValueException;
import com.nashss.se.noteworthy.models.NoteModel;
import com.nashss.se.noteworthy.models.NoteOrder;
import com.nashss.se.noteworthy.services.dynamodb.NoteDao;
import com.nashss.se.noteworthy.services.dynamodb.models.Note;

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

        List<NoteModel> noteModels = new ArrayList<>();
        for (Note note : notes) {
            NoteModel noteModel = ModelConverter.toNoteModel(note);
            noteModels.add(noteModel);
        }

        List<NoteModel> orderedNotes = orderNotes(noteModels, getNotesRequest.getOrder());

        return GetNotesResult.builder()
                .withNoteList(orderedNotes)
                .build();
    }

    /**
     * Helper function to determine order of notes using the request's query parameter.
     * @param noteModels the current list of notes retrieved from noteDao. Will be in
     *              order of newest to oldest by creation date already.
     * @return the updated order of the notes.
     */
    private List<NoteModel> orderNotes(List<NoteModel> noteModels, String noteOrder) {
        // TODO: make case statement
        if (noteOrder == null || noteOrder.equals(NoteOrder.DEFAULT)) {
            return noteModels;
        } else if (!Arrays.asList(NoteOrder.values()).contains(noteOrder)) {
            throw new InvalidAttributeValueException(String.format("Unrecognized sort order: '%s'", noteOrder));
        }

        if (noteOrder.equals(NoteOrder.DEFAULT_REVERSED)) {
            Collections.reverse(noteModels);
        }

        return noteModels;
    }
}
