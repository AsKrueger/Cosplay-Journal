package com.cosplayjournal.domain.event;

import java.time.Instant;
import java.util.UUID;

public record EventCreatedEvent(
        String eventId,
        String eventRefId,
        String name,
        Instant occurredOn
) implements DomainEvent {

    public EventCreatedEvent(String eventRefId, String name) {
        this(UUID.randomUUID().toString(), eventRefId, name, Instant.now());
    }
}
