package com.cosplayjournal.infrastructure.adapter.out.messaging.kafka;

import com.cosplayjournal.domain.event.ParticipantJoinedEvent;
import com.cosplayjournal.domain.event.ParticipationCreatedEvent;
import com.cosplayjournal.domain.model.participation.ParticipantRole;
import com.cosplayjournal.domain.model.participation.ParticipationType;
import com.cosplayjournal.infrastructure.adapter.out.messaging.kafka.adapter.KafkaDomainEventPublisherAdapter;
import com.cosplayjournal.infrastructure.adapter.out.messaging.kafka.dto.ParticipantJoinedMessage;
import com.cosplayjournal.infrastructure.adapter.out.messaging.kafka.dto.ParticipationCreatedMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KafkaDomainEventPublisherAdapterTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    private KafkaDomainEventPublisherAdapter publisherAdapter;

    @BeforeEach
    void setUp() {
        publisherAdapter = new KafkaDomainEventPublisherAdapter(kafkaTemplate);
    }

    @Test
    @DisplayName("Debe publicar ParticipantJoinedEvent en topic cosplay-journal.participation con key = participationId")
    void shouldPublishParticipantJoinedEventToKafka() {
        ParticipantJoinedEvent event = new ParticipantJoinedEvent("part-999", "user-888", ParticipantRole.LEADER);

        publisherAdapter.publish(event);

        verify(kafkaTemplate, times(1)).send(
                eq(KafkaDomainEventPublisherAdapter.PARTICIPATION_TOPIC),
                eq("part-999"),
                any(ParticipantJoinedMessage.class)
        );
    }

    @Test
    @DisplayName("Debe publicar ParticipationCreatedEvent en topic cosplay-journal.participation con key = participationId")
    void shouldPublishParticipationCreatedEventToKafka() {
        ParticipationCreatedEvent event = new ParticipationCreatedEvent("part-1000", "evt-1", 10L, ParticipationType.GROUP);

        publisherAdapter.publish(event);

        verify(kafkaTemplate, times(1)).send(
                eq(KafkaDomainEventPublisherAdapter.PARTICIPATION_TOPIC),
                eq("part-1000"),
                any(ParticipationCreatedMessage.class)
        );
    }
}
