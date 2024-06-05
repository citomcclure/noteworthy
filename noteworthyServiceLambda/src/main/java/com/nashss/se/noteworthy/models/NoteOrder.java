package com.nashss.se.noteworthy.models;

public class NoteOrder {

    public static final String DEFAULT = "DEFAULT";
    public static final String DEFAULT_REVERSED = "DEFAULT_REVERSED";
    public static final String LAST_UPDATED = "LAST_UPDATED";
    public static final String LAST_UPDATED_REVERSED = "LAST_UPDATED_REVERSED";

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
