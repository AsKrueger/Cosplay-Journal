package com.cosplayjournal.infrastructure.adapter.in.rest.dto;

import com.cosplayjournal.domain.model.participation.ParticipantRole;
import jakarta.validation.constraints.NotBlank;

public record JoinParticipationRequest(
        @NotBlank(message = "El ID del usuario es obligatorio")
        String userId,

        @NotBlank(message = "El nombre del participante es obligatorio")
        String name,

        ParticipantRole role
) {
}
