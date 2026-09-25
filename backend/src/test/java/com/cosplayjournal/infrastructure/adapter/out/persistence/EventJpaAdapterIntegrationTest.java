package com.cosplayjournal.infrastructure.adapter.out.persistence;

import com.cosplayjournal.domain.model.event.*;
import com.cosplayjournal.infrastructure.adapter.out.persistence.adapter.JpaEventRepositoryAdapter;
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
class EventJpaAdapterIntegrationTest {

    @Autowired
    private JpaEventRepositoryAdapter eventRepositoryAdapter;

    @Test
    @DisplayName("Debe guardar y recuperar un Event en la base de datos relacional respetando Flyway y Mappers")
    void shouldSaveAndFindEventInDatabase() {
        EventId eventId = EventId.of("evt-test-100");
        Event event = Event.create(
                eventId,
                "Japan Weekend Barcelona",
                "Fira Barcelona Gran Via",
                LocalDate.of(2026, 11, 5),
                LocalDate.of(2026, 11, 7),
                EventLocation.of("Barcelona", "Fira Barcelona"),
                "https://japanweekend.com",
                EventSource.LISTADOMANGA
        );

        Event saved = eventRepositoryAdapter.save(event);

        assertNotNull(saved);
        assertEquals("evt-test-100", saved.getId().value());
        assertEquals("Japan Weekend Barcelona", saved.getName());
        assertEquals("Barcelona", saved.getLocation().city());

        Optional<Event> found = eventRepositoryAdapter.findById(eventId);
        assertTrue(found.isPresent());
        assertEquals("Japan Weekend Barcelona", found.get().getName());
        assertEquals(3, found.get().getDateRange().durationInDays());
    }
}
