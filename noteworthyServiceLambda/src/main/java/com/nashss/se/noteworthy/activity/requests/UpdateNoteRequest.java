package com.nashss.se.noteworthy.activity.requests;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import java.time.LocalDateTime;

@JsonDeserialize(builder = UpdateNoteRequest.Builder.class)
public class UpdateNoteRequest {
    private String title;
    private String content;
    private LocalDateTime dateCreated;
    private String email;

    private UpdateNoteRequest(String title, String content, LocalDateTime dateCreated, String email) {
        this.title = title;
        this.content = content;
        this.dateCreated = dateCreated;
        this.email = email;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public LocalDateTime getDateCreated() {
        return dateCreated;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public String toString() {
        return "UpdateNoteRequest{" +
                "title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", dateCreated=" + dateCreated +
                ", email='" + email + '\'' +
                '}';
    }

    //CHECKSTYLE:OFF:Builder
    public static Builder builder() {
        return new Builder();
    }

    @JsonPOJOBuilder
    public static class Builder {
        private String title;
        private String content;
        private LocalDateTime dateCreated;
        private String email;

        public Builder withTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder withContent(String content) {
            this.content = content;
            return this;
        }

        public Builder withDateCreated(LocalDateTime dateCreated) {
            this.dateCreated = dateCreated;
            return this;
        }

        public Builder withEmail(String email) {
            this.email = email;
            return this;
        }

        public UpdateNoteRequest build() {
            return new UpdateNoteRequest(title, content, dateCreated, email);
        }
    }
}
