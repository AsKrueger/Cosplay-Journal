package com.cosplayjournal.infrastructure.adapter.out.persistence.mapper;

import com.cosplayjournal.domain.model.event.*;
import com.cosplayjournal.infrastructure.adapter.out.persistence.entity.EventJpaEntity;

public class EventPersistenceMapper {

    public static EventJpaEntity toJpaEntity(Event event) {
        if (event == null) return null;
        return new EventJpaEntity(
                event.getId().value(),
                event.getExternalId(),
                event.getName(),
                event.getDescription(),
                event.getDateRange().startDate(),
                event.getDateRange().endDate(),
                event.getLocation().city(),
                event.getLocation().venue(),
                event.getLocation().province(),
                event.getLocation().country(),
                event.getLocation().address(),
                event.getLocation().latitude(),
                event.getLocation().longitude(),
                event.getWebsite(),
                event.getSource().name(),
                event.getStatus().name()
        );
    }

    public static Event toDomain(EventJpaEntity entity) {
        if (entity == null) return null;
        EventDateRange dateRange = EventDateRange.of(
                entity.getStartDate(),
                entity.getEndDate()
        );

        EventLocation location = new EventLocation(
                entity.getCity(),
                entity.getVenue(),
                entity.getProvince(),
                entity.getCountry(),
                entity.getAddress(),
                entity.getLatitude(),
                entity.getLongitude()
        );

        return new Event(
                EventId.of(entity.getId()),
                entity.getExternalId(),
                entity.getName(),
                entity.getDescription(),
                dateRange,
                location,
                entity.getWebsite(),
                EventSource.valueOf(entity.getSource()),
                EventStatus.valueOf(entity.getStatus())
        );
    }
}
