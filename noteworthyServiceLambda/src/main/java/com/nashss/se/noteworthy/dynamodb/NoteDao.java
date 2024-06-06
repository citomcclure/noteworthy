package com.nashss.se.noteworthy.dynamodb;

import com.nashss.se.noteworthy.dynamodb.models.Note;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;

import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Accesses data for a note using {@link Note} to represent the model in DynamoDB.
 */
@Singleton
public class NoteDao {
    private final DynamoDBMapper dynamoDBMapper;

    /**
     * Instantiates a NoteDao object.
     *
     * @param dynamoDBMapper the {@link DynamoDBMapper} used to interact with the notes table
     */
    @Inject
    public NoteDao(DynamoDBMapper dynamoDBMapper) {
        this.dynamoDBMapper = dynamoDBMapper;
    }

    /**
     * Saves (creates or updates) the note.
     *
     * @param note The note to save.
     * @return the Note object that was saved.
     */
    public Note saveNote(Note note) {
        this.dynamoDBMapper.save(note);
        return note;
    }

    /**
     * Retrieves all notes made by a specific user, determined by their email.
     *
     * @param email the user's email.
     * @return a list of notes made by that user in no specified order.
     */
    public List<Note> getAllNotes(String email) {
        Note note = new Note();
        note.setEmail(email);

        DynamoDBQueryExpression<Note> queryExpression = new DynamoDBQueryExpression<Note>()
                .withHashKeyValues(note)
                .withScanIndexForward(false);

        return this.dynamoDBMapper.query(Note.class, queryExpression);
    }

    /**
     * Hard deletes a note using the set partition and sort keys.
     *
     * @param note the note to be deleted, containing the email and dateCreated.
     * @return the note that was used for deletion.
     */
    public Note deleteNote(Note note) {
        this.dynamoDBMapper.delete(note);
        return note;
    }
}
