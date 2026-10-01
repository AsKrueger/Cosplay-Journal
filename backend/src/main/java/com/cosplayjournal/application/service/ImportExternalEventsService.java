package com.cosplayjournal.application.service;

import com.cosplayjournal.application.dto.ExternalEventData;
import com.cosplayjournal.application.dto.ImportEventsResult;
import com.cosplayjournal.application.port.in.ImportExternalEventsUseCase;
import com.cosplayjournal.application.port.out.DomainEventPublisherPort;
import com.cosplayjournal.application.port.out.EventRepositoryPort;
import com.cosplayjournal.application.port.out.ExternalEventSourcePort;
import com.cosplayjournal.domain.event.EventCreatedEvent;
import com.cosplayjournal.domain.model.event.Event;
import com.cosplayjournal.domain.model.event.EventId;
import com.cosplayjournal.domain.model.event.EventLocation;
import com.cosplayjournal.domain.model.event.EventSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class ImportExternalEventsService implements ImportExternalEventsUseCase {

    private static final Logger log = LoggerFactory.getLogger(ImportExternalEventsService.class);

    private final ExternalEventSourcePort externalEventSourcePort;
    private final EventRepositoryPort eventRepositoryPort;
    private final DomainEventPublisherPort eventPublisherPort;

    public ImportExternalEventsService(
            ExternalEventSourcePort externalEventSourcePort,
            EventRepositoryPort eventRepositoryPort,
            DomainEventPublisherPort eventPublisherPort
    ) {
        this.externalEventSourcePort = externalEventSourcePort;
        this.eventRepositoryPort = eventRepositoryPort;
        this.eventPublisherPort = eventPublisherPort;
    }

    @Override
    public ImportEventsResult importEvents() {
        log.info("Iniciando importación de eventos desde fuente externa ListadoManga...");
        List<ExternalEventData> externalEvents;
        try {
            externalEvents = externalEventSourcePort.fetchEvents();
        } catch (Exception e) {
            log.error("Error al obtener eventos de la fuente externa", e);
            return new ImportEventsResult(0, 0, 0, 0, 1);
        }

        int totalFound = externalEvents.size();
        int created = 0;
        int updated = 0;
        int skipped = 0;
        int failed = 0;

        for (ExternalEventData data : externalEvents) {
            try {
                Optional<Event> existingOpt = eventRepositoryPort.findBySourceAndExternalId(
                        EventSource.LISTADOMANGA, data.externalId()
                );

                if (existingOpt.isPresent()) {
                    skipped++;
                    continue;
                }

                EventLocation location = new EventLocation(
                        data.city() != null ? data.city() : "España",
                        data.venue() != null ? data.venue() : "",
                        data.province() != null ? data.province() : "",
                        "España",
                        "",
                        null,
                        null
                );

                Event event = Event.createWithExternalId(
                        EventId.generate(),
                        data.externalId(),
                        data.name(),
                        data.description(),
                        data.startDate(),
                        data.endDate(),
                        location,
                        data.website(),
                        EventSource.LISTADOMANGA
                );

                Event saved = eventRepositoryPort.save(event);
                eventPublisherPort.publish(new EventCreatedEvent(saved.getId().value(), saved.getName()));
                created++;
            } catch (Exception ex) {
                log.warn("Falló el procesamiento del evento externo ID '{}': {}", data.externalId(), ex.getMessage());
                failed++;
            }
        }

        log.info("Importación completada. Total: {}, Creados: {}, Omitidos: {}, Fallidos: {}", totalFound, created, skipped, failed);
        return new ImportEventsResult(totalFound, created, updated, skipped, failed);
    }
}
