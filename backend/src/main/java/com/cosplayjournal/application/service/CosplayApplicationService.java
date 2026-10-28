package com.cosplayjournal.application.service;

import com.cosplayjournal.application.port.in.CreateCosplayCommand;
import com.cosplayjournal.application.port.in.CreateCosplayUseCase;
import com.cosplayjournal.application.port.in.GetCosplayUseCase;
import com.cosplayjournal.application.port.out.CosplayRepositoryPort;
import com.cosplayjournal.application.port.out.CurrentUserPort;
import com.cosplayjournal.application.port.out.DomainEventPublisherPort;
import com.cosplayjournal.domain.event.CosplayCreatedEvent;
import com.cosplayjournal.domain.exception.CosplayNotFoundException;
import com.cosplayjournal.domain.model.Cosplay;
import com.cosplayjournal.domain.model.user.UserId;

import java.util.List;

public class CosplayApplicationService implements CreateCosplayUseCase, GetCosplayUseCase {

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
        Cosplay saved = cosplayRepositoryPort.save(Cosplay.createNew(command.name(), command.photoUrl(), currentUserId));
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
}
