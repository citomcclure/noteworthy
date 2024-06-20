package com.nashss.se.noteworthy.models;

public class NoteOrder {

    // Default is newest to oldest using dateCreated
    public static final String DEFAULT = "default";
    public static final String DEFAULT_REVERSED = "default_reversed";

    private NoteOrder() {
    }

    /**
     * Return an array of all the valid SongOrder values.
     * @return An array of SongOrder values.
     */
    public static String[] values() {
        return new String[]{DEFAULT, DEFAULT_REVERSED};
    }
}
