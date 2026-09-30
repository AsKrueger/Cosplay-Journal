package com.cosplayjournal.domain.exception;

import com.cosplayjournal.domain.model.event.EventId;

public class EventNotFoundException extends RuntimeException {

    public EventNotFoundException(EventId id) {
        super("Evento con ID " + (id != null ? id.value() : "null") + " no fue encontrado");
    }

    public EventNotFoundException(String id) {
        super("Evento con ID " + id + " no fue encontrado");
    }
}
