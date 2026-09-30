package com.cosplayjournal.application.port.in;

import com.cosplayjournal.domain.model.event.Event;

public interface CreateEventUseCase {
    Event createEvent(CreateEventCommand command);
}
