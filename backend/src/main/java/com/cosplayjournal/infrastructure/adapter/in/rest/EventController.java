package com.cosplayjournal.infrastructure.adapter.in.rest;

import com.cosplayjournal.application.dto.EventSearchCriteria;
import com.cosplayjournal.application.dto.PageResult;
import com.cosplayjournal.application.port.in.CreateEventUseCase;
import com.cosplayjournal.application.port.in.GetEventUseCase;
import com.cosplayjournal.domain.model.event.Event;
import com.cosplayjournal.domain.model.event.EventId;
import com.cosplayjournal.domain.model.event.EventSource;
import com.cosplayjournal.domain.model.event.EventStatus;
import com.cosplayjournal.infrastructure.adapter.in.rest.dto.CreateEventRequest;
import com.cosplayjournal.infrastructure.adapter.in.rest.dto.EventResponse;
import com.cosplayjournal.infrastructure.adapter.in.rest.mapper.EventRestMapper;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/events")
public class EventController {

    private final CreateEventUseCase createEventUseCase;
    private final GetEventUseCase getEventUseCase;

    public EventController(CreateEventUseCase createEventUseCase, GetEventUseCase getEventUseCase) {
        this.createEventUseCase = createEventUseCase;
        this.getEventUseCase = getEventUseCase;
    }

    @PostMapping
    public ResponseEntity<EventResponse> createEvent(@Valid @RequestBody CreateEventRequest request) {
        Event created = createEventUseCase.createEvent(EventRestMapper.toCommand(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(EventRestMapper.toResponse(created));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventResponse> getEventById(@PathVariable String id) {
        Event event = getEventUseCase.getEventById(EventId.of(id));
        return ResponseEntity.ok(EventRestMapper.toResponse(event));
    }

    @GetMapping
    public ResponseEntity<PageResult<EventResponse>> searchEvents(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) EventSource source,
            @RequestParam(required = false) EventStatus status,
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        EventSearchCriteria criteria = new EventSearchCriteria(city, from, to, source, status, query, page, size);
        PageResult<Event> pagedEvents = getEventUseCase.searchEvents(criteria);

        List<EventResponse> responses = pagedEvents.content().stream()
                .map(EventRestMapper::toResponse)
                .toList();

        PageResult<EventResponse> pageResult = new PageResult<>(
                responses,
                pagedEvents.page(),
                pagedEvents.size(),
                pagedEvents.totalElements(),
                pagedEvents.totalPages(),
                pagedEvents.last()
        );

        return ResponseEntity.ok(pageResult);
    }
}
