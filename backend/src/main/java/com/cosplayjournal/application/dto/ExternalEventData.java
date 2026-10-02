package com.cosplayjournal.application.dto;

import java.time.LocalDate;

public record ExternalEventData(
        String externalId,
        String name,
        String description,
        LocalDate startDate,
        LocalDate endDate,
        String city,
        String venue,
        String province,
        String website
) {
}
