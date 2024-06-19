package com.nashss.se.noteworthy.activity;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.transcribe.AmazonTranscribe;
import com.nashss.se.noteworthy.activity.requests.TranscribeAudioRequest;
import com.nashss.se.noteworthy.exceptions.TranscriptionException;
import com.nashss.se.noteworthy.services.dynamodb.NoteDao;
import com.nashss.se.noteworthy.services.dynamodb.TranscriptionDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class TranscribeAudioActivityTest {
    @Mock
    private NoteDao noteDao;

    @Mock
    private TranscriptionDao transcriptionDao;

    @Mock
    private AmazonS3 s3Client;

    @Mock
    private AmazonTranscribe transcribeClient;

    private TranscribeAudioActivity transcribeAudioActivity;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        transcribeAudioActivity = new TranscribeAudioActivity(noteDao, transcriptionDao, s3Client, transcribeClient);
    }

    @Test
    public void handleRequest_nullAudio_throwTranscriptionException() throws TranscriptionException {
        // GIVEN
        TranscribeAudioRequest request = TranscribeAudioRequest.builder()
                .withEmail("email")
                .build();

        // THEN
        assertThrows(TranscriptionException.class, () -> transcribeAudioActivity.handleRequest(request));
    }
}
