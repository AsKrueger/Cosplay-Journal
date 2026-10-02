package com.cosplayjournal.application.port.in;

import com.cosplayjournal.application.dto.EventSearchCriteria;
import com.cosplayjournal.application.dto.PageResult;
import com.cosplayjournal.domain.model.event.Event;
import com.cosplayjournal.domain.model.event.EventId;

import java.util.List;

public interface GetEventUseCase {
    Event getEventById(EventId id);
    List<Event> getAllEvents();
    PageResult<Event> searchEvents(EventSearchCriteria criteria);
}
