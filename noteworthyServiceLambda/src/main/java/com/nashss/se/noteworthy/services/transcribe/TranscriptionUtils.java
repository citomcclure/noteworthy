package com.nashss.se.noteworthy.services.transcribe;

import com.nashss.se.noteworthy.exceptions.TranscriptionException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * Helpful method(s) for TranscribeAudio endpoint that are not service specific (e.g., DynamoDB or S3).
 */
public class TranscriptionUtils {
    static final int ID_PREFIX_LENGTH = 5;

    /**
     * API Gateway encodes the request body whenever multimedia files are sent via form-data,
     * which also included header information like content-type and file variable name. Method
     * removes anything before byte sequence 'RIFF', which corresponds to the start of a .wav file
     * @param encodedArr the Base64 encoded area including headers and .wav binary
     * @return byte array corresponding to .wav binary
     */
    public static byte[] removeEncodedHeaders(byte[] encodedArr) {
        for (int i = 0; i < encodedArr.length; i++) {
            // byte sequence corresponds to 'RIFF' found at the beginning of every .wav file
            if (encodedArr[i] == 82 && encodedArr[i + 1] == 73 && encodedArr[i + 2] == 70 && encodedArr[i + 3] == 70) {
                return Arrays.copyOfRange(encodedArr, i, encodedArr.length);
            }
        }

        throw new RuntimeException(".wav file not found in request.");
    }

    /**
     * Transcription job results in json string that needs to be parsed to obtain transcription.
     * @param json of successful transcription job
     * @return the string result of transcription
     * @throws JsonProcessingException if there is error processing json
     */
    public static String parseJsonForTranscript(String json) throws JsonProcessingException {
        JsonNode jsonNode = (new ObjectMapper()).readTree(json);
        return jsonNode.get("results").get("transcripts").get(0).get("transcript").asText();
    }

    /**
     * Create unique transcription identifier for bucket keys, transcription job, and transcription output.
     * @param localDateTime string representation of current time corresponding to dateCreated of new voice note
     * @return a string combining prefix and current time
     */
    public static String generateTranscriptionId(String localDateTime) {
        String transcriptionId = RandomStringUtils.randomAlphanumeric(ID_PREFIX_LENGTH) +
                "_" + localDateTime;

        // transcription job names do not allow colon character
        return transcriptionId.replace(":", "-");
    }

    /**
     * Using AudioInputStream, we are making sure the audio is a valid WAV file and obtaining the sample rate.
     * @param audio byte array of audio
     * @param log logger
     * @return the sample rate in Hz
     */
    public static int getSampleRate(byte[] audio, Logger log) {
        // Ensure media file is valid WAV file and identify sample rate
        AudioFormat audioFormat;
        int sampleRate = 0;
        log.info("Attempting to identify sample rate...");
        try {
            // Create I/O stream of audio
            InputStream inputStream = new ByteArrayInputStream(audio);

            // Stream as AudioInputStream to get audio format
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(inputStream);
            audioFormat = audioInputStream.getFormat();

            inputStream.close();
            audioInputStream.close();

            if (audioFormat.getSampleRate() != AudioSystem.NOT_SPECIFIED) {
                sampleRate = (int) audioFormat.getSampleRate();
                log.info("Sample rate correctly identified as {} Hz.", sampleRate);
            } else {
                // If sampleRate is not specified, we will default to 48 kHz sample rate expected from the frontend
                sampleRate = 48_000;
                log.info("Could not identify sample rate, setting to default.");
            }
        } catch (IOException e) {
            throw new TranscriptionException("Issue streaming WAV file.", e);
        } catch (UnsupportedAudioFileException e) {
            throw new TranscriptionException("Media file not recognized as valid WAV file.", e);
        }

        return sampleRate;
    }
}
