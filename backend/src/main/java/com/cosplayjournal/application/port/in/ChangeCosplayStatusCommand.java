package com.cosplayjournal.application.port.in;

import com.cosplayjournal.domain.model.CosplayStatus;

public record ChangeCosplayStatusCommand(
        Long cosplayId,
        CosplayStatus newStatus
) {
    public ChangeCosplayStatusCommand {
        if (cosplayId == null) {
            throw new IllegalArgumentException("El ID del cosplay es obligatorio");
        }
        if (newStatus == null) {
            throw new IllegalArgumentException("El nuevo estado del cosplay es obligatorio");
        }
    }
}
