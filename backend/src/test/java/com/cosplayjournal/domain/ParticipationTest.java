package com.cosplayjournal.domain;

import com.cosplayjournal.domain.exception.InvalidParticipationDataException;
import com.cosplayjournal.domain.model.event.EventId;
import com.cosplayjournal.domain.model.participation.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ParticipationTest {

    @Test
    @DisplayName("Debe crear una participación individual válida")
    void shouldCreateIndividualParticipation() {
        ParticipationId id = ParticipationId.of("part-1");
        EventId eventId = EventId.of("evt-100");
        Long cosplayId = 10L;
        Participant leader = Participant.createLeader("user-1", "Carlos");

        Participation participation = Participation.create(id, eventId, cosplayId, ParticipationType.INDIVIDUAL, leader);

        assertNotNull(participation);
        assertEquals("part-1", participation.getId().value());
        assertEquals("evt-100", participation.getEventId().value());
        assertEquals(10L, participation.getCosplayId());
        assertEquals(ParticipationType.INDIVIDUAL, participation.getType());
        assertEquals(1, participation.getParticipants().size());
        assertEquals(ParticipationStatus.PLANNED, participation.getStatus());
    }

    @Test
    @DisplayName("Debe impedir añadir un segundo participante a una participación INDIVIDUAL")
    void shouldNotAllowMoreThanOneParticipantForIndividual() {
        Participation participation = Participation.create(
                ParticipationId.generate(), EventId.of("evt-1"), 1L, ParticipationType.INDIVIDUAL,
                Participant.createLeader("u1", "Carlos")
        );

        assertThrows(InvalidParticipationDataException.class, () ->
                participation.addParticipant(Participant.create("u2", "María", ParticipantRole.MEMBER))
        );
    }

    @Test
    @DisplayName("Debe permitir hasta 2 participantes en una participación DUO")
    void shouldAllowTwoParticipantsForDuo() {
        Participation participation = Participation.create(
                ParticipationId.generate(), EventId.of("evt-1"), 1L, ParticipationType.DUO,
                Participant.createLeader("u1", "Carlos")
        );

        participation.addParticipant(Participant.create("u2", "María", ParticipantRole.MEMBER));
        assertEquals(2, participation.getParticipants().size());

        assertThrows(InvalidParticipationDataException.class, () ->
                participation.addParticipant(Participant.create("u3", "Juan", ParticipantRole.MEMBER))
        );
    }

    @Test
    @DisplayName("Debe validar cantidad de participantes para confirmar una participación GROUP (mínimo 3)")
    void shouldValidateMinParticipantsWhenConfirmingGroup() {
        Participation participation = Participation.create(
                ParticipationId.generate(), EventId.of("evt-1"), 1L, ParticipationType.GROUP,
                Participant.createLeader("u1", "Leader")
        );

        participation.addParticipant(Participant.create("u2", "Member 2", ParticipantRole.MEMBER));

        // Con solo 2 participantes no se puede confirmar un GROUP
        assertThrows(InvalidParticipationDataException.class, () ->
                participation.changeStatus(ParticipationStatus.CONFIRMED)
        );

        // Añadimos el 3er participante
        participation.addParticipant(Participant.create("u3", "Member 3", ParticipantRole.MEMBER));

        // Ahora sí se puede confirmar
        participation.changeStatus(ParticipationStatus.CONFIRMED);
        assertEquals(ParticipationStatus.CONFIRMED, participation.getStatus());
    }

    @Test
    @DisplayName("Debe asignar personajes a participantes dentro de la participación")
    void shouldAssignCharacterToParticipantInParticipation() {
        Participation participation = Participation.create(
                ParticipationId.generate(), EventId.of("evt-1"), 1L, ParticipationType.INDIVIDUAL,
                Participant.createLeader("u1", "Carlos")
        );

        participation.assignCharacterToParticipant("u1", "Spider-Man");

        assertEquals("Spider-Man", participation.getParticipants().get(0).getAssignedCharacter());
    }
}
