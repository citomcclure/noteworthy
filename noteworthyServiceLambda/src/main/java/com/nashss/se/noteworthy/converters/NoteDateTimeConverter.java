package com.nashss.se.noteworthy.converters;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter;

import java.time.LocalDateTime;

public class NoteDateTimeConverter implements DynamoDBTypeConverter<String, LocalDateTime> {
    @Override
    public String convert(LocalDateTime localdatetime) {
        if (localdatetime == null) {
            return null;
        }
        return localdatetime.toString();
    }

    @Override
    public LocalDateTime unconvert(String stringValue) {
        if (stringValue == null || stringValue.isEmpty()) {
            return null;
        }
        return LocalDateTime.parse(stringValue);
    }
}
