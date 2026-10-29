package com.cosplayjournal.infrastructure.adapter.in.rest;

import com.cosplayjournal.application.port.in.ManageEventStatusUseCase;
import com.cosplayjournal.application.port.out.CurrentUserPort;
import com.cosplayjournal.domain.exception.InvalidUserDataException;
import com.cosplayjournal.domain.model.social.UserEventStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

/**
 * Estado de participación del usuario en cada evento: "Voy", "Fui", "Me interesa" o "Planeando".
 * Se guarda en el servidor para verlo desde cualquier dispositivo.
 */
@RestController
@RequestMapping("/api/v1/me/events")
@Tag(name = "Mi perfil", description = "Estado de participación en eventos del usuario autenticado")
public class MyEventStatusController {

    private final ManageEventStatusUseCase manageEventStatusUseCase;
    private final CurrentUserPort currentUserPort;

    public MyEventStatusController(ManageEventStatusUseCase manageEventStatusUseCase, CurrentUserPort currentUserPort) {
        this.manageEventStatusUseCase = manageEventStatusUseCase;
        this.currentUserPort = currentUserPort;
    }

    @GetMapping
    @Operation(summary = "Mis estados de evento")
    public ResponseEntity<List<EventStatusResponse>> list() {
        List<EventStatusResponse> body = manageEventStatusUseCase.listStatuses(currentUserPort.getRequiredCurrentUserId()).stream()
                .map(s -> new EventStatusResponse(s.eventId(), s.status(), s.updatedAt()))
                .toList();
        return ResponseEntity.ok(body);
    }

    @PutMapping("/{eventId}")
    @Operation(summary = "Cambiar mi estado en un evento", description = "ATTENDING, ATTENDED, INTERESTED, PLANNING o NONE para quitar la selección")
    public ResponseEntity<Void> set(@PathVariable String eventId, @RequestBody EventStatusRequest request) {
        String status = request.status();
        if (status == null || (!"NONE".equals(status) && !UserEventStatus.isStorable(status))) {
            throw new InvalidUserDataException("Estado de participación no válido: " + status);
        }
        manageEventStatusUseCase.setStatus(currentUserPort.getRequiredCurrentUserId(), eventId, status);
        return ResponseEntity.noContent().build();
    }

    public record EventStatusRequest(String status) {
    }

    public record EventStatusResponse(String eventId, String status, Instant updatedAt) {
    }
}
