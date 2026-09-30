package com.cosplayjournal.application.port.in;

import com.cosplayjournal.domain.model.participation.ParticipationId;

public record AddPhotoCommand(
        ParticipationId participationId,
        String storageReference,
        String caption,
        String uploadedByUserId
) {
    public AddPhotoCommand {
        if (participationId == null) {
            throw new IllegalArgumentException("El ID de la participación es obligatorio");
        }
        if (storageReference == null || storageReference.trim().isEmpty()) {
            throw new IllegalArgumentException("La referencia de almacenamiento de la fotografía es obligatoria");
        }
    }
}
