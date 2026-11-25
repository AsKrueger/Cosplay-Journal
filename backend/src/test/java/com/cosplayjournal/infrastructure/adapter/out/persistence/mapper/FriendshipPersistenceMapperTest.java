package com.cosplayjournal.infrastructure.adapter.out.persistence.mapper;

import com.cosplayjournal.domain.model.social.Friendship;
import com.cosplayjournal.domain.model.social.FriendshipStatus;
import com.cosplayjournal.domain.model.user.UserId;
import com.cosplayjournal.infrastructure.adapter.out.persistence.entity.FriendshipJpaEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class FriendshipPersistenceMapperTest {

    private static final UserId ANA = UserId.of("user-ana");
    private static final UserId LUIS = UserId.of("user-luis");

    @Test
    @DisplayName("Debe mapear una solicitud pendiente a entidad JPA sin fecha de respuesta")
    void shouldMapPendingFriendshipToEntity() {
        Friendship friendship = Friendship.request(ANA, LUIS);

        FriendshipJpaEntity entity = FriendshipPersistenceMapper.toJpaEntity(friendship);

        assertEquals(friendship.getId(), entity.getId());
        assertEquals("user-ana", entity.getRequesterId());
        assertEquals("user-luis", entity.getAddresseeId());
        assertEquals("PENDING", entity.getStatus());
        assertEquals(friendship.getCreatedAt(), entity.getCreatedAt());
        assertNull(entity.getRespondedAt());
    }

    @Test
    @DisplayName("Debe conservar estado y fecha de respuesta de una amistad aceptada")
    void shouldMapAcceptedFriendshipToEntity() {
        Friendship friendship = Friendship.request(ANA, LUIS);
        friendship.accept(LUIS);

        FriendshipJpaEntity entity = FriendshipPersistenceMapper.toJpaEntity(friendship);

        assertEquals("ACCEPTED", entity.getStatus());
        assertNotNull(entity.getRespondedAt());
        assertEquals(friendship.getRespondedAt(), entity.getRespondedAt());
    }

    @Test
    @DisplayName("Debe reconstruir el dominio desde la entidad JPA")
    void shouldMapEntityToDomain() {
        Instant created = Instant.parse("2026-01-10T10:00:00Z");
        Instant responded = Instant.parse("2026-01-11T09:30:00Z");
        FriendshipJpaEntity entity = new FriendshipJpaEntity("f-1", "user-ana", "user-luis", "ACCEPTED", created, responded);

        Friendship friendship = FriendshipPersistenceMapper.toDomain(entity);

        assertEquals("f-1", friendship.getId());
        assertEquals(ANA, friendship.getRequesterId());
        assertEquals(LUIS, friendship.getAddresseeId());
        assertEquals(FriendshipStatus.ACCEPTED, friendship.getStatus());
        assertTrue(friendship.isAccepted());
        assertEquals(created, friendship.getCreatedAt());
        assertEquals(responded, friendship.getRespondedAt());
    }

    @Test
    @DisplayName("Ida y vuelta dominio -> entidad -> dominio no pierde información")
    void shouldRoundTripWithoutLosingData() {
        Friendship original = Friendship.request(ANA, LUIS);

        Friendship restored = FriendshipPersistenceMapper.toDomain(FriendshipPersistenceMapper.toJpaEntity(original));

        assertEquals(original.getId(), restored.getId());
        assertEquals(original.getRequesterId(), restored.getRequesterId());
        assertEquals(original.getAddresseeId(), restored.getAddresseeId());
        assertEquals(original.getStatus(), restored.getStatus());
        assertEquals(original.getCreatedAt(), restored.getCreatedAt());
        assertEquals(original.getRespondedAt(), restored.getRespondedAt());
    }

    @Test
    @DisplayName("Debe devolver null cuando recibe null")
    void shouldReturnNullForNullInput() {
        assertNull(FriendshipPersistenceMapper.toJpaEntity(null));
        assertNull(FriendshipPersistenceMapper.toDomain(null));
    }

    @Test
    @DisplayName("Debe fallar si la entidad tiene un estado desconocido")
    void shouldFailOnUnknownStatus() {
        FriendshipJpaEntity entity = new FriendshipJpaEntity("f-2", "user-ana", "user-luis", "BLOCKED",
                Instant.now(), null);

        assertThrows(IllegalArgumentException.class, () -> FriendshipPersistenceMapper.toDomain(entity));
    }
}
