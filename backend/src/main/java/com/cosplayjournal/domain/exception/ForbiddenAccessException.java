package com.cosplayjournal.domain.exception;

public class ForbiddenAccessException extends RuntimeException {

    public ForbiddenAccessException(String message) {
        super(message);
    }

    public ForbiddenAccessException() {
        super("No dispone de permisos para acceder o modificar este recurso");
    }
}
