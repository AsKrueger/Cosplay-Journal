package com.cosplayjournal.infrastructure.adapter.in.rest.dto;

import jakarta.validation.constraints.Size;

public record ChangeAvatarRequest(
        @Size(max = 500, message = "La URL de la foto no puede exceder 500 caracteres")
        String avatarUrl
) {
}
