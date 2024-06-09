package com.nashss.se.noteworthy.utils;

import org.apache.commons.lang3.RandomStringUtils;

import java.util.Arrays;

/**
 * Helpful method(s) for TranscribeAudio endpoint.
 */
public class TranscriptionUtils {
    static final int ID_LENGTH = 5;

    /**
     * Generates an alphanumeric string of length 5. Used for unique identification of
     * transcription keys for S3 objects and transcription job names.
     * @return an ID of length 5.
     */
    public static String generateID() {
        return RandomStringUtils.randomAlphanumeric(ID_LENGTH);
    }

    /**
     * API Gateway encodes the request body whenever multimedia files are sent via form-data,
     * which also included header information like content-type and file variable name. Method
     * removes anything before byte sequence 'RIFF', which corresponds to the start of a .wav file
     * @param encodedArr the Base64 encoded area including headers and .wav binary
     * @return byte array corresponding to .wav binary
     */
    public static byte[] removeEncodedHeaders(byte[] encodedArr) {
        boolean byteSequenceFound = false;
        for (int i = 0; i < encodedArr.length; i++) {
            // byte sequence corresponds to 'RIFF' found at the beginning of every .wav file
            if (encodedArr[i] == 82 && encodedArr[i+1] == 73 && encodedArr[i+2] == 70 && encodedArr[i+3] == 70) {
                return Arrays.copyOfRange(encodedArr, i, encodedArr.length-1);
            }
        }
        return null;
    }
}
