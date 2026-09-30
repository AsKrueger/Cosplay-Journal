package com.cosplayjournal.infrastructure.adapter.in.rest.dto;

import com.cosplayjournal.domain.model.participation.ParticipationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateParticipationRequest(
        @NotBlank(message = "El ID del evento es obligatorio")
        String eventId,

        @NotNull(message = "El ID del cosplay es obligatorio")
        Long cosplayId,

        @NotNull(message = "El tipo de participación es obligatorio")
        ParticipationType type,

        @NotBlank(message = "El ID del usuario líder es obligatorio")
        String leaderUserId,

        @NotBlank(message = "El nombre del líder es obligatorio")
        String leaderName,

        String groupName
) {
}
