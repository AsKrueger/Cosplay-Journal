package com.cosplayjournal.domain;

import com.cosplayjournal.domain.exception.InvalidUserDataException;
import com.cosplayjournal.domain.model.social.UserEventStatus;
import com.cosplayjournal.domain.model.user.UserId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class UserEventStatusTest {

    private final UserId ana = UserId.of("ana");

    @ParameterizedTest
    @ValueSource(strings = {"ATTENDING", "ATTENDED", "INTERESTED", "PLANNING"})
    @DisplayName("Acepta los cuatro estados de participación que se pueden guardar")
    void acceptsStorableStatuses(String status) {
        UserEventStatus value = new UserEventStatus(ana, "evt-1", status, null);

        assertEquals(status, value.status());
        assertTrue(UserEventStatus.isStorable(status));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"NONE", "attending", "GOING"})
    @DisplayName("Rechaza estados nulos, vacíos, en minúsculas o desconocidos")
    void rejectsUnknownStatuses(String status) {
        assertThrows(InvalidUserDataException.class,
                () -> new UserEventStatus(ana, "evt-1", status, null));
        assertFalse(UserEventStatus.isStorable(status));
    }

    @Test
    @DisplayName("El usuario es obligatorio")
    void requiresUser() {
        assertThrows(InvalidUserDataException.class,
                () -> new UserEventStatus(null, "evt-1", "ATTENDING", null));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    @DisplayName("El identificador del evento no puede ser nulo ni estar en blanco")
    void requiresEventId(String eventId) {
        assertThrows(InvalidUserDataException.class,
                () -> new UserEventStatus(ana, eventId, "ATTENDING", null));
    }

    @Test
    @DisplayName("El identificador del evento admite hasta 150 caracteres")
    void eventIdMaxLength() {
        assertDoesNotThrow(() -> new UserEventStatus(ana, "e".repeat(150), "ATTENDING", null));
        assertThrows(InvalidUserDataException.class,
                () -> new UserEventStatus(ana, "e".repeat(151), "ATTENDING", null));
    }

    @Test
    @DisplayName("Si no se indica fecha de actualización se usa el instante actual; si se indica, se conserva")
    void updatedAtDefaults() {
        Instant before = Instant.now();
        UserEventStatus generated = new UserEventStatus(ana, "evt-1", "PLANNING", null);
        Instant after = Instant.now();

        assertNotNull(generated.updatedAt());
        assertFalse(generated.updatedAt().isBefore(before));
        assertFalse(generated.updatedAt().isAfter(after));

        Instant fixed = Instant.parse("2026-05-01T10:00:00Z");
        assertEquals(fixed, new UserEventStatus(ana, "evt-1", "PLANNING", fixed).updatedAt());
    }
}
