package com.cosplayjournal.domain.exception;

import com.cosplayjournal.domain.model.participation.ParticipationId;

public class ParticipationNotFoundException extends RuntimeException {

    public ParticipationNotFoundException(ParticipationId id) {
        super("Participación con ID " + (id != null ? id.value() : "null") + " no fue encontrada");
    }

    public ParticipationNotFoundException(String id) {
        super("Participación con ID " + id + " no fue encontrada");
    }
}
