package com.cosplayjournal.domain.model.photo;

import com.cosplayjournal.domain.exception.InvalidParticipationDataException;

import java.util.Objects;
import java.util.UUID;

public record PhotoId(String value) {

    public PhotoId {
        if (value == null || value.trim().isEmpty()) {
            throw new InvalidParticipationDataException("El ID de la fotografía no puede estar vacío");
        }
        value = value.trim();
    }

    public static PhotoId generate() {
        return new PhotoId(UUID.randomUUID().toString());
    }

    public static PhotoId of(String value) {
        return new PhotoId(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
