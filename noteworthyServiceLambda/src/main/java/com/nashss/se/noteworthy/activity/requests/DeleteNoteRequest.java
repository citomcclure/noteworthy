package com.nashss.se.noteworthy.activity.requests;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import java.time.LocalDateTime;

@JsonDeserialize(builder = DeleteNoteRequest.Builder.class)
public class DeleteNoteRequest {
    private LocalDateTime dateCreated;
    private String email;

    private DeleteNoteRequest(LocalDateTime dateCreated, String email) {
        this.dateCreated = dateCreated;
        this.email = email;
    }

    public LocalDateTime getDateCreated() {
        return dateCreated;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public String toString() {
        return "DeleteNoteRequest{" +
                "dateCreated=" + dateCreated +
                ", email='" + email + '\'' +
                '}';
    }

    //CHECKSTYLE:OFF:Builder
    public static Builder builder() {
        return new Builder();
    }

    @JsonPOJOBuilder
    public static class Builder {
        private LocalDateTime dateCreated;
        private String email;

        public Builder withDateCreated(LocalDateTime dateCreated) {
            this.dateCreated = dateCreated;
            return this;
        }

        public Builder withEmail(String email) {
            this.email = email;
            return this;
        }

        public DeleteNoteRequest build() {
            return new DeleteNoteRequest(dateCreated, email);
        }
    }
}

