package com.nashss.se.noteworthy.activity.results;

import com.nashss.se.noteworthy.models.NoteModel;

public class UpdateNoteResult {
    private NoteModel note;

    private UpdateNoteResult(NoteModel note) {
        this.note = note;
    }

    public NoteModel getNote() {
        return note;
    }

    @Override
    public String toString() {
        return "UpdateNoteResult{" +
                "note=" + note +
                '}';
    }

    //CHECKSTYLE:OFF:Builder
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private NoteModel note;

        public Builder withNote(NoteModel note) {
            this.note = note;
            return this;
        }

        public UpdateNoteResult build() {
            return new UpdateNoteResult(note);
        }
    }
}

