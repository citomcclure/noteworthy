package com.nashss.se.noteworthy.activity.results;

import com.nashss.se.noteworthy.models.NoteModel;

import java.util.List;

public class GetNotesResult {
    private final List<NoteModel> notes;

    private GetNotesResult(List<NoteModel> notes) {
        this.notes = notes;
    }

    public List<NoteModel> getNoteModels() {
        return notes;
    }

    @Override
    public String toString() {
        return "GetNotesResult{" +
                "noteModel=" + notes +
                '}';
    }

    //CHECKSTYLE:OFF:Builder
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private List<NoteModel> notes;

        public Builder withNoteModels(List<NoteModel> notes) {
            this.notes = notes;
            return this;
        }

        public GetNotesResult build() {
            return new GetNotesResult(notes);
        }
    }
}
