package com.cosplayjournal.domain.model.event;

import com.cosplayjournal.domain.exception.InvalidEventDataException;

public record EventLocation(
        String city,
        String venue,
        String province,
        String country,
        String address,
        Double latitude,
        Double longitude
) {
    public EventLocation {
        if (city == null || city.trim().isEmpty()) {
            throw new InvalidEventDataException("La ciudad de la ubicación es obligatoria");
        }
        city = city.trim();
        venue = venue != null ? venue.trim() : "";
        province = province != null ? province.trim() : "";
        country = country != null && !country.trim().isEmpty() ? country.trim() : "España";
        address = address != null ? address.trim() : "";
    }

    public static EventLocation of(String city, String venue) {
        return new EventLocation(city, venue, "", "España", "", null, null);
    }

    public static EventLocation of(String city, String venue, String province) {
        return new EventLocation(city, venue, province, "España", "", null, null);
    }
}
