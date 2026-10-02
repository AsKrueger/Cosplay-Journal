package com.cosplayjournal.infrastructure;

import com.cosplayjournal.application.dto.EventSearchCriteria;
import com.cosplayjournal.application.dto.PageResult;
import com.cosplayjournal.domain.model.Cosplay;
import com.cosplayjournal.domain.model.event.Event;
import com.cosplayjournal.domain.model.event.EventId;
import com.cosplayjournal.domain.model.event.EventLocation;
import com.cosplayjournal.domain.model.event.EventSource;
import com.cosplayjournal.domain.model.user.User;
import com.cosplayjournal.domain.model.user.UserId;
import com.cosplayjournal.infrastructure.adapter.out.persistence.adapter.JpaCosplayRepositoryAdapter;
import com.cosplayjournal.infrastructure.adapter.out.persistence.adapter.JpaEventRepositoryAdapter;
import com.cosplayjournal.infrastructure.adapter.out.persistence.adapter.JpaUserRepositoryAdapter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class PostgresFlywayRepositoryIntegrationTest extends AbstractTestcontainersIntegrationTest {

    @Autowired
    private JpaUserRepositoryAdapter userRepositoryAdapter;

    @Autowired
    private JpaEventRepositoryAdapter eventRepositoryAdapter;

    @Autowired
    private JpaCosplayRepositoryAdapter cosplayRepositoryAdapter;

    @Test
    @DisplayName("Debe ejecutar todas las migraciones Flyway (V1..V5) e interactuar con PostgreSQL 16 real")
    void shouldExecuteFlywayMigrationsAndPersistEntitiesInRealPostgres() {
        // 1. Guardar Usuario en PostgreSQL 16
        UserId userId = UserId.of("usr-tc-001");
        User user = User.create(userId, "cosplayer_postgres", "postgres@example.com", "$2a$10$hash");
        userRepositoryAdapter.save(user);

        Optional<User> foundUser = userRepositoryAdapter.findByEmail("postgres@example.com");
        assertTrue(foundUser.isPresent());
        assertEquals("cosplayer_postgres", foundUser.get().getUsername());

        // 2. Guardar Cosplay
        Cosplay cosplay = Cosplay.createNew("Goku SSJ", "Traje gi naranja", "Goku", "Dragon Ball", userId);
        Cosplay savedCosplay = cosplayRepositoryAdapter.save(cosplay);
        assertNotNull(savedCosplay.getId());

        // 3. Guardar Evento y ejecutar Búsqueda Paginada en PostgreSQL
        Event event = Event.create(
                EventId.of("evt-tc-100"), "Manga BCN 2026", "Gran evento",
                LocalDate.now().plusDays(10), LocalDate.now().plusDays(12),
                EventLocation.of("Barcelona", "Fira Gran Via"), "https://mangabcn.com", EventSource.MANUAL_ADMIN
        );
        eventRepositoryAdapter.save(event);

        EventSearchCriteria criteria = new EventSearchCriteria("Barcelona", null, null, null, null, "Manga", 0, 10);
        PageResult<Event> page = eventRepositoryAdapter.search(criteria);

        assertNotNull(page);
        assertEquals(1, page.totalElements());
        assertEquals("Manga BCN 2026", page.content().get(0).getName());
    }
}
