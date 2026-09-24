package com.cosplayjournal.domain;

import com.cosplayjournal.domain.exception.InvalidParticipationDataException;
import com.cosplayjournal.domain.model.participation.Participant;
import com.cosplayjournal.domain.model.participation.ParticipantRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ParticipantTest {

    @Test
    @DisplayName("Debe crear un participante con datos válidos")
    void shouldCreateParticipant() {
        Participant participant = Participant.createLeader("user-101", "Alonso");

        assertEquals("user-101", participant.getUserId());
        assertEquals("Alonso", participant.getName());
        assertEquals(ParticipantRole.LEADER, participant.getRole());
        assertEquals("", participant.getAssignedCharacter());
    }

    @Test
    @DisplayName("Debe asignar un personaje a un participante")
    void shouldAssignCharacterToParticipant() {
        Participant participant = Participant.create("user-102", "Elena", ParticipantRole.MEMBER);
        participant.assignCharacter("Spider-Gwen");

        assertEquals("Spider-Gwen", participant.getAssignedCharacter());
    }

    @Test
    @DisplayName("Debe lanzar excepción si userId o name son vacíos")
    void shouldThrowExceptionWhenUserIdOrNameIsInvalid() {
        assertThrows(InvalidParticipationDataException.class, () -> Participant.create("", "Elena", ParticipantRole.MEMBER));
        assertThrows(InvalidParticipationDataException.class, () -> Participant.create("user-1", "", ParticipantRole.MEMBER));
    }
}
