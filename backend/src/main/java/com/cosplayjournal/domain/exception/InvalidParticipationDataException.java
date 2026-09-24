package com.cosplayjournal.domain.exception;

public class InvalidParticipationDataException extends RuntimeException {

    public InvalidParticipationDataException(String message) {
        super(message);
    }

    public InvalidParticipationDataException(String message, Throwable cause) {
        super(message, cause);
    }
}
