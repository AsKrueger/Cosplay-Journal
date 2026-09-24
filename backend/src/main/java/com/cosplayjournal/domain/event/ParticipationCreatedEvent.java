package com.cosplayjournal.domain.event;

import com.cosplayjournal.domain.model.participation.ParticipationType;

import java.time.Instant;
import java.util.UUID;

public record ParticipationCreatedEvent(
        String eventId,
        String participationId,
        String eventRefId,
        Long cosplayId,
        ParticipationType type,
        Instant occurredOn
) implements DomainEvent {

    public ParticipationCreatedEvent(String participationId, String eventRefId, Long cosplayId, ParticipationType type) {
        this(UUID.randomUUID().toString(), participationId, eventRefId, cosplayId, type, Instant.now());
    }
}
