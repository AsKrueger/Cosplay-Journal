package com.cosplayjournal.infrastructure.adapter.in.rest.dto;

import jakarta.validation.constraints.NotBlank;

public record AssignCharacterRequest(
        @NotBlank(message = "El ID del usuario es obligatorio")
        String userId,

        @NotBlank(message = "El nombre del personaje asignado es obligatorio")
        String characterName
) {
}
