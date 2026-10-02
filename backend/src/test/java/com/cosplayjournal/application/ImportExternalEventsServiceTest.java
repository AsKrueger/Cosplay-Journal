package com.cosplayjournal.application;

import com.cosplayjournal.application.dto.ExternalEventData;
import com.cosplayjournal.application.dto.ImportEventsResult;
import com.cosplayjournal.application.port.out.DomainEventPublisherPort;
import com.cosplayjournal.application.port.out.EventRepositoryPort;
import com.cosplayjournal.application.port.out.ExternalEventSourcePort;
import com.cosplayjournal.application.service.ImportExternalEventsService;
import com.cosplayjournal.domain.event.EventCreatedEvent;
import com.cosplayjournal.domain.event.EventUpdatedEvent;
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
    @DisplayName("Debe clasificar correctamente eventos en CREATE, UPDATE y SKIP durante la sincronización")
    void shouldClassifyEventsInCreateUpdateAndSkip() {
        // Evento 1: Nuevo -> CREATE
        ExternalEventData event1 = new ExternalEventData(
                "lm-101", "Japan Weekend Madrid", "Desc",
                LocalDate.now().plusDays(10), LocalDate.now().plusDays(12),
                "Madrid", "IFEMA", "Madrid", "https://japanweekend.com"
        );

        // Evento 2: Existente con datos idénticos -> SKIP
        ExternalEventData event2 = new ExternalEventData(
                "lm-102", "Manga Fest Sevilla", "Evento de cosplay y manga importado desde ListadoManga (Fibes)",
                LocalDate.now().plusDays(30), LocalDate.now().plusDays(32),
                "Sevilla", "Fibes", "Sevilla", "https://mangafest.com"
        );

        // Evento 3: Existente con fechas/lugar modificados -> UPDATE
        ExternalEventData event3 = new ExternalEventData(
                "lm-103", "Comic Con BCN Modificado", "Nueva desc",
                LocalDate.now().plusDays(40), LocalDate.now().plusDays(42),
                "Barcelona", "Fira Gran Via", "Barcelona", "https://comicconbcn.com"
        );

        when(externalEventSourcePort.fetchEvents()).thenReturn(List.of(event1, event2, event3));

        // event1 no existe -> CREATE
        when(eventRepositoryPort.findBySourceAndExternalId(EventSource.LISTADOMANGA, "lm-101"))
                .thenReturn(Optional.empty());

        // event2 existe con mismos datos -> SKIP
        EventLocation location2 = new EventLocation("Sevilla", "Fibes", "Sevilla", "España", "", null, null);
        Event existingEvent2 = Event.createWithExternalId(
                EventId.of("evt-2"), "lm-102", "Manga Fest Sevilla", "Evento de cosplay y manga importado desde ListadoManga (Fibes)",
                LocalDate.now().plusDays(30), LocalDate.now().plusDays(32),
                location2, "https://mangafest.com", EventSource.LISTADOMANGA
        );
        when(eventRepositoryPort.findBySourceAndExternalId(EventSource.LISTADOMANGA, "lm-102"))
                .thenReturn(Optional.of(existingEvent2));

        // event3 existe con datos antiguos -> UPDATE
        EventLocation location3Old = new EventLocation("Barcelona", "Fira Montjuic", "Barcelona", "España", "", null, null);
        Event existingEvent3 = Event.createWithExternalId(
                EventId.of("evt-3"), "lm-103", "Comic Con BCN Antiguo", "Antigua desc",
                LocalDate.now().plusDays(38), LocalDate.now().plusDays(39),
                location3Old, "https://oldbcn.com", EventSource.LISTADOMANGA
        );
        when(eventRepositoryPort.findBySourceAndExternalId(EventSource.LISTADOMANGA, "lm-103"))
                .thenReturn(Optional.of(existingEvent3));

        when(eventRepositoryPort.save(any(Event.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ImportEventsResult result = service.importEvents();

        assertNotNull(result);
        assertEquals(3, result.totalFound());
        assertEquals(1, result.created());
        assertEquals(1, result.updated());
        assertEquals(1, result.skipped());
        assertEquals(0, result.failed());

        verify(eventPublisherPort, times(1)).publish(any(EventCreatedEvent.class));
        verify(eventPublisherPort, times(1)).publish(any(EventUpdatedEvent.class));
    }
}
