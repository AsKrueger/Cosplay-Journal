package com.cosplayjournal.infrastructure.adapter.in.rest.dto;

import com.cosplayjournal.domain.model.CosplayStatus;
import jakarta.validation.constraints.NotNull;

public record ChangeCosplayStatusRequest(
        @NotNull(message = "El nuevo estado del cosplay es obligatorio")
        CosplayStatus status
) {
}
