package com.cosplayjournal.infrastructure.adapter.in.rest.dto;

import com.cosplayjournal.domain.model.event.EventSource;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreateEventRequest(
        @NotBlank(message = "El nombre del evento es obligatorio")
        @Size(min = 2, max = 150, message = "El nombre debe tener entre 2 y 150 caracteres")
        String name,

        String description,

        @NotNull(message = "La fecha de inicio es obligatoria")
        LocalDate startDate,

        @NotNull(message = "La fecha de fin es obligatoria")
        LocalDate endDate,

        @NotBlank(message = "La ciudad es obligatoria")
        String city,

        String venue,
        String province,
        String country,
        String address,
        Double latitude,
        Double longitude,
        String website,
        EventSource source
) {
}
