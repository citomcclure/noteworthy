package com.nashss.se.noteworthy.services.s3;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class S3ClientProviderTest {
    @Test
    public void getS3Client_nullRegion_throwsException() {
        // GIVEN + WHEN + THEN
        assertThrows(IllegalArgumentException.class,
                () -> S3ClientProvider.getS3Client(null));
    }
}
