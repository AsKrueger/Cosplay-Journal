package com.cosplayjournal.domain.exception;

public class InvalidStateTransitionException extends InvalidCosplayDataException {

    public InvalidStateTransitionException(String message) {
        super(message);
    }

    public InvalidStateTransitionException(String currentStatus, String targetStatus) {
        super("Transición de estado no permitida de " + currentStatus + " a " + targetStatus);
    }
}
