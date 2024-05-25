package com.nashss.se.noteworthy.activity.requests;

public class GetNotesRequest {
    private final String email;

    private GetNotesRequest(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public String toString() {
        return "GetNotesRequest{" +
                "email='" + email + '\'' +
                '}';
    }

    //CHECKSTYLE:OFF:Builder
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String email;

        public Builder withEmail(String email) {
            this.email = email;
            return this;
        }

        public GetNotesRequest build() {
            return new GetNotesRequest(email);
        }
    }
}
