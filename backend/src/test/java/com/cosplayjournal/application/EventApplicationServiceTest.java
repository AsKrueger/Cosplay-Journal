package com.cosplayjournal.application;

import com.cosplayjournal.application.port.in.CreateEventCommand;
import com.cosplayjournal.application.port.out.DomainEventPublisherPort;
import com.cosplayjournal.application.port.out.EventRepositoryPort;
import com.cosplayjournal.application.service.EventApplicationService;
import com.cosplayjournal.domain.event.EventCreatedEvent;
import com.cosplayjournal.domain.exception.EventNotFoundException;
import com.cosplayjournal.domain.model.event.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventApplicationServiceTest {

    @Mock
    private EventRepositoryPort eventRepositoryPort;

    @Mock
    private DomainEventPublisherPort eventPublisherPort;

    private EventApplicationService eventApplicationService;

    @BeforeEach
    void setUp() {
        eventApplicationService = new EventApplicationService(eventRepositoryPort, eventPublisherPort);
    }

    @Test
    @DisplayName("Debe crear un evento y publicar EventCreatedEvent")
    void shouldCreateEventAndPublishEvent() {
        CreateEventCommand command = new CreateEventCommand(
                "Expocomic Madrid", "Gran feria",
                LocalDate.now().plusDays(5), LocalDate.now().plusDays(7),
                EventLocation.of("Madrid", "IFEMA"), "https://expocomic.es", EventSource.MANUAL_ADMIN
        );

        when(eventRepositoryPort.save(any(Event.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Event result = eventApplicationService.createEvent(command);

        assertNotNull(result);
        assertEquals("Expocomic Madrid", result.getName());
        verify(eventRepositoryPort, times(1)).save(any(Event.class));
        verify(eventPublisherPort, times(1)).publish(any(EventCreatedEvent.class));
    }

    @Test
    @DisplayName("Debe obtener un evento existente por EventId")
    void shouldGetEventByIdWhenExists() {
        EventId id = EventId.of("evt-555");
        Event event = Event.create(
                id, "Comic Con Valencia", "Desc",
                LocalDate.now(), LocalDate.now().plusDays(1),
                EventLocation.of("Valencia", "Feria Valencia"), "", EventSource.MANUAL_ADMIN
        );

        when(eventRepositoryPort.findById(id)).thenReturn(Optional.of(event));

        Event result = eventApplicationService.getEventById(id);

        assertNotNull(result);
        assertEquals("Comic Con Valencia", result.getName());
    }

    @Test
    @DisplayName("Debe lanzar EventNotFoundException si el evento no existe")
    void shouldThrowExceptionWhenEventNotFound() {
        EventId id = EventId.of("evt-999");
        when(eventRepositoryPort.findById(id)).thenReturn(Optional.empty());

        assertThrows(EventNotFoundException.class, () -> eventApplicationService.getEventById(id));
    }
}
