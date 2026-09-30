package com.cosplayjournal.infrastructure.adapter.in.rest.dto;

import jakarta.validation.constraints.NotBlank;

public record AddPhotoRequest(
        @NotBlank(message = "El ID de la participación es obligatorio")
        String participationId,

        @NotBlank(message = "La referencia de almacenamiento es obligatoria")
        String storageReference,

        String caption,
        String uploadedByUserId
) {
}
