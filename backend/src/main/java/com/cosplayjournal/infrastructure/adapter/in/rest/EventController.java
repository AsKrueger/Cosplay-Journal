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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/events")
@Tag(name = "Events", description = "Consulta, filtrado, paginación y creación del catálogo de eventos de cosplay")
public class EventController {

    private final CreateEventUseCase createEventUseCase;
    private final GetEventUseCase getEventUseCase;

    public EventController(CreateEventUseCase createEventUseCase, GetEventUseCase getEventUseCase) {
        this.createEventUseCase = createEventUseCase;
        this.getEventUseCase = getEventUseCase;
    }

    @PostMapping
    @Operation(summary = "Crear nuevo evento manualmente", description = "Crea un evento en el catálogo con fuente MANUAL_ADMIN")
    @ApiResponse(responseCode = "201", description = "Evento creado exitosamente")
    @ApiResponse(responseCode = "400", description = "Datos de evento inválidos")
    @ApiResponse(responseCode = "401", description = "No autenticado")
    public ResponseEntity<EventResponse> createEvent(@Valid @RequestBody CreateEventRequest request) {
        Event created = createEventUseCase.createEvent(EventRestMapper.toCommand(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(EventRestMapper.toResponse(created));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener evento por ID", description = "Consulta la información detallada de un evento del catálogo")
    @ApiResponse(responseCode = "200", description = "Evento encontrado")
    @ApiResponse(responseCode = "404", description = "Evento no encontrado")
    public ResponseEntity<EventResponse> getEventById(@PathVariable String id) {
        Event event = getEventUseCase.getEventById(EventId.of(id));
        return ResponseEntity.ok(EventRestMapper.toResponse(event));
    }

    @GetMapping
    @Operation(summary = "Consultar y filtrar catálogo de eventos", description = "Busca eventos aplicando filtros opcionales por ciudad, rango de fechas, fuente, estado o búsqueda textual con resultados paginados")
    @ApiResponse(responseCode = "200", description = "Listado paginado de eventos")
    @ApiResponse(responseCode = "400", description = "Parámetros de búsqueda o rango de fechas inválido")
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
