package com.cosplayjournal.domain.event;

import java.time.Instant;
import java.util.UUID;

public record CosplayCreatedEvent(
        String eventId,
        Long cosplayId,
        String name,
        Instant occurredOn
) implements DomainEvent {

    public CosplayCreatedEvent(Long cosplayId, String name) {
        this(UUID.randomUUID().toString(), cosplayId, name, Instant.now());
    }
}
