package com.cosplayjournal.infrastructure.adapter.in.rest;

import com.cosplayjournal.application.dto.RefreshStatus;
import com.cosplayjournal.application.port.in.ImportExternalEventsUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

/**
 * Actualización del catálogo al abrir la app. Es pública porque no expone datos privados.
 * La sincronización se ejecuta en segundo plano, con límite de frecuencia en el servicio.
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
    @Operation(summary = "Actualizar el catálogo desde ListadoManga", description = "Lanza la sincronización en segundo plano si la última tiene más de 30 minutos. Responde de inmediato; el progreso se consulta con GET.")
    @ApiResponse(responseCode = "202", description = "Actualización aceptada (se ejecuta en segundo plano)")
    public ResponseEntity<Void> refresh() {
        CompletableFuture.runAsync(importExternalEventsUseCase::refreshIfStale);
        return ResponseEntity.accepted().build();
    }

    @GetMapping
    @Operation(summary = "Estado de la actualización del catálogo", description = "Indica si hay una sincronización en curso y cuándo terminó la última.")
    @ApiResponse(responseCode = "200", description = "Estado de la actualización")
    public ResponseEntity<RefreshStatus> status() {
        return ResponseEntity.ok(importExternalEventsUseCase.refreshStatus());
    }
}
