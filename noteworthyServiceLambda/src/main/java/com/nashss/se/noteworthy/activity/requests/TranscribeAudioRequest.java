package com.nashss.se.noteworthy.activity.requests;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import java.util.Arrays;

@JsonDeserialize(builder = TranscribeAudioRequest.Builder.class)
public class TranscribeAudioRequest {
    private byte[] audio;
    private String email;

    private TranscribeAudioRequest(byte[] audio, String email) {
        this.audio = audio;
        this.email = email;
    }

    public byte[] getAudio() {
        return audio;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public String toString() {
        return "TranscribeAudioRequest{" +
                "audio=" + Arrays.toString(audio) +
                ", email='" + email + '\'' +
                '}';
    }

    //CHECKSTYLE:OFF:Builder
    public static Builder builder() {
        return new Builder();
    }

    @JsonPOJOBuilder
    public static class Builder {
        private byte[] audio;
        private String email;

        public Builder withAudio(byte[] audio) {
            this.audio = audio;
            return this;
        }

        public Builder withEmail(String email) {
            this.email = email;
            return this;
        }

        public TranscribeAudioRequest build() {
            return new TranscribeAudioRequest(audio, email);
        }
    }
}
