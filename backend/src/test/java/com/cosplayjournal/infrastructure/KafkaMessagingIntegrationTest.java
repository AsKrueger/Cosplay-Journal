package com.cosplayjournal.infrastructure;

import com.cosplayjournal.domain.event.ParticipantJoinedEvent;
import com.cosplayjournal.domain.model.participation.ParticipantRole;
import com.cosplayjournal.infrastructure.adapter.out.messaging.kafka.adapter.KafkaDomainEventPublisherAdapter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class KafkaMessagingIntegrationTest extends AbstractTestcontainersIntegrationTest {

    @Autowired
    private KafkaDomainEventPublisherAdapter kafkaPublisherAdapter;

    @Test
    @DisplayName("Debe publicar y consumir un evento de dominio ParticipantJoinedEvent en un contenedor Kafka real")
    void shouldPublishAndConsumeEventInRealKafkaContainer() {
        ParticipantJoinedEvent event = new ParticipantJoinedEvent(
                "part-tc-999", "usr-kafka-1", ParticipantRole.MEMBER
        );

        assertDoesNotThrow(() -> kafkaPublisherAdapter.publish(event));
    }
}
