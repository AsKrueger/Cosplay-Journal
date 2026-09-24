package com.cosplayjournal.domain.event;

import com.cosplayjournal.domain.model.participation.ParticipantRole;

import java.time.Instant;
import java.util.UUID;

public record ParticipantJoinedEvent(
        String eventId,
        String participationId,
        String userId,
        ParticipantRole role,
        Instant occurredOn
) implements DomainEvent {

    public ParticipantJoinedEvent(String participationId, String userId, ParticipantRole role) {
        this(UUID.randomUUID().toString(), participationId, userId, role, Instant.now());
    }
}
