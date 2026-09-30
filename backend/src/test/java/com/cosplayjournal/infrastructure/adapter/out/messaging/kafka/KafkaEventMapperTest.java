package com.cosplayjournal.infrastructure.adapter.out.messaging.kafka;

import com.cosplayjournal.domain.event.ParticipantJoinedEvent;
import com.cosplayjournal.domain.event.ParticipationCreatedEvent;
import com.cosplayjournal.domain.event.PhotoUploadedEvent;
import com.cosplayjournal.domain.model.participation.ParticipantRole;
import com.cosplayjournal.domain.model.participation.ParticipationType;
import com.cosplayjournal.infrastructure.adapter.out.messaging.kafka.dto.ParticipantJoinedMessage;
import com.cosplayjournal.infrastructure.adapter.out.messaging.kafka.dto.ParticipationCreatedMessage;
import com.cosplayjournal.infrastructure.adapter.out.messaging.kafka.dto.PhotoUploadedMessage;
import com.cosplayjournal.infrastructure.adapter.out.messaging.kafka.mapper.KafkaEventMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class KafkaEventMapperTest {

    @Test
    @DisplayName("Debe mapear ParticipationCreatedEvent a ParticipationCreatedMessage correctamente")
    void shouldMapParticipationCreatedEvent() {
        ParticipationCreatedEvent event = new ParticipationCreatedEvent("part-1", "evt-1", 10L, ParticipationType.GROUP);

        ParticipationCreatedMessage message = KafkaEventMapper.toMessage(event);

        assertNotNull(message);
        assertEquals(event.eventId(), message.eventId());
        assertEquals("ParticipationCreated", message.eventType());
        assertEquals("part-1", message.aggregateId());
        assertEquals("evt-1", message.eventRefId());
        assertEquals(10L, message.cosplayId());
        assertEquals("GROUP", message.type());
    }

    @Test
    @DisplayName("Debe mapear ParticipantJoinedEvent a ParticipantJoinedMessage correctamente")
    void shouldMapParticipantJoinedEvent() {
        ParticipantJoinedEvent event = new ParticipantJoinedEvent("part-1", "user-100", ParticipantRole.LEADER);

        ParticipantJoinedMessage message = KafkaEventMapper.toMessage(event);

        assertNotNull(message);
        assertEquals(event.eventId(), message.eventId());
        assertEquals("ParticipantJoined", message.eventType());
        assertEquals("part-1", message.aggregateId());
        assertEquals("user-100", message.userId());
        assertEquals("LEADER", message.role());
    }

    @Test
    @DisplayName("Debe mapear PhotoUploadedEvent a PhotoUploadedMessage correctamente")
    void shouldMapPhotoUploadedEvent() {
        PhotoUploadedEvent event = new PhotoUploadedEvent("photo-1", "part-1", "user-100", "s3://photos/photo1.jpg");

        PhotoUploadedMessage message = KafkaEventMapper.toMessage(event);

        assertNotNull(message);
        assertEquals("PhotoUploaded", message.eventType());
        assertEquals("photo-1", message.aggregateId());
        assertEquals("part-1", message.participationId());
        assertEquals("s3://photos/photo1.jpg", message.storageReference());
    }
}
