package com.cosplayjournal.application.port.in;

import com.cosplayjournal.domain.model.participation.ParticipantRole;
import com.cosplayjournal.domain.model.participation.ParticipationId;

public record JoinParticipationCommand(
        ParticipationId participationId,
        String userId,
        String name,
        ParticipantRole role
) {
    public JoinParticipationCommand {
        if (participationId == null) {
            throw new IllegalArgumentException("El ID de la participación es obligatorio");
        }
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID del usuario es obligatorio");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del participante es obligatorio");
        }
    }
}
