package com.cosplayjournal.infrastructure.adapter.in.rest.dto;

import java.time.Instant;
import java.util.List;

public record ParticipationResponse(
        String id,
        String eventId,
        Long cosplayId,
        String type,
        String status,
        String groupName,
        List<ParticipantResponse> participants,
        Instant createdAt,
        Instant updatedAt
) {
}
