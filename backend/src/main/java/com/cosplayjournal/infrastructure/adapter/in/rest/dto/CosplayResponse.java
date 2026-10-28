package com.cosplayjournal.infrastructure.adapter.in.rest.dto;

import java.time.Instant;

public record CosplayResponse(
        Long id,
        String name,
        String photoUrl,
        Instant createdAt,
        Instant updatedAt
) {
}
