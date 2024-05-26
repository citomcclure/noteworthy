package com.nashss.se.noteworthy.activity.requests;

import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

public class CreateNoteRequest {
    private String title;
    private String content;
    private String email;

    private CreateNoteRequest(String title, String content, String email) {
        this.title = title;
        this.content = content;
        this.email = email;
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
        return "CreateNoteRequest{" +
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
        private String title;
        private String content;
        private String email;

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

        public CreateNoteRequest build() {
            return new CreateNoteRequest(title, content, email);
        }
    }
}
