package com.cosplayjournal.infrastructure.adapter.in.rest.dto;

import java.time.Instant;

public record ParticipantResponse(
        String userId,
        String name,
        String role,
        String assignedCharacter,
        Instant joinedAt
) {
}
