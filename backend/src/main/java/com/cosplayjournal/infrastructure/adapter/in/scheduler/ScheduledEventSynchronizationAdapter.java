package com.cosplayjournal.infrastructure.adapter.in.scheduler;

import com.cosplayjournal.application.dto.ImportEventsResult;
import com.cosplayjournal.application.port.in.ImportExternalEventsUseCase;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class ScheduledEventSynchronizationAdapter {

    private static final Logger log = LoggerFactory.getLogger(ScheduledEventSynchronizationAdapter.class);

    private final ImportExternalEventsUseCase importExternalEventsUseCase;
    private final boolean enabled;
    private final AtomicBoolean isSyncRunning = new AtomicBoolean(false);

    private final Counter createdCounter;
    private final Counter updatedCounter;
    private final Counter skippedCounter;
    private final Counter failedCounter;

    public ScheduledEventSynchronizationAdapter(
            ImportExternalEventsUseCase importExternalEventsUseCase,
            @Value("${cosplay-journal.events.sync.enabled:true}") boolean enabled,
            MeterRegistry meterRegistry
    ) {
        this.importExternalEventsUseCase = importExternalEventsUseCase;
        this.enabled = enabled;

        this.createdCounter = meterRegistry.counter("cosplay_journal_event_sync_created_total");
        this.updatedCounter = meterRegistry.counter("cosplay_journal_event_sync_updated_total");
        this.skippedCounter = meterRegistry.counter("cosplay_journal_event_sync_skipped_total");
        this.failedCounter = meterRegistry.counter("cosplay_journal_event_sync_failed_total");
    }

    @Scheduled(cron = "${cosplay-journal.events.sync.cron:0 0 */6 * * *}")
    public void scheduledSync() {
        if (!enabled) {
            log.debug("Sincronización automática deshabilitada por configuración (enabled=false)");
            return;
        }

        if (!isSyncRunning.compareAndSet(false, true)) {
            log.warn("Omitida sincronización automática porque ya hay otra ejecución en curso en esta instancia");
            return;
        }

        long startTime = System.currentTimeMillis();
        try {
            log.info("Iniciando tarea programada de sincronización de eventos de ListadoManga...");
            ImportEventsResult result = importExternalEventsUseCase.importEvents();
            long duration = System.currentTimeMillis() - startTime;

            createdCounter.increment(result.created());
            updatedCounter.increment(result.updated());
            skippedCounter.increment(result.skipped());
            failedCounter.increment(result.failed());

            log.info("Sincronización programada finalizada con éxito. [Total: {}, Creados: {}, Actualizados: {}, Omitidos: {}, Fallidos: {}, Duración: {}ms]",
                    result.totalFound(), result.created(), result.updated(), result.skipped(), result.failed(), duration);
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            failedCounter.increment();
            log.error("Error no controlado durante la sincronización programada de eventos tras {}ms", duration, e);
        } finally {
            isSyncRunning.set(false);
        }
    }

    public boolean isSyncRunning() {
        return isSyncRunning.get();
    }
}
