package com.nashss.se.noteworthy.activity.results;

import com.nashss.se.noteworthy.models.NoteModel;

import java.util.List;

public class GetNotesResult {
    private final List<NoteModel> noteModels;

    private GetNotesResult(List<NoteModel> noteModels) {
        this.noteModels = noteModels;
    }

    public List<NoteModel> getNoteModels() {
        return noteModels;
    }

    @Override
    public String toString() {
        return "GetNotesResult{" +
                "noteModel=" + noteModels +
                '}';
    }

    //CHECKSTYLE:OFF:Builder
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private List<NoteModel> noteModels;

        public Builder withNoteModels(List<NoteModel> noteModels) {
            this.noteModels = noteModels;
            return this;
        }

        public GetNotesResult build() {
            return new GetNotesResult(noteModels);
        }
    }
}
