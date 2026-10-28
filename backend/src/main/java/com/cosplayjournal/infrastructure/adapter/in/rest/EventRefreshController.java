package com.cosplayjournal.infrastructure.adapter.in.rest;

import com.cosplayjournal.application.dto.ImportEventsResult;
import com.cosplayjournal.application.port.in.ImportExternalEventsUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Actualización del catálogo al abrir la app. Es pública porque no expone datos privados,
 * pero solo consulta ListadoManga si la última sincronización tiene más de 30 minutos.
 */
@RestController
@RequestMapping("/api/v1/events/refresh")
@Tag(name = "Eventos", description = "Consulta y actualización del catálogo de eventos")
public class EventRefreshController {

    private final ImportExternalEventsUseCase importExternalEventsUseCase;

    public EventRefreshController(ImportExternalEventsUseCase importExternalEventsUseCase) {
        this.importExternalEventsUseCase = importExternalEventsUseCase;
    }

    @PostMapping
    @Operation(summary = "Actualizar el catálogo desde ListadoManga", description = "Sincroniza todos los meses del periodo si la última sincronización tiene más de 30 minutos. En caso contrario devuelve el resultado de la última sincronización.")
    @ApiResponse(responseCode = "200", description = "Resultado de la sincronización")
    public ResponseEntity<ImportEventsResult> refresh() {
        return ResponseEntity.ok(importExternalEventsUseCase.refreshIfStale());
    }
}
