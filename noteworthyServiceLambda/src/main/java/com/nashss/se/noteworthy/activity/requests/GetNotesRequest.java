package com.nashss.se.noteworthy.activity.requests;

public class GetNotesRequest {
    private final String email;
    private final String order;

    private GetNotesRequest(String email, String order) {
        this.email = email;
        this.order = order;
    }

    public String getEmail() {
        return email;
    }

    public String getOrder() {
        return order;
    }

    @Override
    public String toString() {
        return "GetNotesRequest{" +
                "email='" + email + '\'' +
                ", order='" + order + '\'' +
                '}';
    }

    //CHECKSTYLE:OFF:Builder
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String email;
        private String order;

        public Builder withEmail(String email) {
            this.email = email;
            return this;
        }

        public Builder withOrder(String order) {
            this.order = order;
            return this;
        }

        public GetNotesRequest build() {
            return new GetNotesRequest(email, order);
        }
    }
}
