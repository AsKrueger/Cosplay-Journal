package com.cosplayjournal.infrastructure.adapter.in.rest.dto;

import java.time.Instant;

public record UserResponse(
        String id,
        String username,
        String email,
        String role,
        String status,
        Instant createdAt
) {
}
