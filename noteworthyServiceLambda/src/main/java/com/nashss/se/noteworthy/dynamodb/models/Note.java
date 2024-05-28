package com.nashss.se.noteworthy.dynamodb.models;

import com.nashss.se.noteworthy.converters.NoteDateTimeConverter;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverted;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a record in the notes table.
 */
@DynamoDBTable(tableName = "notes")
public class Note {
    private String title;
    private String content;
    private LocalDateTime dateCreated;
    private LocalDateTime dateUpdated;
    private String email;

    @DynamoDBAttribute(attributeName = "title")
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @DynamoDBAttribute(attributeName = "content")
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @DynamoDBTypeConverted(converter = NoteDateTimeConverter.class)
    @DynamoDBRangeKey(attributeName = "dateCreated")
    public LocalDateTime getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(LocalDateTime dateCreated) {
        this.dateCreated = dateCreated;
    }

    @DynamoDBTypeConverted(converter = NoteDateTimeConverter.class)
    @DynamoDBAttribute(attributeName = "dateUpdated")
    public LocalDateTime getDateUpdated() {
        return dateUpdated;
    }

    public void setDateUpdated(LocalDateTime dateUpdated) {
        this.dateUpdated = dateUpdated;
    }

    @DynamoDBHashKey(attributeName = "email")
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Note note = (Note) o;
        return Objects.equals(title, note.title) &&
                Objects.equals(content, note.content) &&
                Objects.equals(dateCreated, note.dateCreated) &&
                Objects.equals(dateUpdated, note.dateUpdated) &&
                Objects.equals(email, note.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, content, dateCreated, dateUpdated, email);
    }

    @Override
    public String toString() {
        return "Note{" +
                "title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", dateCreated=" + dateCreated +
                ", dateUpdated=" + dateUpdated +
                ", email='" + email + '\'' +
                '}';
    }
}
