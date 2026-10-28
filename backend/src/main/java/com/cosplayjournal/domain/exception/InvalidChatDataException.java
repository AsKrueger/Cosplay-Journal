package com.cosplayjournal.domain.exception;

public class InvalidChatDataException extends RuntimeException {
    public InvalidChatDataException(String message) {
        super(message);
    }
}
