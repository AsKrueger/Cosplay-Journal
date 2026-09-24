package com.cosplayjournal.domain.exception;

public class InvalidEventDataException extends RuntimeException {

    public InvalidEventDataException(String message) {
        super(message);
    }

    public InvalidEventDataException(String message, Throwable cause) {
        super(message, cause);
    }
}
