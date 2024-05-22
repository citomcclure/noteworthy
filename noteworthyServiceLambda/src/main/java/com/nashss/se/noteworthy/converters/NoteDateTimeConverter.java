package com.nashss.se.noteworthy.converters;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter;

import org.joda.time.DateTime;

public class NoteDateTimeConverter implements DynamoDBTypeConverter<String, DateTime> {
    @Override
    public String convert(DateTime datetime) {
        if (datetime == null) {
            return null;
        }
        return datetime.toString();
    }

    @Override
    public DateTime unconvert(String stringValue) {
        if (stringValue == null || stringValue.isEmpty()) {
            return null;
        }
        return DateTime.parse(stringValue);
    }
}
