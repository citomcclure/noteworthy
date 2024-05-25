package com.nashss.se.noteworthy.activity.results;

import com.nashss.se.noteworthy.models.NoteModel;

import java.util.List;

public class GetNotesResult {
    private final List<NoteModel> noteList;

    private GetNotesResult(List<NoteModel> noteList) {
        this.noteList = noteList;
    }

    public List<NoteModel> getNoteList() {
        return noteList;
    }

    @Override
    public String toString() {
        return "GetNotesResult{" +
                "noteList=" + noteList +
                '}';
    }

    //CHECKSTYLE:OFF:Builder
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private List<NoteModel> noteList;

        public Builder withNoteList(List<NoteModel> noteList) {
            this.noteList = noteList;
            return this;
        }

        public GetNotesResult build() {
            return new GetNotesResult(noteList);
        }
    }
}
