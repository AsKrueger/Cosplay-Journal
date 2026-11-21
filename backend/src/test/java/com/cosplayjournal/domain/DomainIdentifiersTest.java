package com.cosplayjournal.domain;

import com.cosplayjournal.domain.exception.InvalidEventDataException;
import com.cosplayjournal.domain.exception.InvalidParticipationDataException;
import com.cosplayjournal.domain.exception.InvalidUserDataException;
import com.cosplayjournal.domain.model.event.EventId;
import com.cosplayjournal.domain.model.participation.ParticipationId;
import com.cosplayjournal.domain.model.photo.PhotoId;
import com.cosplayjournal.domain.model.user.UserId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Invariantes de los identificadores del dominio (value objects basados en {@code record}):
 * no admiten valores nulos ni en blanco, normalizan espacios, se generan como UUID
 * y se comparan por valor.
 */
class DomainIdentifiersTest {

    @Nested
    @DisplayName("EventId")
    class EventIdTests {

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t"})
        @DisplayName("Rechaza valores nulos o en blanco con InvalidEventDataException")
        void rejectsBlank(String value) {
            assertThrows(InvalidEventDataException.class, () -> EventId.of(value));
        }

        @Test
        @DisplayName("Elimina los espacios de los extremos y se compara por valor")
        void trimsAndComparesByValue() {
            EventId id = EventId.of("  evt-1 ");

            assertEquals("evt-1", id.value());
            assertEquals("evt-1", id.toString());
            assertEquals(EventId.of("evt-1"), id);
            assertEquals(EventId.of("evt-1").hashCode(), id.hashCode());
        }

        @Test
        @DisplayName("generate() produce UUID válidos y distintos")
        void generatesUniqueUuids() {
            EventId a = EventId.generate();
            EventId b = EventId.generate();

            assertDoesNotThrow(() -> UUID.fromString(a.value()));
            assertNotEquals(a, b);
        }
    }

    @Nested
    @DisplayName("UserId")
    class UserIdTests {

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t"})
        @DisplayName("Rechaza valores nulos o en blanco con InvalidUserDataException")
        void rejectsBlank(String value) {
            assertThrows(InvalidUserDataException.class, () -> UserId.of(value));
        }

        @Test
        @DisplayName("Elimina los espacios de los extremos y se compara por valor")
        void trimsAndComparesByValue() {
            UserId id = UserId.of(" ana ");

            assertEquals("ana", id.value());
            assertEquals("ana", id.toString());
            assertEquals(UserId.of("ana"), id);
        }

        @Test
        @DisplayName("generate() produce UUID válidos y distintos")
        void generatesUniqueUuids() {
            UserId a = UserId.generate();

            assertDoesNotThrow(() -> UUID.fromString(a.value()));
            assertNotEquals(a, UserId.generate());
        }
    }

    @Nested
    @DisplayName("ParticipationId")
    class ParticipationIdTests {

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t"})
        @DisplayName("Rechaza valores nulos o en blanco con InvalidParticipationDataException")
        void rejectsBlank(String value) {
            assertThrows(InvalidParticipationDataException.class, () -> ParticipationId.of(value));
        }

        @Test
        @DisplayName("Elimina los espacios de los extremos y se compara por valor")
        void trimsAndComparesByValue() {
            ParticipationId id = ParticipationId.of(" part-7\n");

            assertEquals("part-7", id.value());
            assertEquals(ParticipationId.of("part-7"), id);
        }

        @Test
        @DisplayName("generate() produce UUID válidos y distintos")
        void generatesUniqueUuids() {
            ParticipationId a = ParticipationId.generate();

            assertDoesNotThrow(() -> UUID.fromString(a.value()));
            assertNotEquals(a, ParticipationId.generate());
        }
    }

    @Nested
    @DisplayName("PhotoId")
    class PhotoIdTests {

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t"})
        @DisplayName("Rechaza valores nulos o en blanco con InvalidParticipationDataException")
        void rejectsBlank(String value) {
            assertThrows(InvalidParticipationDataException.class, () -> PhotoId.of(value));
        }

        @Test
        @DisplayName("Elimina los espacios de los extremos y se compara por valor")
        void trimsAndComparesByValue() {
            PhotoId id = PhotoId.of(" foto-3 ");

            assertEquals("foto-3", id.value());
            assertEquals(PhotoId.of("foto-3"), id);
        }

        @Test
        @DisplayName("generate() produce UUID válidos y distintos")
        void generatesUniqueUuids() {
            PhotoId a = PhotoId.generate();

            assertDoesNotThrow(() -> UUID.fromString(a.value()));
            assertNotEquals(a, PhotoId.generate());
        }
    }
}
