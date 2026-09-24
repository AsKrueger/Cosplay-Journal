package com.cosplayjournal.domain.model.event;

import com.cosplayjournal.domain.exception.InvalidEventDataException;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public record EventDateRange(LocalDate startDate, LocalDate endDate) {

    public EventDateRange {
        if (startDate == null) {
            throw new InvalidEventDataException("La fecha de inicio del evento es obligatoria");
        }
        if (endDate == null) {
            throw new InvalidEventDataException("La fecha de fin del evento es obligatoria");
        }
        if (startDate.isAfter(endDate)) {
            throw new InvalidEventDataException("La fecha de inicio (" + startDate + ") no puede ser posterior a la fecha de fin (" + endDate + ")");
        }
    }

    public static EventDateRange singleDay(LocalDate date) {
        return new EventDateRange(date, date);
    }

    public static EventDateRange of(LocalDate startDate, LocalDate endDate) {
        return new EventDateRange(startDate, endDate);
    }

    public boolean isOngoing(LocalDate currentDate) {
        if (currentDate == null) return false;
        return !currentDate.isBefore(startDate) && !currentDate.isAfter(endDate);
    }

    public boolean isFuture(LocalDate currentDate) {
        if (currentDate == null) return false;
        return startDate.isAfter(currentDate);
    }

    public boolean isPast(LocalDate currentDate) {
        if (currentDate == null) return false;
        return endDate.isBefore(currentDate);
    }

    public long durationInDays() {
        return ChronoUnit.DAYS.between(startDate, endDate) + 1;
    }
}
