package com.cosplayjournal.application;

import com.cosplayjournal.application.port.in.*;
import com.cosplayjournal.application.port.out.CosplayRepositoryPort;
import com.cosplayjournal.application.port.out.DomainEventPublisherPort;
import com.cosplayjournal.application.port.out.EventRepositoryPort;
import com.cosplayjournal.application.port.out.ParticipationRepositoryPort;
import com.cosplayjournal.application.service.ParticipationApplicationService;
import com.cosplayjournal.domain.event.ParticipantJoinedEvent;
import com.cosplayjournal.domain.event.ParticipationCreatedEvent;
import com.cosplayjournal.domain.exception.CosplayNotFoundException;
import com.cosplayjournal.domain.exception.EventNotFoundException;
import com.cosplayjournal.domain.exception.ParticipationNotFoundException;
import com.cosplayjournal.domain.model.Cosplay;
import com.cosplayjournal.domain.model.event.*;
import com.cosplayjournal.domain.model.participation.*;
import com.cosplayjournal.domain.service.ParticipationValidationDomainService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ParticipationApplicationServiceTest {

    @Mock
    private ParticipationRepositoryPort participationRepositoryPort;

    @Mock
    private EventRepositoryPort eventRepositoryPort;

    @Mock
    private CosplayRepositoryPort cosplayRepositoryPort;

    @Mock
    private DomainEventPublisherPort eventPublisherPort;

    private ParticipationApplicationService participationApplicationService;

    @BeforeEach
    void setUp() {
        participationApplicationService = new ParticipationApplicationService(
                participationRepositoryPort, eventRepositoryPort, cosplayRepositoryPort,
                eventPublisherPort, new ParticipationValidationDomainService()
        );
    }

    @Test
    @DisplayName("Debe crear una participación si el Evento y Cosplay existen")
    void shouldCreateParticipationSuccessfully() {
        EventId eventId = EventId.of("evt-1");
        Long cosplayId = 10L;

        Event event = Event.create(eventId, "Salon Manga", "Desc", LocalDate.now(), LocalDate.now(), EventLocation.of("Sevilla", "Fibes"), "", EventSource.MANUAL_ADMIN);
        Cosplay cosplay = Cosplay.createNew("Luffy", "Gear 5", "Luffy", "One Piece").withId(cosplayId);

        when(eventRepositoryPort.findById(eventId)).thenReturn(Optional.of(event));
        when(cosplayRepositoryPort.findById(cosplayId)).thenReturn(Optional.of(cosplay));
        when(participationRepositoryPort.save(any(Participation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CreateParticipationCommand command = new CreateParticipationCommand(
                eventId, cosplayId, ParticipationType.INDIVIDUAL, "user-leader", "Alonso", "Grupo One Piece"
        );

        Participation result = participationApplicationService.createParticipation(command);

        assertNotNull(result);
        assertEquals(eventId, result.getEventId());
        assertEquals(cosplayId, result.getCosplayId());
        assertEquals(1, result.getParticipants().size());

        verify(eventPublisherPort, times(1)).publish(any(ParticipationCreatedEvent.class));
        verify(eventPublisherPort, times(1)).publish(any(ParticipantJoinedEvent.class));
    }

    @Test
    @DisplayName("Debe lanzar EventNotFoundException si el evento no existe al crear participación")
    void shouldThrowExceptionWhenEventDoesNotExist() {
        EventId eventId = EventId.of("evt-999");
        when(eventRepositoryPort.findById(eventId)).thenReturn(Optional.empty());

        CreateParticipationCommand command = new CreateParticipationCommand(
                eventId, 10L, ParticipationType.INDIVIDUAL, "u1", "Alonso", ""
        );

        assertThrows(EventNotFoundException.class, () -> participationApplicationService.createParticipation(command));
    }

    @Test
    @DisplayName("Debe permitir unirse a una participación grupal existente")
    void shouldJoinParticipation() {
        ParticipationId participationId = ParticipationId.of("part-100");
        Participation participation = Participation.create(
                participationId, EventId.of("evt-1"), 10L, ParticipationType.GROUP, Participant.createLeader("u1", "Alonso")
        );

        when(participationRepositoryPort.findById(participationId)).thenReturn(Optional.of(participation));
        when(participationRepositoryPort.save(any(Participation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        JoinParticipationCommand command = new JoinParticipationCommand(participationId, "u2", "Beatriz", ParticipantRole.MEMBER);

        Participation updated = participationApplicationService.joinParticipation(command);

        assertEquals(2, updated.getParticipants().size());
        verify(eventPublisherPort, times(1)).publish(any(ParticipantJoinedEvent.class));
    }
}
