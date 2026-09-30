package com.cosplayjournal.infrastructure.adapter.out.messaging.kafka.mapper;

import com.cosplayjournal.domain.event.ParticipantJoinedEvent;
import com.cosplayjournal.domain.event.ParticipationCreatedEvent;
import com.cosplayjournal.domain.event.PhotoUploadedEvent;
import com.cosplayjournal.infrastructure.adapter.out.messaging.kafka.dto.ParticipantJoinedMessage;
import com.cosplayjournal.infrastructure.adapter.out.messaging.kafka.dto.ParticipationCreatedMessage;
import com.cosplayjournal.infrastructure.adapter.out.messaging.kafka.dto.PhotoUploadedMessage;

public class KafkaEventMapper {

    public static ParticipationCreatedMessage toMessage(ParticipationCreatedEvent event) {
        return new ParticipationCreatedMessage(
                event.eventId(),
                "ParticipationCreated",
                event.occurredOn().toString(),
                event.participationId(),
                event.eventRefId(),
                event.cosplayId(),
                event.type().name()
        );
    }

    public static ParticipantJoinedMessage toMessage(ParticipantJoinedEvent event) {
        return new ParticipantJoinedMessage(
                event.eventId(),
                "ParticipantJoined",
                event.occurredOn().toString(),
                event.participationId(),
                event.userId(),
                event.role().name()
        );
    }

    public static PhotoUploadedMessage toMessage(PhotoUploadedEvent event) {
        return new PhotoUploadedMessage(
                event.eventId(),
                "PhotoUploaded",
                event.occurredOn().toString(),
                event.photoId(),
                event.participationId(),
                event.uploadedByUserId(),
                event.storageReference()
        );
    }
}
