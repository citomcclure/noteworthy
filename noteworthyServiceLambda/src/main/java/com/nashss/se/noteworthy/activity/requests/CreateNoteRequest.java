package com.nashss.se.noteworthy.activity.requests;

import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import java.time.LocalDateTime;

public class CreateNoteRequest {
    private String noteId;
    private String title;
    private String content;
    private LocalDateTime dateUpdated;
    private String email;

    private CreateNoteRequest(String noteId, String title, String content, LocalDateTime dateUpdated, String email) {
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
    public String toString() {
        return "CreateNoteRequest{" +
                "noteId='" + noteId + '\'' +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", dateUpdated=" + dateUpdated +
                ", email='" + email + '\'' +
                '}';
    }

    //CHECKSTYLE:OFF:Builder
    public static Builder builder() {
        return new Builder();
    }

    @JsonPOJOBuilder
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

        public CreateNoteRequest build() {
            return new CreateNoteRequest(noteId, title, content, dateUpdated, email);
        }
    }
}
