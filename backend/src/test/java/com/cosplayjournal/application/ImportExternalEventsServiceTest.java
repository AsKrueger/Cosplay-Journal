package com.cosplayjournal.application;

import com.cosplayjournal.application.dto.ExternalEventData;
import com.cosplayjournal.application.dto.ImportEventsResult;
import com.cosplayjournal.application.port.out.DomainEventPublisherPort;
import com.cosplayjournal.application.port.out.EventRepositoryPort;
import com.cosplayjournal.application.port.out.ExternalEventSourcePort;
import com.cosplayjournal.application.service.ImportExternalEventsService;
import com.cosplayjournal.domain.event.EventCreatedEvent;
import com.cosplayjournal.domain.model.event.Event;
import com.cosplayjournal.domain.model.event.EventId;
import com.cosplayjournal.domain.model.event.EventLocation;
import com.cosplayjournal.domain.model.event.EventSource;
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
class ImportExternalEventsServiceTest {

    @Mock
    private ExternalEventSourcePort externalEventSourcePort;

    @Mock
    private EventRepositoryPort eventRepositoryPort;

    @Mock
    private DomainEventPublisherPort eventPublisherPort;

    private ImportExternalEventsService service;

    @BeforeEach
    void setUp() {
        service = new ImportExternalEventsService(externalEventSourcePort, eventRepositoryPort, eventPublisherPort);
    }

    @Test
    @DisplayName("Debe importar eventos externos nuevos e ignorar duplicados de forma idempotente")
    void shouldImportNewEventsAndSkipDuplicates() {
        ExternalEventData event1 = new ExternalEventData(
                "lm-101", "Japan Weekend Madrid", "Desc",
                LocalDate.now().plusDays(10), LocalDate.now().plusDays(12),
                "Madrid", "IFEMA", "Madrid", "https://japanweekend.com"
        );

        ExternalEventData event2 = new ExternalEventData(
                "lm-102", "Manga Fest Sevilla", "Desc",
                LocalDate.now().plusDays(30), LocalDate.now().plusDays(32),
                "Sevilla", "Fibes", "Sevilla", "https://mangafest.com"
        );

        when(externalEventSourcePort.fetchEvents()).thenReturn(List.of(event1, event2));

        // event1 no existe -> se crea
        when(eventRepositoryPort.findBySourceAndExternalId(EventSource.LISTADOMANGA, "lm-101"))
                .thenReturn(Optional.empty());

        // event2 ya existe -> se omite
        Event existingEvent2 = Event.createWithExternalId(
                EventId.of("evt-existing-2"), "lm-102", "Manga Fest Sevilla", "Desc",
                LocalDate.now().plusDays(30), LocalDate.now().plusDays(32),
                EventLocation.of("Sevilla", "Fibes"), "https://mangafest.com", EventSource.LISTADOMANGA
        );
        when(eventRepositoryPort.findBySourceAndExternalId(EventSource.LISTADOMANGA, "lm-102"))
                .thenReturn(Optional.of(existingEvent2));

        when(eventRepositoryPort.save(any(Event.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ImportEventsResult result = service.importEvents();

        assertNotNull(result);
        assertEquals(2, result.totalFound());
        assertEquals(1, result.created());
        assertEquals(1, result.skipped());
        assertEquals(0, result.failed());

        verify(eventPublisherPort, times(1)).publish(any(EventCreatedEvent.class));
    }
}
