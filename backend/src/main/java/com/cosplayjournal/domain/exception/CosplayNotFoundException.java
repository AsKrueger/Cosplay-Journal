package com.cosplayjournal.domain.exception;

public class CosplayNotFoundException extends RuntimeException {

    public CosplayNotFoundException(Long id) {
        super("Cosplay con ID " + id + " no fue encontrado");
    }

    public CosplayNotFoundException(String message) {
        super(message);
    }
}
