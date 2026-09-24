package com.cosplayjournal.domain.exception;

public class InvalidCosplayDataException extends RuntimeException {

    public InvalidCosplayDataException(String message) {
        super(message);
    }

    public InvalidCosplayDataException(String message, Throwable cause) {
        super(message, cause);
    }
}
