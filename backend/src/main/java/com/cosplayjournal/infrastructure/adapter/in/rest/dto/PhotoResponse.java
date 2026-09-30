package com.cosplayjournal.infrastructure.adapter.in.rest.dto;

import java.time.Instant;

public record PhotoResponse(
        String id,
        String participationId,
        String storageReference,
        String caption,
        String uploadedByUserId,
        Instant uploadedAt
) {
}
