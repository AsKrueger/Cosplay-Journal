package com.cosplayjournal.domain.model.participation;

import com.cosplayjournal.domain.exception.InvalidParticipationDataException;

import java.util.Objects;
import java.util.UUID;

public record ParticipationId(String value) {

    public ParticipationId {
        if (value == null || value.trim().isEmpty()) {
            throw new InvalidParticipationDataException("El ID de la participación no puede estar vacío");
        }
        value = value.trim();
    }

    public static ParticipationId generate() {
        return new ParticipationId(UUID.randomUUID().toString());
    }

    public static ParticipationId of(String value) {
        return new ParticipationId(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
