package com.cosplayjournal.infrastructure.adapter.in.rest;

import com.cosplayjournal.application.port.in.CreateEventUseCase;
import com.cosplayjournal.application.port.in.GetEventUseCase;
import com.cosplayjournal.domain.model.event.Event;
import com.cosplayjournal.domain.model.event.EventId;
import com.cosplayjournal.infrastructure.adapter.in.rest.dto.CreateEventRequest;
import com.cosplayjournal.infrastructure.adapter.in.rest.dto.EventResponse;
import com.cosplayjournal.infrastructure.adapter.in.rest.mapper.EventRestMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<List<EventResponse>> getAllEvents() {
        List<EventResponse> events = getEventUseCase.getAllEvents().stream()
                .map(EventRestMapper::toResponse)
                .toList();
        return ResponseEntity.ok(events);
    }
}
