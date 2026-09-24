package com.cosplayjournal.infrastructure.adapter.in.rest.dto;

import java.time.Instant;

public record CosplayResponse(
        Long id,
        String name,
        String description,
        String characterName,
        String originSeries,
        String status,
        Instant createdAt,
        Instant updatedAt
) {
}
