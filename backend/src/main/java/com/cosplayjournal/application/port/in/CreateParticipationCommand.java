package com.cosplayjournal.application.port.in;

import com.cosplayjournal.domain.model.event.EventId;
import com.cosplayjournal.domain.model.participation.ParticipationType;

public record CreateParticipationCommand(
        EventId eventId,
        Long cosplayId,
        ParticipationType type,
        String leaderUserId,
        String leaderName,
        String groupName
) {
    public CreateParticipationCommand {
        if (eventId == null) {
            throw new IllegalArgumentException("El ID del evento es obligatorio");
        }
        if (cosplayId == null) {
            throw new IllegalArgumentException("El ID del cosplay es obligatorio");
        }
        if (type == null) {
            throw new IllegalArgumentException("El tipo de participación es obligatorio");
        }
        if (leaderUserId == null || leaderUserId.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID del usuario líder es obligatorio");
        }
        if (leaderName == null || leaderName.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del líder es obligatorio");
        }
    }
}
