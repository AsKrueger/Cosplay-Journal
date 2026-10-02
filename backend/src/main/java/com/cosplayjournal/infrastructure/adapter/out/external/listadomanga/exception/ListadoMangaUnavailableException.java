package com.cosplayjournal.infrastructure.adapter.out.external.listadomanga.exception;

public class ListadoMangaUnavailableException extends RuntimeException {

    public ListadoMangaUnavailableException(String message) {
        super(message);
    }

    public ListadoMangaUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
