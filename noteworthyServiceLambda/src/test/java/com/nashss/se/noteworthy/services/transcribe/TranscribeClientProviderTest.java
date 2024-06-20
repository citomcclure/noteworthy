package com.nashss.se.noteworthy.services.transcribe;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class TranscribeClientProviderTest {
    @Test
    public void getTranscribeClient_nullRegion_throwsException() {
        // GIVEN + WHEN + THEN
        assertThrows(IllegalArgumentException.class,
                () -> TranscribeClientProvider.getTranscribeClient(null));
    }
}
