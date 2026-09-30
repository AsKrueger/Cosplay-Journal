package com.cosplayjournal.domain.model.user;

import com.cosplayjournal.domain.exception.InvalidUserDataException;

import java.util.Objects;
import java.util.UUID;

public record UserId(String value) {

    public UserId {
        if (value == null || value.trim().isEmpty()) {
            throw new InvalidUserDataException("El ID del usuario no puede estar vacío");
        }
        value = value.trim();
    }

    public static UserId generate() {
        return new UserId(UUID.randomUUID().toString());
    }

    public static UserId of(String value) {
        return new UserId(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
