package com.cosplayjournal.domain.event;

import com.cosplayjournal.domain.model.event.EventSource;

import java.time.Instant;

public record EventUpdatedEvent(
        String eventId,
        String name,
        EventSource source,
        Instant occurredOn
) implements DomainEvent {
    public EventUpdatedEvent(String eventId, String name, EventSource source) {
        this(eventId, name, source, Instant.now());
    }
}
