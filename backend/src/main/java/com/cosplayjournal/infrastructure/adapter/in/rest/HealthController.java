package com.cosplayjournal.infrastructure.adapter.in.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/health")
@Tag(name = "Health", description = "Verificación pública de estado de salud del servidor")
public class HealthController {

    @GetMapping
    @Operation(summary = "Healthcheck de la API", description = "Retorna el estado de salud del servidor (público)")
    @ApiResponse(responseCode = "200", description = "Servidor operativo")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP"));
    }
}
