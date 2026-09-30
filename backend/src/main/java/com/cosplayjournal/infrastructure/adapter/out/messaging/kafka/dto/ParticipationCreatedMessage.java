package com.cosplayjournal.infrastructure.adapter.out.messaging.kafka.dto;

public record ParticipationCreatedMessage(
        String eventId,
        String eventType,
        String occurredAt,
        String aggregateId,
        String eventRefId,
        Long cosplayId,
        String type
) {
}
