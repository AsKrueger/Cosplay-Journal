package com.cosplayjournal.domain.event;

import com.cosplayjournal.domain.model.CosplayStatus;

import java.time.Instant;
import java.util.UUID;

public record CosplayStatusChangedEvent(
        String eventId,
        Long cosplayId,
        CosplayStatus previousStatus,
        CosplayStatus newStatus,
        Instant occurredOn
) implements DomainEvent {

    public CosplayStatusChangedEvent(Long cosplayId, CosplayStatus previousStatus, CosplayStatus newStatus) {
        this(UUID.randomUUID().toString(), cosplayId, previousStatus, newStatus, Instant.now());
    }
}
