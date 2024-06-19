package com.nashss.se.noteworthy.activity;

import com.amazonaws.services.transcribe.AmazonTranscribe;
import com.nashss.se.noteworthy.activity.requests.TranscribeAudioRequest;
import com.nashss.se.noteworthy.exceptions.TranscriptionException;
import com.nashss.se.noteworthy.services.dynamodb.NoteDao;
import com.nashss.se.noteworthy.services.dynamodb.TranscriptionDao;
import com.nashss.se.noteworthy.services.s3.S3Wrapper;
import com.nashss.se.noteworthy.services.transcribe.TranscribeWrapper;
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
    private S3Wrapper s3Wrapper;

    @Mock
    private TranscribeWrapper transcribeWrapper;

    private TranscribeAudioActivity transcribeAudioActivity;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        transcribeAudioActivity = new TranscribeAudioActivity(noteDao, transcriptionDao, s3Wrapper, transcribeWrapper);
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
