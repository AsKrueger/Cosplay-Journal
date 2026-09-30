package com.cosplayjournal.application.service;

import com.cosplayjournal.application.port.in.*;
import com.cosplayjournal.application.port.out.CosplayRepositoryPort;
import com.cosplayjournal.application.port.out.DomainEventPublisherPort;
import com.cosplayjournal.application.port.out.EventRepositoryPort;
import com.cosplayjournal.application.port.out.ParticipationRepositoryPort;
import com.cosplayjournal.domain.event.ParticipantJoinedEvent;
import com.cosplayjournal.domain.event.ParticipationCreatedEvent;
import com.cosplayjournal.domain.exception.CosplayNotFoundException;
import com.cosplayjournal.domain.exception.EventNotFoundException;
import com.cosplayjournal.domain.exception.ParticipationNotFoundException;
import com.cosplayjournal.domain.model.Cosplay;
import com.cosplayjournal.domain.model.event.Event;
import com.cosplayjournal.domain.model.participation.*;
import com.cosplayjournal.domain.service.ParticipationValidationDomainService;

import java.util.List;

public class ParticipationApplicationService implements
        CreateParticipationUseCase,
        GetParticipationUseCase,
        JoinParticipationUseCase,
        LeaveParticipationUseCase,
        AssignCharacterUseCase {

    private final ParticipationRepositoryPort participationRepositoryPort;
    private final EventRepositoryPort eventRepositoryPort;
    private final CosplayRepositoryPort cosplayRepositoryPort;
    private final DomainEventPublisherPort eventPublisherPort;
    private final ParticipationValidationDomainService validationDomainService;

    public ParticipationApplicationService(
            ParticipationRepositoryPort participationRepositoryPort,
            EventRepositoryPort eventRepositoryPort,
            CosplayRepositoryPort cosplayRepositoryPort,
            DomainEventPublisherPort eventPublisherPort,
            ParticipationValidationDomainService validationDomainService
    ) {
        this.participationRepositoryPort = participationRepositoryPort;
        this.eventRepositoryPort = eventRepositoryPort;
        this.cosplayRepositoryPort = cosplayRepositoryPort;
        this.eventPublisherPort = eventPublisherPort;
        this.validationDomainService = validationDomainService != null ? validationDomainService : new ParticipationValidationDomainService();
    }

    public ParticipationApplicationService(
            ParticipationRepositoryPort participationRepositoryPort,
            EventRepositoryPort eventRepositoryPort,
            CosplayRepositoryPort cosplayRepositoryPort
    ) {
        this(participationRepositoryPort, eventRepositoryPort, cosplayRepositoryPort, event -> {}, new ParticipationValidationDomainService());
    }

    @Override
    public Participation createParticipation(CreateParticipationCommand command) {
        Event event = eventRepositoryPort.findById(command.eventId())
                .orElseThrow(() -> new EventNotFoundException(command.eventId()));

        Cosplay cosplay = cosplayRepositoryPort.findById(command.cosplayId())
                .orElseThrow(() -> new CosplayNotFoundException(command.cosplayId()));

        Participant leader = Participant.createLeader(command.leaderUserId(), command.leaderName());
        ParticipationId newId = ParticipationId.generate();

        Participation participation = Participation.create(newId, event.getId(), cosplay.getId(), command.type(), leader);
        if (command.groupName() != null && !command.groupName().trim().isEmpty()) {
            participation.setGroupName(command.groupName());
        }

        validationDomainService.validateParticipationForEvent(participation, event);

        Participation saved = participationRepositoryPort.save(participation);
        eventPublisherPort.publish(new ParticipationCreatedEvent(saved.getId().value(), saved.getEventId().value(), saved.getCosplayId(), saved.getType()));
        eventPublisherPort.publish(new ParticipantJoinedEvent(saved.getId().value(), leader.getUserId(), leader.getRole()));

        return saved;
    }

    @Override
    public Participation getParticipationById(ParticipationId id) {
        return participationRepositoryPort.findById(id)
                .orElseThrow(() -> new ParticipationNotFoundException(id));
    }

    @Override
    public List<Participation> getAllParticipations() {
        return participationRepositoryPort.findAll();
    }

    @Override
    public Participation joinParticipation(JoinParticipationCommand command) {
        Participation participation = getParticipationById(command.participationId());
        Participant newParticipant = Participant.create(command.userId(), command.name(), command.role());

        participation.addParticipant(newParticipant);

        Event event = eventRepositoryPort.findById(participation.getEventId()).orElse(null);
        if (event != null) {
            validationDomainService.validateParticipationForEvent(participation, event);
        }

        Participation updated = participationRepositoryPort.save(participation);
        eventPublisherPort.publish(new ParticipantJoinedEvent(updated.getId().value(), newParticipant.getUserId(), newParticipant.getRole()));

        return updated;
    }

    @Override
    public Participation leaveParticipation(LeaveParticipationCommand command) {
        Participation participation = getParticipationById(command.participationId());
        participation.removeParticipant(command.userId());

        return participationRepositoryPort.save(participation);
    }

    @Override
    public Participation assignCharacter(AssignCharacterCommand command) {
        Participation participation = getParticipationById(command.participationId());
        participation.assignCharacterToParticipant(command.userId(), command.characterName());

        return participationRepositoryPort.save(participation);
    }
}
