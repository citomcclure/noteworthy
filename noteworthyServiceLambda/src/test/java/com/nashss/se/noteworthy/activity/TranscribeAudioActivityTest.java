package com.nashss.se.noteworthy.activity;

import com.amazonaws.services.transcribe.AmazonTranscribe;
import com.nashss.se.noteworthy.activity.requests.TranscribeAudioRequest;
import com.nashss.se.noteworthy.activity.results.TranscribeAudioResult;
import com.nashss.se.noteworthy.exceptions.TranscriptionException;
import com.nashss.se.noteworthy.models.NoteModel;
import com.nashss.se.noteworthy.services.dynamodb.NoteDao;
import com.nashss.se.noteworthy.services.dynamodb.TranscriptionDao;
import com.nashss.se.noteworthy.services.dynamodb.models.Note;
import com.nashss.se.noteworthy.services.dynamodb.models.Transcription;
import com.nashss.se.noteworthy.services.s3.S3Wrapper;
import com.nashss.se.noteworthy.services.transcribe.TranscribeWrapper;
import com.nashss.se.noteworthy.services.transcribe.TranscriptionUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import javax.sound.sampled.AudioSystem;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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

    @Test
    public void handleRequest_validAudio_returnsResult() {
        // GIVEN
        String email = "email@test.com";
        byte[] audio = new byte[] {1, 1, 1, 1};

        TranscribeAudioRequest request = TranscribeAudioRequest.builder()
                .withEmail(email)
                .withAudio(audio)
                .build();

        ArgumentCaptor<Transcription> captorTranscription = ArgumentCaptor.forClass(Transcription.class);
        ArgumentCaptor<Note> captorNote = ArgumentCaptor.forClass(Note.class);

        doNothing().when(s3Wrapper).putAudioInS3(any(String.class), eq(audio));

        String url = "url";
        when(s3Wrapper.getAudioLocation(anyString())).thenReturn(url);

        doNothing().when(transcribeWrapper).startTranscriptionJob(anyString(), eq(url), anyInt());

        doNothing().when(transcribeWrapper).pollTranscriptionJob(anyString());

        String jsonStr = "json";
        when(s3Wrapper.getTranscriptionJobResult(anyString())).thenReturn(jsonStr);

        String expectedTranscript = "transcript";
        String expectedTranscriptionId = "ABC123";
        TranscribeAudioResult result;
        try (MockedStatic<TranscriptionUtils> mockedTranscriptionUtils = Mockito.mockStatic(TranscriptionUtils.class)) {
            mockedTranscriptionUtils.when(() -> TranscriptionUtils.generateTranscriptionId(anyString()))
                    .thenReturn(expectedTranscriptionId);
            mockedTranscriptionUtils.when(() -> TranscriptionUtils.getSampleRate(audio))
                    .thenReturn(48_000);
            mockedTranscriptionUtils.when(() -> TranscriptionUtils.parseJsonForTranscript(jsonStr))
                    .thenReturn(expectedTranscript);

            TranscriptionUtils.getSampleRate(audio);
            TranscriptionUtils.parseJsonForTranscript(jsonStr);

            // WHEN
            result = transcribeAudioActivity.handleRequest(request);
        }

        // THEN
        NoteModel noteModel = result.getNote();

        verify(transcriptionDao).saveTranscription(captorTranscription.capture());
        verify(noteDao).saveNote(captorNote.capture());

        assertEquals(expectedTranscriptionId, captorTranscription.getValue().getTranscriptionId());

        assertEquals(email, noteModel.getEmail());
        assertEquals(expectedTranscript, captorNote.getValue().getContent());
        assertEquals(expectedTranscriptionId, captorNote.getValue().getTranscriptionId());
    }
}
