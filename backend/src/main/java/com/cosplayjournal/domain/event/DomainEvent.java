package com.cosplayjournal.domain.event;

import java.time.Instant;

public interface DomainEvent {
    String eventId();
    Instant occurredOn();
}
