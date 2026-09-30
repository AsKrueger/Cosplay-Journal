package com.cosplayjournal.infrastructure.adapter.in.kafka;

import com.cosplayjournal.application.port.in.ProcessParticipantJoinedCommand;
import com.cosplayjournal.application.port.in.ProcessParticipantJoinedUseCase;
import com.cosplayjournal.domain.model.participation.ParticipantRole;
import com.cosplayjournal.infrastructure.adapter.out.messaging.kafka.dto.ParticipantJoinedMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class ParticipantJoinedKafkaConsumerAdapter {

    private static final Logger log = LoggerFactory.getLogger(ParticipantJoinedKafkaConsumerAdapter.class);

    private final ProcessParticipantJoinedUseCase processParticipantJoinedUseCase;

    public ParticipantJoinedKafkaConsumerAdapter(ProcessParticipantJoinedUseCase processParticipantJoinedUseCase) {
        this.processParticipantJoinedUseCase = processParticipantJoinedUseCase;
    }

    @KafkaListener(topics = "cosplay-journal.participation", groupId = "cosplay-journal-group")
    public void consume(Object record) {
        if (record instanceof ParticipantJoinedMessage message) {
            log.info("Consumidor Kafka recibió mensaje ParticipantJoined -> User: {}, Participation: {}",
                    message.userId(), message.aggregateId());

            Instant occurredOn = message.occurredAt() != null ? Instant.parse(message.occurredAt()) : Instant.now();
            ParticipantRole role = message.role() != null ? ParticipantRole.valueOf(message.role()) : ParticipantRole.MEMBER;

            ProcessParticipantJoinedCommand command = new ProcessParticipantJoinedCommand(
                    message.eventId(),
                    message.aggregateId(),
                    message.userId(),
                    role,
                    occurredOn
            );

            processParticipantJoinedUseCase.processParticipantJoined(command);
        } else {
            log.debug("Mensaje ignorado en topic 'cosplay-journal.participation': {}", record != null ? record.getClass().getSimpleName() : "null");
        }
    }
}
