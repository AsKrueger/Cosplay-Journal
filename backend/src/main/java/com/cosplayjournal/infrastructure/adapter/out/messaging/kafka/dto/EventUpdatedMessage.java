package com.cosplayjournal.infrastructure.adapter.out.messaging.kafka.dto;

import java.time.Instant;

public record EventUpdatedMessage(
        String eventId,
        String eventName,
        String source,
        Instant occurredOn
) {
}
