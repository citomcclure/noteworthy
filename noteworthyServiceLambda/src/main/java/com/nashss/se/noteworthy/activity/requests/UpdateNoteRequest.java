package com.nashss.se.noteworthy.activity.requests;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

@JsonDeserialize(builder = UpdateNoteRequest.Builder.class)
public class UpdateNoteRequest {
    private String noteId;
    private String title;
    private String content;
    private String email;

    private UpdateNoteRequest(String noteId, String title, String content, String email) {
        this.noteId = noteId;
        this.title = title;
        this.content = content;
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

    public String getEmail() {
        return email;
    }

    @Override
    public String toString() {
        return "UpdateNoteRequest{" +
                ", noteId='" + noteId + '\'' +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
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

        public Builder withEmail(String email) {
            this.email = email;
            return this;
        }

        public UpdateNoteRequest build() {
            return new UpdateNoteRequest(noteId, title, content, email);
        }
    }
}
