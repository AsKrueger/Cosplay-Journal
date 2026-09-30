package com.cosplayjournal.infrastructure.adapter.in.kafka;

import com.cosplayjournal.application.port.in.ProcessParticipantJoinedCommand;
import com.cosplayjournal.application.port.in.ProcessParticipantJoinedUseCase;
import com.cosplayjournal.domain.model.participation.ParticipantRole;
import com.cosplayjournal.infrastructure.adapter.out.messaging.kafka.dto.ParticipantJoinedMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ParticipantJoinedKafkaConsumerAdapterTest {

    @Mock
    private ProcessParticipantJoinedUseCase processParticipantJoinedUseCase;

    private ParticipantJoinedKafkaConsumerAdapter consumerAdapter;

    @BeforeEach
    void setUp() {
        consumerAdapter = new ParticipantJoinedKafkaConsumerAdapter(processParticipantJoinedUseCase);
    }

    @Test
    @DisplayName("Debe procesar mensaje Kafka ParticipantJoinedMessage e invocar el caso de uso de aplicación")
    void shouldConsumeParticipantJoinedMessageAndInvokeUseCase() {
        ParticipantJoinedMessage message = new ParticipantJoinedMessage(
                "msg-123",
                "ParticipantJoined",
                Instant.now().toString(),
                "part-555",
                "user-777",
                "MEMBER"
        );

        consumerAdapter.consume(message);

        ArgumentCaptor<ProcessParticipantJoinedCommand> captor = ArgumentCaptor.forClass(ProcessParticipantJoinedCommand.class);
        verify(processParticipantJoinedUseCase, times(1)).processParticipantJoined(captor.capture());

        ProcessParticipantJoinedCommand command = captor.getValue();
        assertEquals("part-555", command.participationId());
        assertEquals("user-777", command.userId());
        assertEquals(ParticipantRole.MEMBER, command.role());
    }
}
