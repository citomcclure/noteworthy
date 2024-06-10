package com.nashss.se.noteworthy.exceptions;

public class TranscriptionException extends RuntimeException {

    private static final long serialVersionUID = -6571101872693071875L;

    /**
     * Exception with no message or cause.
     */
    public TranscriptionException() {
        super();
    }

    /**
     * Exception with a message, but no cause.
     * @param message A descriptive message for this exception.
     */
    public TranscriptionException(String message) {
        super(message);
    }

    /**
     * Exception with no message, but with a cause.
     * @param cause The original throwable resulting in this exception.
     */
    public TranscriptionException(Throwable cause) {
        super(cause);
    }

    /**
     * Exception with message and cause.
     * @param message A descriptive message for this exception.
     * @param cause The original throwable resulting in this exception.
     */
    public TranscriptionException(String message, Throwable cause) {
        super(message, cause);
    }
}
