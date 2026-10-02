package com.cosplayjournal.infrastructure.adapter.in.scheduler;

import com.cosplayjournal.application.dto.ImportEventsResult;
import com.cosplayjournal.application.port.in.ImportExternalEventsUseCase;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScheduledEventSynchronizationAdapterTest {

    @Mock
    private ImportExternalEventsUseCase importExternalEventsUseCase;

    private MeterRegistry meterRegistry;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
    }

    @Test
    @DisplayName("Debe ejecutar la sincronización automática cuando está habilitada (enabled=true) y registrar métricas")
    void shouldExecuteSyncWhenEnabled() {
        ScheduledEventSynchronizationAdapter adapter = new ScheduledEventSynchronizationAdapter(
                importExternalEventsUseCase, true, meterRegistry
        );

        when(importExternalEventsUseCase.importEvents()).thenReturn(new ImportEventsResult(10, 2, 3, 5, 0));

        adapter.scheduledSync();

        verify(importExternalEventsUseCase, times(1)).importEvents();
        assertEquals(2.0, meterRegistry.counter("cosplay_journal_event_sync_created_total").count());
        assertEquals(3.0, meterRegistry.counter("cosplay_journal_event_sync_updated_total").count());
        assertEquals(5.0, meterRegistry.counter("cosplay_journal_event_sync_skipped_total").count());
        assertEquals(0.0, meterRegistry.counter("cosplay_journal_event_sync_failed_total").count());
    }

    @Test
    @DisplayName("No debe ejecutar la sincronización automática cuando está deshabilitada (enabled=false)")
    void shouldNotExecuteSyncWhenDisabled() {
        ScheduledEventSynchronizationAdapter adapter = new ScheduledEventSynchronizationAdapter(
                importExternalEventsUseCase, false, meterRegistry
        );

        adapter.scheduledSync();

        verify(importExternalEventsUseCase, never()).importEvents();
        assertEquals(0.0, meterRegistry.counter("cosplay_journal_event_sync_created_total").count());
    }

    @Test
    @DisplayName("Debe manejar errores adecuadamente e incrementar el contador de fallos sin lanzar excepción")
    void shouldHandleErrorsGracefully() {
        ScheduledEventSynchronizationAdapter adapter = new ScheduledEventSynchronizationAdapter(
                importExternalEventsUseCase, true, meterRegistry
        );

        when(importExternalEventsUseCase.importEvents()).thenThrow(new RuntimeException("Fallo de conexión"));

        assertDoesNotThrow(adapter::scheduledSync);
        assertEquals(1.0, meterRegistry.counter("cosplay_journal_event_sync_failed_total").count());
    }
}
