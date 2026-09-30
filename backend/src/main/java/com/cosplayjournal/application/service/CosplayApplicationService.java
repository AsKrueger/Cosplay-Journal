package com.cosplayjournal.application.service;

import com.cosplayjournal.application.port.in.*;
import com.cosplayjournal.application.port.out.CosplayRepositoryPort;
import com.cosplayjournal.application.port.out.DomainEventPublisherPort;
import com.cosplayjournal.domain.event.CosplayCreatedEvent;
import com.cosplayjournal.domain.event.CosplayStatusChangedEvent;
import com.cosplayjournal.domain.exception.CosplayNotFoundException;
import com.cosplayjournal.domain.model.Cosplay;
import com.cosplayjournal.domain.model.CosplayStatus;

import java.util.List;

public class CosplayApplicationService implements CreateCosplayUseCase, GetCosplayUseCase, ChangeCosplayStatusUseCase {

    private final CosplayRepositoryPort cosplayRepositoryPort;
    private final DomainEventPublisherPort eventPublisherPort;

    public CosplayApplicationService(CosplayRepositoryPort cosplayRepositoryPort, DomainEventPublisherPort eventPublisherPort) {
        this.cosplayRepositoryPort = cosplayRepositoryPort;
        this.eventPublisherPort = eventPublisherPort;
    }

    public CosplayApplicationService(CosplayRepositoryPort cosplayRepositoryPort) {
        this(cosplayRepositoryPort, event -> {}); // Default no-op for backward compatibility in unit tests
    }

    @Override
    public Cosplay createCosplay(CreateCosplayCommand command) {
        Cosplay cosplay = Cosplay.createNew(
                command.name(),
                command.description(),
                command.characterName(),
                command.originSeries()
        );
        Cosplay saved = cosplayRepositoryPort.save(cosplay);
        eventPublisherPort.publish(new CosplayCreatedEvent(saved.getId(), saved.getName()));
        return saved;
    }

    @Override
    public Cosplay getCosplayById(Long id) {
        return cosplayRepositoryPort.findById(id)
                .orElseThrow(() -> new CosplayNotFoundException(id));
    }

    @Override
    public List<Cosplay> getAllCosplays() {
        return cosplayRepositoryPort.findAll();
    }

    @Override
    public Cosplay changeCosplayStatus(ChangeCosplayStatusCommand command) {
        Cosplay cosplay = getCosplayById(command.cosplayId());
        CosplayStatus previousStatus = cosplay.getStatus();

        cosplay.changeStatus(command.newStatus());
        Cosplay updated = cosplayRepositoryPort.save(cosplay);

        eventPublisherPort.publish(new CosplayStatusChangedEvent(updated.getId(), previousStatus, updated.getStatus()));
        return updated;
    }
}
