package com.cosplayjournal.application.port.in;

import com.cosplayjournal.domain.model.participation.ParticipationId;

public record AssignCharacterCommand(
        ParticipationId participationId,
        String userId,
        String characterName
) {
    public AssignCharacterCommand {
        if (participationId == null) {
            throw new IllegalArgumentException("El ID de la participación es obligatorio");
        }
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID del usuario es obligatorio");
        }
        if (characterName == null || characterName.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del personaje asignado es obligatorio");
        }
    }
}
