package com.nashss.se.noteworthy.models;

import java.time.LocalDateTime;
import java.util.Objects;

public class NoteModel {
    private final String noteId;
    private final String title;
    private final String content;
    private final LocalDateTime dateUpdated;
    private final String email;

    private NoteModel(String noteId, String title, String content, LocalDateTime dateUpdated, String email) {
        this.noteId = noteId;
        this.title = title;
        this.content = content;
        this.dateUpdated = dateUpdated;
        this.email = email;
    }

    public String getNoteId() {
        return noteId;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public LocalDateTime getDateUpdated() {
        return dateUpdated;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        NoteModel noteModel = (NoteModel) o;
        return Objects.equals(noteId, noteModel.noteId) &&
                Objects.equals(title, noteModel.title) &&
                Objects.equals(content, noteModel.content) &&
                Objects.equals(dateUpdated, noteModel.dateUpdated) &&
                Objects.equals(email, noteModel.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(noteId, title, content, dateUpdated, email);
    }

    //CHECKSTYLE:OFF:Builder
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String noteId;
        private String title;
        private String content;
        private LocalDateTime dateUpdated;
        private String email;

        public Builder withNoteId(String noteId) {
            this.noteId = noteId;
            return this;
        }

        public Builder withTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder withContent(String content) {
            this.content = content;
            return this;
        }

        public Builder withDateUpdated(LocalDateTime dateUpdated) {
            this.dateUpdated = dateUpdated;
            return this;
        }

        public Builder withEmail(String email) {
            this.email = email;
            return this;
        }

        public NoteModel build() {
            return new NoteModel(noteId, title, content, dateUpdated, email);
        }
    }
}
