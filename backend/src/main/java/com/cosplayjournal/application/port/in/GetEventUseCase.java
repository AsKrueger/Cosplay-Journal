package com.cosplayjournal.application.port.in;

import com.cosplayjournal.domain.model.event.Event;
import com.cosplayjournal.domain.model.event.EventId;

import java.util.List;

public interface GetEventUseCase {
    Event getEventById(EventId id);
    List<Event> getAllEvents();
}
