package com.cosplayjournal.infrastructure.adapter.out.messaging.kafka.dto;

public record PhotoUploadedMessage(
        String eventId,
        String eventType,
        String occurredAt,
        String aggregateId,
        String participationId,
        String uploadedByUserId,
        String storageReference
) {
}
