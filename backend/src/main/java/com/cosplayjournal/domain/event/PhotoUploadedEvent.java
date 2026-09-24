package com.cosplayjournal.domain.event;

import java.time.Instant;
import java.util.UUID;

public record PhotoUploadedEvent(
        String eventId,
        String photoId,
        String participationId,
        String uploadedByUserId,
        String storageReference,
        Instant occurredOn
) implements DomainEvent {

    public PhotoUploadedEvent(String photoId, String participationId, String uploadedByUserId, String storageReference) {
        this(UUID.randomUUID().toString(), photoId, participationId, uploadedByUserId, storageReference, Instant.now());
    }
}
