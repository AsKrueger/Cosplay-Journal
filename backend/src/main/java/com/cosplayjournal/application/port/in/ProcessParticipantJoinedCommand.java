package com.cosplayjournal.application.port.in;

import com.cosplayjournal.domain.model.participation.ParticipantRole;

import java.time.Instant;

public record ProcessParticipantJoinedCommand(
        String eventId,
        String participationId,
        String userId,
        ParticipantRole role,
        Instant occurredOn
) {
    public ProcessParticipantJoinedCommand {
        if (eventId == null || eventId.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID del evento es obligatorio");
        }
        if (participationId == null || participationId.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID de la participación es obligatorio");
        }
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID del usuario es obligatorio");
        }
    }
}
