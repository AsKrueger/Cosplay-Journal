package com.cosplayjournal.domain;

import com.cosplayjournal.domain.exception.InvalidEventDataException;
import com.cosplayjournal.domain.model.event.EventDateRange;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class EventDateRangeTest {

    @Test
    @DisplayName("Debe crear un rango de fechas válido")
    void shouldCreateValidDateRange() {
        LocalDate start = LocalDate.of(2026, 10, 12);
        LocalDate end = LocalDate.of(2026, 10, 14);

        EventDateRange range = EventDateRange.of(start, end);

        assertEquals(start, range.startDate());
        assertEquals(end, range.endDate());
        assertEquals(3, range.durationInDays());
    }

    @Test
    @DisplayName("Debe lanzar excepción si startDate es posterior a endDate")
    void shouldThrowExceptionWhenStartDateIsAfterEndDate() {
        LocalDate start = LocalDate.of(2026, 10, 15);
        LocalDate end = LocalDate.of(2026, 10, 12);

        assertThrows(InvalidEventDataException.class, () -> EventDateRange.of(start, end));
    }

    @Test
    @DisplayName("Debe evaluar isOngoing, isFuture e isPast correctamente")
    void shouldEvaluateDateHelperMethods() {
        LocalDate start = LocalDate.of(2026, 5, 10);
        LocalDate end = LocalDate.of(2026, 5, 12);
        EventDateRange range = EventDateRange.of(start, end);

        assertTrue(range.isOngoing(LocalDate.of(2026, 5, 11)));
        assertTrue(range.isFuture(LocalDate.of(2026, 5, 1)));
        assertTrue(range.isPast(LocalDate.of(2026, 5, 20)));

        assertFalse(range.isOngoing(LocalDate.of(2026, 5, 15)));
    }
}
