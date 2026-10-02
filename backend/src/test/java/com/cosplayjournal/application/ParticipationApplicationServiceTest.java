package com.cosplayjournal.application;

import com.cosplayjournal.application.port.in.*;
import com.cosplayjournal.application.port.out.*;
import com.cosplayjournal.application.service.ParticipationApplicationService;
import com.cosplayjournal.domain.event.ParticipantJoinedEvent;
import com.cosplayjournal.domain.event.ParticipationCreatedEvent;
import com.cosplayjournal.domain.exception.EventNotFoundException;
import com.cosplayjournal.domain.exception.ForbiddenAccessException;
import com.cosplayjournal.domain.model.Cosplay;
import com.cosplayjournal.domain.model.event.*;
import com.cosplayjournal.domain.model.participation.*;
import com.cosplayjournal.domain.model.user.UserId;
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

    @Mock
    private CurrentUserPort currentUserPort;

    private ParticipationApplicationService participationApplicationService;

    @BeforeEach
    void setUp() {
        participationApplicationService = new ParticipationApplicationService(
                participationRepositoryPort, eventRepositoryPort, cosplayRepositoryPort,
                eventPublisherPort, currentUserPort, new ParticipationValidationDomainService()
        );
    }

    @Test
    @DisplayName("Debe crear una participación asignando creatorId del usuario autenticado")
    void shouldCreateParticipationSuccessfully() {
        EventId eventId = EventId.of("evt-1");
        Long cosplayId = 10L;
        UserId userId = UserId.of("user-leader");

        when(currentUserPort.getRequiredCurrentUserId()).thenReturn(userId);

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
        assertEquals(userId, result.getCreatorId());
        assertEquals(1, result.getParticipants().size());

        verify(eventPublisherPort, times(1)).publish(any(ParticipationCreatedEvent.class));
        verify(eventPublisherPort, times(1)).publish(any(ParticipantJoinedEvent.class));
    }

    @Test
    @DisplayName("Debe lanzar EventNotFoundException si el evento no existe al crear participación")
    void shouldThrowExceptionWhenEventDoesNotExist() {
        EventId eventId = EventId.of("evt-999");
        when(currentUserPort.getRequiredCurrentUserId()).thenReturn(UserId.of("u1"));
        when(eventRepositoryPort.findById(eventId)).thenReturn(Optional.empty());

        CreateParticipationCommand command = new CreateParticipationCommand(
                eventId, 10L, ParticipationType.INDIVIDUAL, "u1", "Alonso", ""
        );

        assertThrows(EventNotFoundException.class, () -> participationApplicationService.createParticipation(command));
    }

    @Test
    @DisplayName("Debe lanzar ForbiddenAccessException si un usuario intenta unir a otro usuario sin ser creador ni admin")
    void shouldThrowExceptionWhenUnauthorizedUserTriesToJoinAnother() {
        ParticipationId participationId = ParticipationId.of("part-100");
        Participation participation = Participation.create(
                participationId, UserId.of("creator-user"), EventId.of("evt-1"), 10L, ParticipationType.GROUP, Participant.createLeader("creator-user", "Alonso")
        );

        when(participationRepositoryPort.findById(participationId)).thenReturn(Optional.of(participation));
        when(currentUserPort.getRequiredCurrentUserId()).thenReturn(UserId.of("stranger-user"));
        when(currentUserPort.isAdmin()).thenReturn(false);

        JoinParticipationCommand command = new JoinParticipationCommand(participationId, "other-user", "Beatriz", ParticipantRole.MEMBER);

        assertThrows(ForbiddenAccessException.class, () -> participationApplicationService.joinParticipation(command));
    }
}
