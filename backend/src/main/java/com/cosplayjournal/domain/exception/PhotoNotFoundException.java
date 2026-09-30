package com.cosplayjournal.domain.exception;

import com.cosplayjournal.domain.model.photo.PhotoId;

public class PhotoNotFoundException extends RuntimeException {

    public PhotoNotFoundException(PhotoId id) {
        super("Fotografía con ID " + (id != null ? id.value() : "null") + " no fue encontrada");
    }

    public PhotoNotFoundException(String id) {
        super("Fotografía con ID " + id + " no fue encontrada");
    }
}
