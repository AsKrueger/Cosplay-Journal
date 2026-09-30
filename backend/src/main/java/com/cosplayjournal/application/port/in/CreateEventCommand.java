package com.cosplayjournal.application.port.in;

import com.cosplayjournal.domain.model.event.EventLocation;
import com.cosplayjournal.domain.model.event.EventSource;

import java.time.LocalDate;

public record CreateEventCommand(
        String name,
        String description,
        LocalDate startDate,
        LocalDate endDate,
        EventLocation location,
        String website,
        EventSource source
) {
    public CreateEventCommand {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del evento es obligatorio");
        }
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Las fechas de inicio y fin son obligatorias");
        }
        if (location == null) {
            throw new IllegalArgumentException("La ubicación del evento es obligatoria");
        }
    }
}
