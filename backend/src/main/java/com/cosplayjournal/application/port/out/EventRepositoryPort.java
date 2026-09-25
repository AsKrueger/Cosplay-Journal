package com.cosplayjournal.application.port.out;

import com.cosplayjournal.domain.model.event.Event;
import com.cosplayjournal.domain.model.event.EventId;

import java.util.List;
import java.util.Optional;

public interface EventRepositoryPort {
    Event save(Event event);
    Optional<Event> findById(EventId id);
    List<Event> findAll();
}
