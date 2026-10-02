package com.cosplayjournal.application.service;

import com.cosplayjournal.application.dto.EventSearchCriteria;
import com.cosplayjournal.application.dto.PageResult;
import com.cosplayjournal.application.port.in.CreateEventCommand;
import com.cosplayjournal.application.port.in.CreateEventUseCase;
import com.cosplayjournal.application.port.in.GetEventUseCase;
import com.cosplayjournal.application.port.out.DomainEventPublisherPort;
import com.cosplayjournal.application.port.out.EventRepositoryPort;
import com.cosplayjournal.domain.event.EventCreatedEvent;
import com.cosplayjournal.domain.exception.EventNotFoundException;
import com.cosplayjournal.domain.model.event.Event;
import com.cosplayjournal.domain.model.event.EventId;

import java.util.List;

public class EventApplicationService implements CreateEventUseCase, GetEventUseCase {

    private final EventRepositoryPort eventRepositoryPort;
    private final DomainEventPublisherPort eventPublisherPort;

    public EventApplicationService(EventRepositoryPort eventRepositoryPort, DomainEventPublisherPort eventPublisherPort) {
        this.eventRepositoryPort = eventRepositoryPort;
        this.eventPublisherPort = eventPublisherPort;
    }

    public EventApplicationService(EventRepositoryPort eventRepositoryPort) {
        this(eventRepositoryPort, event -> {});
    }

    @Override
    public Event createEvent(CreateEventCommand command) {
        EventId newId = EventId.generate();
        Event event = Event.create(
                newId,
                command.name(),
                command.description(),
                command.startDate(),
                command.endDate(),
                command.location(),
                command.website(),
                command.source()
        );

        Event saved = eventRepositoryPort.save(event);
        eventPublisherPort.publish(new EventCreatedEvent(saved.getId().value(), saved.getName()));
        return saved;
    }

    @Override
    public Event getEventById(EventId id) {
        return eventRepositoryPort.findById(id)
                .orElseThrow(() -> new EventNotFoundException(id));
    }

    @Override
    public List<Event> getAllEvents() {
        return eventRepositoryPort.findAll();
    }

    @Override
    public PageResult<Event> searchEvents(EventSearchCriteria criteria) {
        if (criteria == null) {
            criteria = EventSearchCriteria.defaultCriteria();
        }
        return eventRepositoryPort.search(criteria);
    }
}
