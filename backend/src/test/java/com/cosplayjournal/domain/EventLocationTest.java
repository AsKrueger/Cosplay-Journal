package com.cosplayjournal.domain;

import com.cosplayjournal.domain.exception.InvalidEventDataException;
import com.cosplayjournal.domain.model.event.EventLocation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EventLocationTest {

    @Test
    @DisplayName("Debe crear una ubicación válida con valores por defecto")
    void shouldCreateValidLocation() {
        EventLocation location = EventLocation.of("Sevilla", "Fibes");

        assertEquals("Sevilla", location.city());
        assertEquals("Fibes", location.venue());
        assertEquals("España", location.country());
    }

    @Test
    @DisplayName("Debe lanzar excepción si la ciudad es vacía o nula")
    void shouldThrowExceptionWhenCityIsInvalid() {
        assertThrows(InvalidEventDataException.class, () -> EventLocation.of("", "Venue"));
        assertThrows(InvalidEventDataException.class, () -> EventLocation.of(null, "Venue"));
    }
}
