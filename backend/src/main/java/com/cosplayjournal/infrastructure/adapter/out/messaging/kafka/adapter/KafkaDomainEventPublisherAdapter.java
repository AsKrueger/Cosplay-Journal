package com.cosplayjournal.infrastructure.adapter.out.messaging.kafka.adapter;

import com.cosplayjournal.application.port.out.DomainEventPublisherPort;
import com.cosplayjournal.domain.event.*;
import com.cosplayjournal.infrastructure.adapter.out.messaging.kafka.mapper.KafkaEventMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@Primary
@Profile("!test")
public class KafkaDomainEventPublisherAdapter implements DomainEventPublisherPort {

    private static final Logger log = LoggerFactory.getLogger(KafkaDomainEventPublisherAdapter.class);

    public static final String PARTICIPATION_TOPIC = "cosplay-journal.participation";
    public static final String PHOTO_TOPIC = "cosplay-journal.photo";
    public static final String EVENT_TOPIC = "cosplay-journal.event";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public KafkaDomainEventPublisherAdapter(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void publish(DomainEvent event) {
        if (event == null) return;

        try {
            if (event instanceof ParticipationCreatedEvent participationCreated) {
                var message = KafkaEventMapper.toMessage(participationCreated);
                kafkaTemplate.send(PARTICIPATION_TOPIC, participationCreated.participationId(), message);
                log.info("Enviado mensaje Kafka ParticipationCreated a topic '{}' con key '{}'", PARTICIPATION_TOPIC, participationCreated.participationId());
            } else if (event instanceof ParticipantJoinedEvent participantJoined) {
                var message = KafkaEventMapper.toMessage(participantJoined);
                kafkaTemplate.send(PARTICIPATION_TOPIC, participantJoined.participationId(), message);
                log.info("Enviado mensaje Kafka ParticipantJoined a topic '{}' con key '{}'", PARTICIPATION_TOPIC, participantJoined.participationId());
            } else if (event instanceof PhotoUploadedEvent photoUploaded) {
                var message = KafkaEventMapper.toMessage(photoUploaded);
                kafkaTemplate.send(PHOTO_TOPIC, photoUploaded.participationId(), message);
                log.info("Enviado mensaje Kafka PhotoUploaded a topic '{}' con key '{}'", PHOTO_TOPIC, photoUploaded.participationId());
            } else if (event instanceof EventUpdatedEvent eventUpdated) {
                var message = KafkaEventMapper.toMessage(eventUpdated);
                kafkaTemplate.send(EVENT_TOPIC, eventUpdated.eventId(), message);
                log.info("Enviado mensaje Kafka EventUpdated a topic '{}' con key '{}'", EVENT_TOPIC, eventUpdated.eventId());
            } else {
                log.info("Evento de dominio publicado de forma síncrona -> EventId: {}, Type: {}", event.eventId(), event.getClass().getSimpleName());
            }
        } catch (Exception e) {
            log.error("Error al publicar evento en Kafka -> EventId: {}", event.eventId(), e);
        }
    }
}
