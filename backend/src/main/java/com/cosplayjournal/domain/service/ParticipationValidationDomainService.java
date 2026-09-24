package com.cosplayjournal.domain.service;

import com.cosplayjournal.domain.exception.InvalidParticipationDataException;
import com.cosplayjournal.domain.model.event.Event;
import com.cosplayjournal.domain.model.event.EventStatus;
import com.cosplayjournal.domain.model.participation.Participant;
import com.cosplayjournal.domain.model.participation.ParticipantRole;
import com.cosplayjournal.domain.model.participation.Participation;

public class ParticipationValidationDomainService {

    public void validateParticipationForEvent(Participation participation, Event event) {
        if (participation == null) {
            throw new InvalidParticipationDataException("La participación no puede ser nula");
        }
        if (event == null) {
            throw new InvalidParticipationDataException("El evento de referencia no puede ser nulo");
        }

        if (!participation.getEventId().equals(event.getId())) {
            throw new InvalidParticipationDataException("El ID del evento en la participación (" + participation.getEventId() +
                    ") no coincide con el ID del evento proporcionado (" + event.getId() + ")");
        }

        if (event.getStatus() == EventStatus.CANCELLED) {
            throw new InvalidParticipationDataException("No se puede registrar o modificar una participación para el evento '" +
                    event.getName() + "' porque ha sido CANCELADO");
        }

        validateHasExactlyOneLeader(participation);
    }

    public void validateHasExactlyOneLeader(Participation participation) {
        if (participation == null) return;
        long leaderCount = participation.getParticipants().stream()
                .filter(p -> p.getRole() == ParticipantRole.LEADER)
                .count();

        if (participation.getParticipants().isEmpty()) {
            return; // Permite estado inicial sin participantes antes de añadir el líder
        }

        if (leaderCount == 0) {
            throw new InvalidParticipationDataException("La participación debe contar con al menos un participante con rol LEADER");
        }
        if (leaderCount > 1) {
            throw new InvalidParticipationDataException("La participación no puede tener más de un líder (LEADER)");
        }
    }
}
