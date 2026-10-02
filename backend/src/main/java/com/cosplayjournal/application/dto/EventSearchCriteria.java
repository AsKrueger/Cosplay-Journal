package com.cosplayjournal.application.dto;

import com.cosplayjournal.domain.model.event.EventSource;
import com.cosplayjournal.domain.model.event.EventStatus;

import java.time.LocalDate;

public record EventSearchCriteria(
        String city,
        LocalDate from,
        LocalDate to,
        EventSource source,
        EventStatus status,
        String query,
        int page,
        int size
) {
    public EventSearchCriteria {
        if (page < 0) {
            throw new IllegalArgumentException("El número de página no puede ser negativo");
        }
        if (size < 1 || size > 100) {
            throw new IllegalArgumentException("El tamaño de página debe estar entre 1 y 100");
        }
        if (from != null && to != null && from.isAfter(to)) {
            throw new IllegalArgumentException("La fecha 'from' no puede ser posterior a la fecha 'to'");
        }
        city = (city != null && !city.trim().isEmpty()) ? city.trim() : null;
        query = (query != null && !query.trim().isEmpty()) ? query.trim() : null;
    }

    public static EventSearchCriteria defaultCriteria() {
        return new EventSearchCriteria(null, null, null, null, null, null, 0, 20);
    }
}
