package com.cosplayjournal.infrastructure.adapter.out.persistence.mapper;

import com.cosplayjournal.domain.model.event.*;
import com.cosplayjournal.infrastructure.adapter.out.persistence.entity.EventJpaEntity;

public class EventPersistenceMapper {

    public static EventJpaEntity toJpaEntity(Event event) {
        if (event == null) return null;
        return new EventJpaEntity(
                event.getId().value(),
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
        return new Event(
                EventId.of(entity.getId()),
                entity.getName(),
                entity.getDescription(),
                EventDateRange.of(entity.getStartDate(), entity.getEndDate()),
                new EventLocation(
                        entity.getCity(),
                        entity.getVenue(),
                        entity.getProvince(),
                        entity.getCountry(),
                        entity.getAddress(),
                        entity.getLatitude(),
                        entity.getLongitude()
                ),
                entity.getWebsite(),
                EventSource.valueOf(entity.getSource()),
                EventStatus.valueOf(entity.getStatus())
        );
    }
}
