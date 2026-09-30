package com.cosplayjournal.infrastructure.adapter.in.rest.dto;

import java.time.LocalDate;

public record EventResponse(
        String id,
        String name,
        String description,
        LocalDate startDate,
        LocalDate endDate,
        String city,
        String venue,
        String province,
        String country,
        String address,
        Double latitude,
        Double longitude,
        String website,
        String source,
        String status
) {
}
