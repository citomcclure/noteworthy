package com.nashss.se.noteworthy.services.dynamodb.models;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

import java.util.Objects;

@DynamoDBTable(tableName = "transcriptions")
public class Transcription {
    private String transcriptionId;
    private String json;

    @DynamoDBHashKey(attributeName = "transcriptionId")
    public String getTranscriptionId() {
        return transcriptionId;
    }

    public void setTranscriptionId(String transcriptionId) {
        this.transcriptionId = transcriptionId;
    }

    @DynamoDBAttribute(attributeName = "json")
    public String getJson() {
        return json;
    }

    public void setJson(String json) {
        this.json = json;
    }

    @Override
    public String toString() {
        return "Transcription{" +
                "transcriptionId='" + transcriptionId + '\'' +
                ", json='" + json + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Transcription that = (Transcription) o;
        return Objects.equals(transcriptionId, that.transcriptionId) && Objects.equals(json, that.json);
    }

    @Override
    public int hashCode() {
        return Objects.hash(transcriptionId, json);
    }
}
