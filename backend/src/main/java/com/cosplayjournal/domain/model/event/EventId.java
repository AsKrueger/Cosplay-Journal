package com.cosplayjournal.domain.model.event;

import com.cosplayjournal.domain.exception.InvalidEventDataException;

import java.util.Objects;
import java.util.UUID;

public record EventId(String value) {

    public EventId {
        if (value == null || value.trim().isEmpty()) {
            throw new InvalidEventDataException("El ID del evento no puede estar vacío");
        }
        value = value.trim();
    }

    public static EventId generate() {
        return new EventId(UUID.randomUUID().toString());
    }

    public static EventId of(String value) {
        return new EventId(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
