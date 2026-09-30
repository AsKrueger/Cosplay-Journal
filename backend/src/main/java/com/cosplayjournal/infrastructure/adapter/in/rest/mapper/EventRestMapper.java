package com.cosplayjournal.infrastructure.adapter.in.rest.mapper;

import com.cosplayjournal.application.port.in.CreateEventCommand;
import com.cosplayjournal.domain.model.event.Event;
import com.cosplayjournal.domain.model.event.EventLocation;
import com.cosplayjournal.domain.model.event.EventSource;
import com.cosplayjournal.infrastructure.adapter.in.rest.dto.CreateEventRequest;
import com.cosplayjournal.infrastructure.adapter.in.rest.dto.EventResponse;

public class EventRestMapper {

    public static CreateEventCommand toCommand(CreateEventRequest request) {
        EventLocation location = new EventLocation(
                request.city(),
                request.venue(),
                request.province(),
                request.country() != null ? request.country() : "España",
                request.address(),
                request.latitude(),
                request.longitude()
        );

        return new CreateEventCommand(
                request.name(),
                request.description(),
                request.startDate(),
                request.endDate(),
                location,
                request.website(),
                request.source() != null ? request.source() : EventSource.MANUAL_ADMIN
        );
    }

    public static EventResponse toResponse(Event event) {
        if (event == null) return null;
        return new EventResponse(
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
}
