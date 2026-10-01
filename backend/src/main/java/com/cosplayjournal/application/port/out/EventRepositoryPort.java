package com.cosplayjournal.application.port.out;

import com.cosplayjournal.application.dto.EventSearchCriteria;
import com.cosplayjournal.application.dto.PageResult;
import com.cosplayjournal.domain.model.event.Event;
import com.cosplayjournal.domain.model.event.EventId;
import com.cosplayjournal.domain.model.event.EventSource;

import java.util.List;
import java.util.Optional;

public interface EventRepositoryPort {
    Event save(Event event);
    Optional<Event> findById(EventId id);
    Optional<Event> findBySourceAndExternalId(EventSource source, String externalId);
    List<Event> findAll();
    PageResult<Event> search(EventSearchCriteria criteria);
}
