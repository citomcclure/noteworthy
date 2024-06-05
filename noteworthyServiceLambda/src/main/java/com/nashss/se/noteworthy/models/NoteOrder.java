package com.nashss.se.noteworthy.models;

public class NoteOrder {

    public static final String DEFAULT = "date_created_newest_to_oldest";
    public static final String DEFAULT_REVERSED = "date_created_oldest_to_newest";
    public static final String LAST_UPDATED = "date_created_newest_to_oldest";
    public static final String LAST_UPDATED_REVERSED = "date_last_updated_oldest_to_newest";


    private NoteOrder() {
    }

    /**
     * Return an array of all the valid SongOrder values.
     * @return An array of SongOrder values.
     */
    public static String[] values() {
        return new String[]{DEFAULT, DEFAULT_REVERSED, LAST_UPDATED, LAST_UPDATED_REVERSED};
    }
}
