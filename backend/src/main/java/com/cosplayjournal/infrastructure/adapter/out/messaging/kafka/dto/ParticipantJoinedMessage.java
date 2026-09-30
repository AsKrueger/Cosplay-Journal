package com.cosplayjournal.infrastructure.adapter.out.messaging.kafka.dto;

public record ParticipantJoinedMessage(
        String eventId,
        String eventType,
        String occurredAt,
        String aggregateId,
        String userId,
        String role
) {
}
