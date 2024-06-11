package com.nashss.se.noteworthy.activity.results;

import com.nashss.se.noteworthy.models.NoteModel;

public class TranscribeAudioResult {
    private NoteModel note;

    private TranscribeAudioResult(NoteModel note) {
        this.note = note;
    }

    public NoteModel getNote() {
        return note;
    }

    @Override
    public String toString() {
        return "TranscribeAudioResult{" +
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

        public TranscribeAudioResult build() {
            return new TranscribeAudioResult(note);
        }
    }
}
