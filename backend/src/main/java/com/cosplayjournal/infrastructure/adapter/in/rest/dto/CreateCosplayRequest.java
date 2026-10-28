package com.cosplayjournal.infrastructure.adapter.in.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCosplayRequest(
        @NotBlank(message = "El nombre del cosplay es obligatorio")
        @Size(min = 2, max = 100, message = "El nombre del cosplay debe tener entre 2 y 100 caracteres")
        String name,

        @Size(max = 500, message = "La URL de la foto no puede exceder 500 caracteres")
        String photoUrl
) {
}
