package com.cosplayjournal.application.port.in;

import com.cosplayjournal.domain.model.participation.ParticipationId;

public record LeaveParticipationCommand(
        ParticipationId participationId,
        String userId
) {
    public LeaveParticipationCommand {
        if (participationId == null) {
            throw new IllegalArgumentException("El ID de la participación es obligatorio");
        }
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID del usuario es obligatorio");
        }
    }
}
