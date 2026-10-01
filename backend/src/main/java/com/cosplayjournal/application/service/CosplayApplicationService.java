package com.cosplayjournal.application.service;

import com.cosplayjournal.application.port.in.*;
import com.cosplayjournal.application.port.out.CosplayRepositoryPort;
import com.cosplayjournal.application.port.out.CurrentUserPort;
import com.cosplayjournal.application.port.out.DomainEventPublisherPort;
import com.cosplayjournal.domain.event.CosplayCreatedEvent;
import com.cosplayjournal.domain.event.CosplayStatusChangedEvent;
import com.cosplayjournal.domain.exception.CosplayNotFoundException;
import com.cosplayjournal.domain.exception.ForbiddenAccessException;
import com.cosplayjournal.domain.model.Cosplay;
import com.cosplayjournal.domain.model.CosplayStatus;
import com.cosplayjournal.domain.model.user.UserId;

import java.util.List;

public class CosplayApplicationService implements CreateCosplayUseCase, GetCosplayUseCase, ChangeCosplayStatusUseCase {

    private final CosplayRepositoryPort cosplayRepositoryPort;
    private final DomainEventPublisherPort eventPublisherPort;
    private final CurrentUserPort currentUserPort;

    public CosplayApplicationService(
            CosplayRepositoryPort cosplayRepositoryPort,
            DomainEventPublisherPort eventPublisherPort,
            CurrentUserPort currentUserPort
    ) {
        this.cosplayRepositoryPort = cosplayRepositoryPort;
        this.eventPublisherPort = eventPublisherPort;
        this.currentUserPort = currentUserPort;
    }

    public CosplayApplicationService(CosplayRepositoryPort cosplayRepositoryPort) {
        this(cosplayRepositoryPort, event -> {}, new CurrentUserPort() {
            @Override public java.util.Optional<UserId> getCurrentUserId() { return java.util.Optional.of(UserId.of("system-default")); }
            @Override public UserId getRequiredCurrentUserId() { return UserId.of("system-default"); }
            @Override public boolean isAuthenticated() { return true; }
            @Override public boolean isAdmin() { return false; }
        });
    }

    @Override
    public Cosplay createCosplay(CreateCosplayCommand command) {
        UserId currentUserId = currentUserPort.getRequiredCurrentUserId();

        Cosplay cosplay = Cosplay.createNew(
                command.name(),
                command.description(),
                command.characterName(),
                command.originSeries(),
                currentUserId
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
        UserId currentUserId = currentUserPort.getRequiredCurrentUserId();

        if (!cosplay.isOwnedBy(currentUserId) && !currentUserPort.isAdmin()) {
            throw new ForbiddenAccessException("No dispone de permisos para modificar el estado de este cosplay");
        }

        CosplayStatus previousStatus = cosplay.getStatus();
        cosplay.changeStatus(command.newStatus());
        Cosplay updated = cosplayRepositoryPort.save(cosplay);

        eventPublisherPort.publish(new CosplayStatusChangedEvent(updated.getId(), previousStatus, updated.getStatus()));
        return updated;
    }
}
