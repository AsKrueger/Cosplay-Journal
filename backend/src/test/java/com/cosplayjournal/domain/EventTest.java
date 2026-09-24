package com.cosplayjournal.domain;

import com.cosplayjournal.domain.exception.InvalidEventDataException;
import com.cosplayjournal.domain.model.event.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class EventTest {

    @Test
    @DisplayName("Debe crear un Event con datos válidos")
    void shouldCreateEventWithValidData() {
        EventId id = EventId.of("evt-1");
        LocalDate start = LocalDate.now().plusDays(10);
        LocalDate end = start.plusDays(2);
        EventLocation location = EventLocation.of("Madrid", "IFEMA", "Madrid");

        Event event = Event.create(
                id,
                "Japan Weekend Madrid",
                "Gran evento de cultura pop japonesa",
                start,
                end,
                location,
                "https://japanweekend.com",
                EventSource.LISTADOMANGA
        );

        assertNotNull(event);
        assertEquals("evt-1", event.getId().value());
        assertEquals("Japan Weekend Madrid", event.getName());
        assertEquals("Japan Weekend Madrid", event.getName());
        assertEquals(start, event.getDateRange().startDate());
        assertEquals(end, event.getDateRange().endDate());
        assertEquals("Madrid", event.getLocation().city());
        assertEquals(EventSource.LISTADOMANGA, event.getSource());
        assertEquals(EventStatus.SCHEDULED, event.getStatus());
    }

    @Test
    @DisplayName("Debe lanzar excepción si el nombre del evento es nulo o vacío")
    void shouldThrowExceptionWhenNameIsInvalid() {
        EventId id = EventId.generate();
        EventLocation location = EventLocation.of("Barcelona", "La Farga");
        EventDateRange dateRange = EventDateRange.singleDay(LocalDate.now());

        assertThrows(InvalidEventDataException.class, () ->
                new Event(id, "", "Desc", dateRange, location, "", EventSource.MANUAL_ADMIN, EventStatus.SCHEDULED)
        );

        assertThrows(InvalidEventDataException.class, () ->
                new Event(id, null, "Desc", dateRange, location, "", EventSource.MANUAL_ADMIN, EventStatus.SCHEDULED)
        );
    }

    @Test
    @DisplayName("Debe cambiar el estado del evento correctamente")
    void shouldChangeStatus() {
        Event event = Event.create(
                EventId.generate(), "Salon Manga", "Desc",
                LocalDate.now(), LocalDate.now().plusDays(1),
                EventLocation.of("Valencia", "Feria Valencia"),
                "", EventSource.COMMUNITY
        );

        assertEquals(EventStatus.SCHEDULED, event.getStatus());

        event.cancel();
        assertEquals(EventStatus.CANCELLED, event.getStatus());
    }

    @Test
    @DisplayName("No debe permitir completar un evento cancelado")
    void shouldNotAllowCompletingCancelledEvent() {
        Event event = Event.create(
                EventId.generate(), "Salon Manga", "Desc",
                LocalDate.now(), LocalDate.now().plusDays(1),
                EventLocation.of("Valencia", "Feria Valencia"),
                "", EventSource.COMMUNITY
        );

        event.cancel();
        assertThrows(InvalidEventDataException.class, event::complete);
    }
}
