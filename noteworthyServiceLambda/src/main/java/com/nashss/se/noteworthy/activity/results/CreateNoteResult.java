package com.nashss.se.noteworthy.activity.results;

import com.nashss.se.noteworthy.models.NoteModel;

public class CreateNoteResult {
    private NoteModel note;

    private CreateNoteResult(NoteModel note) {
        this.note = note;
    }

    public NoteModel getNote() {
        return note;
    }

    @Override
    public String toString() {
        return "CreateNoteResult{" +
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

        public CreateNoteResult build() {
            return new CreateNoteResult(note);
        }
    }
}
