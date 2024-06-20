package com.nashss.se.noteworthy.converters;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class NoteDateTimeConverterTest {
    private NoteDateTimeConverter converter = new NoteDateTimeConverter();

    @Test
    public void convert_validLocalDateTimeObject_toString() {
        // GIVEN
        LocalDateTime ldt = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

        // WHEN
        String result = converter.convert(ldt);

        // THEN
        assertEquals(result, ldt.toString());
    }

    @Test
    public void convert_nullLocalDateTimeObject_returnsNull() {
        // GIVEN
        LocalDateTime ldt = null;

        // WHEN
        String result = converter.convert(ldt);

        // THEN
        assertNull(result);
    }

    @Test
    public void unconvert_validString_toLocalDateTimeObject() {
        // GIVEN
        String ldtString = "2000-01-01T12:49:40";

        // WHEN
        LocalDateTime result = converter.unconvert(ldtString);

        // THEN
        assertEquals(LocalDateTime.parse(ldtString), result);
    }

    @Test
    public void unconvert_nullString_returnsNull() {
        // GIVEN
        String ldt = null;

        // WHEN
        LocalDateTime result = converter.unconvert(ldt);

        // THEN
        assertNull(result);
    }
}
