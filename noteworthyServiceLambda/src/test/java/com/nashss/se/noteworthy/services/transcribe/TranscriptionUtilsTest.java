package com.nashss.se.noteworthy.services.transcribe;

import com.nashss.se.noteworthy.exceptions.TranscriptionException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TranscriptionUtilsTest {
    @Test
    public void removeEncodedHeaders_validByteSequenceAtStart_returnsSameByteArray() {
        // GIVEN
        byte[] validWav = {82, 73, 70, 70};

        // WHEN
        byte[] result = TranscriptionUtils.removeEncodedHeaders(validWav);

        // THEN
        assertEquals(validWav[0], result[0]);
        assertEquals(validWav[1], result[1]);
        assertEquals(validWav[2], result[2]);
        assertEquals(validWav[3], result[3]);
        assertEquals(validWav.length, result.length);
    }

    @Test
    public void removeEncodedHeaders_validByteSequenceWithHeaders_returnsByteSequence() {
        // GIVEN
        byte[] validWav = {60, 45, 82, 73, 70, 70};

        // WHEN
        byte[] result = TranscriptionUtils.removeEncodedHeaders(validWav);

        // THEN
        assertEquals(validWav[2], result[0]);
        assertEquals(validWav[3], result[1]);
        assertEquals(validWav[4], result[2]);
        assertEquals(validWav[5], result[3]);
        assertEquals(validWav.length - 2, result.length);
    }

    @Test
    public void removeEncodedHeaders_invalidByteSequence_throwsRuntimeException() throws RuntimeException {
        // GIVEN
        byte[] invalidWav = {60, 45, 81, 45, 79, 90};

        // WHEN + THEN
        assertThrows(RuntimeException.class, () -> TranscriptionUtils.removeEncodedHeaders(invalidWav));
    }

    @Test
    public void parseJsonForTranscript_validJsonString_returnsResult() {
        // GIVEN
        String expectedTranscript = "Test";
        String validTranscriptionJson = "{\"jobName\":\"transcription_job\"," +
                        "\"accountId\":\"1234567890\",\"status\":\"COMPLETED\"," +
                        "\"results\":{\"transcripts\":[{\"transcript\":\"Test\"}]}}";

        // WHEN
        String result = TranscriptionUtils.parseJsonForTranscript(validTranscriptionJson);

        // THEN
        assertEquals(expectedTranscript, result);
    }

    @Test
    public void parseJsonForTranscript_invalidJsonString_throwsException() {
        // GIVEN
        String invalidJson = "Test";
        String validTranscriptionJson = "{\"jobName\":\"transcription_job\"," +
                "\"accountId\":\"1234567890\",\"status\":\"COMPLETED\"," +
                "\"WRONG_KEY\":{\"transcripts\":[{\"transcript\":\"Test\"}]}}";

        // WHEN + THEN
        assertThrows(TranscriptionException.class, () -> TranscriptionUtils.parseJsonForTranscript(invalidJson));
    }

    @Test
    public void generateTranscriptionId_returnsTranscriptionId() {
        // GIVEN
        String ldtString = "2000-01-01T12:49:40";
        int expectedLength = TranscriptionUtils.ID_PREFIX_LENGTH + 1 + ldtString.length();

        // WHEN
        String result = TranscriptionUtils.generateTranscriptionId(ldtString);

        // THEN
        assertEquals(expectedLength, result.length());
        assertFalse(result.matches(":"));
    }

    @Test
    public void getSampleRate_invalidAudioFile_throwsTranscriptionException() {
        // GIVEN
        byte[] invalidAudio = new byte[] {0, 0, 0, 0};

        // WHEN + THEN
        assertThrows(TranscriptionException.class, () -> TranscriptionUtils.getSampleRate(invalidAudio));
    }

    @Test
    public void getSampleRate_validAudio_returnsSampleRate() throws IOException {
        // GIVEN
        byte[] audio = new byte[] {0, 0, 0, 0};
        InputStream inputStream = new ByteArrayInputStream(audio);
        int expectedSampleRate = 42_000;
        AudioFormat audioFormat = new AudioFormat((float) expectedSampleRate, 16, 1, true, false);
        AudioInputStream audioInputStream = new AudioInputStream(inputStream, audioFormat, 10);

        // WHEN
        int result;
        try (MockedStatic<AudioSystem> mockedAudioSystem = Mockito.mockStatic(AudioSystem.class)) {
            mockedAudioSystem.when(() -> AudioSystem.getAudioInputStream(ArgumentMatchers.any(InputStream.class)))
                    .thenReturn(audioInputStream);

            result = TranscriptionUtils.getSampleRate(audio);
        }

        inputStream.close();
        audioInputStream.close();

        // THEN
        assertEquals(expectedSampleRate, result);
    }

    @Test
    public void getSampleRate_sampleRateNoteSpecified_returnsSampleRate() throws IOException {
        // GIVEN
        byte[] audio = new byte[] {0, 0, 0, 0};
        InputStream inputStream = new ByteArrayInputStream(audio);
        int sampleRate = AudioSystem.NOT_SPECIFIED;
        AudioFormat audioFormat = new AudioFormat(sampleRate, 16, 1, true, false);
        AudioInputStream audioInputStream = new AudioInputStream(inputStream, audioFormat, 10);

        // WHEN
        int result;
        try (MockedStatic<AudioSystem> mockedAudioSystem = Mockito.mockStatic(AudioSystem.class)) {
            mockedAudioSystem.when(() -> AudioSystem.getAudioInputStream(ArgumentMatchers.any(InputStream.class)))
                    .thenReturn(audioInputStream);

            result = TranscriptionUtils.getSampleRate(audio);
        }

        inputStream.close();
        audioInputStream.close();

        // THEN
        assertEquals(48_000, result);
    }
}
