package com.cosplayjournal.domain.model.social;

import com.cosplayjournal.domain.exception.InvalidUserDataException;
import com.cosplayjournal.domain.model.user.UserId;

import java.time.Instant;
import java.util.List;

/**
 * Participación del usuario en un evento. "Voy" y "Fui" se usan en el calendario;
 * "NONE" significa que no hay selección y no se guarda.
 */
public record UserEventStatus(UserId userId, String eventId, String status, Instant updatedAt) {

    private static final List<String> STATUSES = List.of("ATTENDING", "ATTENDED", "INTERESTED", "PLANNING");

    public UserEventStatus {
        if (userId == null) {
            throw new InvalidUserDataException("El usuario es obligatorio");
        }
        if (eventId == null || eventId.isBlank() || eventId.length() > 150) {
            throw new InvalidUserDataException("El identificador del evento no es válido");
        }
        if (!STATUSES.contains(status)) {
            throw new InvalidUserDataException("Estado de participación no válido: " + status);
        }
        if (updatedAt == null) {
            updatedAt = Instant.now();
        }
    }

    /** Valores que el usuario puede guardar. "NONE" no se guarda: borra la selección. */
    public static boolean isStorable(String status) {
        return STATUSES.contains(status);
    }
}
