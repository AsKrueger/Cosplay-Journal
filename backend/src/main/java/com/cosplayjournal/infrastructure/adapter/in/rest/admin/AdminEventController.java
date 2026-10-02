package com.cosplayjournal.infrastructure.adapter.in.rest.admin;

import com.cosplayjournal.application.dto.ImportEventsResult;
import com.cosplayjournal.application.port.in.ImportExternalEventsUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/events")
@Tag(name = "Admin", description = "Operaciones administrativas e importación/sincronización externa")
public class AdminEventController {

    private final ImportExternalEventsUseCase importExternalEventsUseCase;

    public AdminEventController(ImportExternalEventsUseCase importExternalEventsUseCase) {
        this.importExternalEventsUseCase = importExternalEventsUseCase;
    }

    @PostMapping("/import")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Sincronización manual de eventos de ListadoManga", description = "Ejecuta a demanda la sincronización e ingesta de eventos desde la fuente externa ListadoManga. Requiere rol ROLE_ADMIN")
    @ApiResponse(responseCode = "200", description = "Sincronización completada exitosamente")
    @ApiResponse(responseCode = "401", description = "No autenticado")
    @ApiResponse(responseCode = "403", description = "Se requiere rol de administrador (ROLE_ADMIN)")
    public ResponseEntity<ImportEventsResult> importEvents() {
        ImportEventsResult result = importExternalEventsUseCase.importEvents();
        return ResponseEntity.ok(result);
    }
}
