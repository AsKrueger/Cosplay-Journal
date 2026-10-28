package com.cosplayjournal.application;

import com.cosplayjournal.application.dto.ImportEventsResult;
import com.cosplayjournal.application.port.out.DomainEventPublisherPort;
import com.cosplayjournal.application.port.out.EventRepositoryPort;
import com.cosplayjournal.application.port.out.ExternalEventSourcePort;
import com.cosplayjournal.application.service.ImportExternalEventsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ImportExternalEventsRefreshTest {

    @Mock
    private ExternalEventSourcePort externalEventSourcePort;

    @Mock
    private EventRepositoryPort eventRepositoryPort;

    @Mock
    private DomainEventPublisherPort eventPublisherPort;

    private MutableClock clock;
    private ImportExternalEventsService service;

    @BeforeEach
    void setUp() {
        clock = new MutableClock(Instant.parse("2026-10-28T10:00:00Z"));
        service = new ImportExternalEventsService(externalEventSourcePort, eventRepositoryPort, eventPublisherPort, clock);
    }

    @Test
    @DisplayName("Dentro de 30 minutos no vuelve a consultar la fuente y devuelve el resultado anterior")
    void shouldNotQuerySourceAgainWithinInterval() {
        when(externalEventSourcePort.fetchEvents()).thenReturn(List.of());

        ImportEventsResult first = service.refreshIfStale();
        clock.advance(Duration.ofMinutes(29));
        ImportEventsResult second = service.refreshIfStale();

        verify(externalEventSourcePort, times(1)).fetchEvents();
        assertEquals(first, second);
    }

    @Test
    @DisplayName("Pasados 30 minutos vuelve a sincronizar con la fuente")
    void shouldResyncAfterInterval() {
        when(externalEventSourcePort.fetchEvents()).thenReturn(List.of());

        service.refreshIfStale();
        clock.advance(Duration.ofMinutes(30));
        service.refreshIfStale();

        verify(externalEventSourcePort, times(2)).fetchEvents();
    }

    /** Reloj controlable para probar el intervalo sin esperar. */
    private static final class MutableClock extends Clock {
        private Instant now;

        MutableClock(Instant start) {
            this.now = start;
        }

        void advance(Duration duration) {
            now = now.plus(duration);
        }

        @Override
        public ZoneId getZone() {
            return ZoneId.of("UTC");
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return now;
        }
    }
}
