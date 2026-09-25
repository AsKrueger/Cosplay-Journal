package com.cosplayjournal.infrastructure.adapter.out.persistence;

import com.cosplayjournal.domain.model.Cosplay;
import com.cosplayjournal.domain.model.event.Event;
import com.cosplayjournal.domain.model.event.EventId;
import com.cosplayjournal.domain.model.event.EventLocation;
import com.cosplayjournal.domain.model.event.EventSource;
import com.cosplayjournal.domain.model.participation.*;
import com.cosplayjournal.infrastructure.adapter.out.persistence.adapter.JpaCosplayRepositoryAdapter;
import com.cosplayjournal.infrastructure.adapter.out.persistence.adapter.JpaEventRepositoryAdapter;
import com.cosplayjournal.infrastructure.adapter.out.persistence.adapter.JpaParticipationRepositoryAdapter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class ParticipationJpaAdapterIntegrationTest {

    @Autowired
    private JpaParticipationRepositoryAdapter participationRepositoryAdapter;

    @Autowired
    private JpaEventRepositoryAdapter eventRepositoryAdapter;

    @Autowired
    private JpaCosplayRepositoryAdapter cosplayRepositoryAdapter;

    @Test
    @DisplayName("Debe guardar y recuperar una Participation con sus Participants en la base de datos relacional")
    void shouldSaveAndFindParticipationInDatabase() {
        // Guardamos dependencias de claves foráneas
        Event event = Event.create(
                EventId.of("evt-part-1"), "Comic Con", "Desc",
                LocalDate.now(), LocalDate.now().plusDays(1),
                EventLocation.of("Valencia", "Feria Valencia"), "", EventSource.MANUAL_ADMIN
        );
        eventRepositoryAdapter.save(event);

        Cosplay cosplay = cosplayRepositoryAdapter.save(
                Cosplay.createNew("Goku SSJ", "Traje naranja", "Goku", "Dragon Ball")
        );

        ParticipationId partId = ParticipationId.of("part-test-1");
        Participant leader = Participant.createLeader("user-leader-1", "Alonso");
        leader.assignCharacter("Goku");

        Participation participation = Participation.create(
                partId, event.getId(), cosplay.getId(), ParticipationType.DUO, leader
        );
        participation.addParticipant(Participant.create("user-member-2", "Beatriz", ParticipantRole.MEMBER));

        Participation saved = participationRepositoryAdapter.save(participation);

        assertNotNull(saved);
        assertEquals("part-test-1", saved.getId().value());
        assertEquals(2, saved.getParticipants().size());

        Optional<Participation> found = participationRepositoryAdapter.findById(partId);
        assertTrue(found.isPresent());
        assertEquals(2, found.get().getParticipants().size());
        assertEquals("Goku", found.get().getParticipants().get(0).getAssignedCharacter());
    }
}
