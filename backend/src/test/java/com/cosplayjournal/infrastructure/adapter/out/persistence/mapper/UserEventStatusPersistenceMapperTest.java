package com.cosplayjournal.infrastructure.adapter.out.persistence.mapper;

import com.cosplayjournal.domain.exception.InvalidUserDataException;
import com.cosplayjournal.domain.model.social.UserEventStatus;
import com.cosplayjournal.domain.model.user.UserId;
import com.cosplayjournal.infrastructure.adapter.out.persistence.entity.UserEventStatusJpaEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class UserEventStatusPersistenceMapperTest {

    @Test
    @DisplayName("La clave combina usuario y evento separados por '|'")
    void shouldBuildCompositeKey() {
        assertEquals("user-1|evt-9", UserEventStatusPersistenceMapper.key("user-1", "evt-9"));
    }

    @Test
    @DisplayName("Debe mapear el estado a entidad JPA usando la clave compuesta como id")
    void shouldMapDomainToEntity() {
        Instant updated = Instant.parse("2026-03-01T12:00:00Z");
        UserEventStatus status = new UserEventStatus(UserId.of("user-1"), "evt-9", "ATTENDING", updated);

        UserEventStatusJpaEntity entity = UserEventStatusPersistenceMapper.toJpaEntity(status);

        assertEquals("user-1|evt-9", entity.getId());
        assertEquals("user-1", entity.getUserId());
        assertEquals("evt-9", entity.getEventId());
        assertEquals("ATTENDING", entity.getStatus());
        assertEquals(updated, entity.getUpdatedAt());
    }

    @Test
    @DisplayName("Debe reconstruir el dominio desde la entidad JPA")
    void shouldMapEntityToDomain() {
        Instant updated = Instant.parse("2026-03-02T08:15:00Z");
        UserEventStatusJpaEntity entity = new UserEventStatusJpaEntity("user-1|evt-9", "user-1", "evt-9", "ATTENDED", updated);

        UserEventStatus status = UserEventStatusPersistenceMapper.toDomain(entity);

        assertEquals(UserId.of("user-1"), status.userId());
        assertEquals("evt-9", status.eventId());
        assertEquals("ATTENDED", status.status());
        assertEquals(updated, status.updatedAt());
    }

    @Test
    @DisplayName("Ida y vuelta dominio -> entidad -> dominio devuelve un valor igual")
    void shouldRoundTrip() {
        UserEventStatus original = new UserEventStatus(UserId.of("user-2"), "evt-3", "INTERESTED",
                Instant.parse("2026-04-05T18:00:00Z"));

        UserEventStatus restored = UserEventStatusPersistenceMapper.toDomain(
                UserEventStatusPersistenceMapper.toJpaEntity(original));

        assertEquals(original, restored);
    }

    @Test
    @DisplayName("Un estado guardado que ya no es válido se rechaza al leerlo")
    void shouldRejectInvalidStoredStatus() {
        UserEventStatusJpaEntity entity = new UserEventStatusJpaEntity("user-1|evt-9", "user-1", "evt-9", "NONE",
                Instant.now());

        assertThrows(InvalidUserDataException.class, () -> UserEventStatusPersistenceMapper.toDomain(entity));
    }
}
