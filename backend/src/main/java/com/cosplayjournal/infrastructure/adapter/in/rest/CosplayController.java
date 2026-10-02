package com.cosplayjournal.infrastructure.adapter.in.rest;

import com.cosplayjournal.application.port.in.ChangeCosplayStatusUseCase;
import com.cosplayjournal.application.port.in.CreateCosplayUseCase;
import com.cosplayjournal.application.port.in.GetCosplayUseCase;
import com.cosplayjournal.domain.model.Cosplay;
import com.cosplayjournal.domain.model.CosplayStatus;
import com.cosplayjournal.infrastructure.adapter.in.rest.dto.ChangeCosplayStatusRequest;
import com.cosplayjournal.infrastructure.adapter.in.rest.dto.CreateCosplayRequest;
import com.cosplayjournal.infrastructure.adapter.in.rest.dto.CosplayResponse;
import com.cosplayjournal.infrastructure.adapter.in.rest.mapper.CosplayRestMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/cosplays")
@Tag(name = "Cosplays", description = "Gestión de proyectos de cosplay (cosplans) y estados")
public class CosplayController {

    private final CreateCosplayUseCase createCosplayUseCase;
    private final GetCosplayUseCase getCosplayUseCase;
    private final ChangeCosplayStatusUseCase changeCosplayStatusUseCase;

    public CosplayController(
            CreateCosplayUseCase createCosplayUseCase,
            GetCosplayUseCase getCosplayUseCase,
            ChangeCosplayStatusUseCase changeCosplayStatusUseCase
    ) {
        this.createCosplayUseCase = createCosplayUseCase;
        this.getCosplayUseCase = getCosplayUseCase;
        this.changeCosplayStatusUseCase = changeCosplayStatusUseCase;
    }

    @PostMapping
    @Operation(summary = "Crear nuevo proyecto de cosplay", description = "Crea un cosplan asignando como ownerId al usuario autenticado")
    @ApiResponse(responseCode = "201", description = "Cosplay creado exitosamente")
    @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos")
    @ApiResponse(responseCode = "401", description = "No autenticado")
    public ResponseEntity<CosplayResponse> createCosplay(@Valid @RequestBody CreateCosplayRequest request) {
        Cosplay created = createCosplayUseCase.createCosplay(CosplayRestMapper.toCommand(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(CosplayRestMapper.toResponse(created));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener cosplay por ID", description = "Devuelve los detalles del proyecto de cosplay")
    @ApiResponse(responseCode = "200", description = "Cosplay encontrado")
    @ApiResponse(responseCode = "404", description = "Cosplay no encontrado")
    public ResponseEntity<CosplayResponse> getCosplayById(@PathVariable Long id) {
        Cosplay cosplay = getCosplayUseCase.getCosplayById(id);
        return ResponseEntity.ok(CosplayRestMapper.toResponse(cosplay));
    }

    @GetMapping
    @Operation(summary = "Listar todos los cosplays", description = "Obtiene el listado general de proyectos de cosplay")
    @ApiResponse(responseCode = "200", description = "Listado de cosplays")
    public ResponseEntity<List<CosplayResponse>> getAllCosplays() {
        List<CosplayResponse> list = getCosplayUseCase.getAllCosplays().stream()
                .map(CosplayRestMapper::toResponse)
                .toList();
        return ResponseEntity.ok(list);
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Cambiar estado de un cosplay", description = "Actualiza el estado guardando las reglas de transición del dominio. Requiere ser propietario del cosplay o ADMIN")
    @ApiResponse(responseCode = "200", description = "Estado de cosplay actualizado")
    @ApiResponse(responseCode = "400", description = "Transición de estado inválida")
    @ApiResponse(responseCode = "403", description = "No autorizado para modificar este cosplay")
    @ApiResponse(responseCode = "404", description = "Cosplay no encontrado")
    public ResponseEntity<CosplayResponse> changeStatus(
            @PathVariable Long id,
            @Valid @RequestBody ChangeCosplayStatusRequest request
    ) {
        Cosplay updated = changeCosplayStatusUseCase.changeCosplayStatus(
                CosplayRestMapper.toStatusCommand(id, request)
        );
        return ResponseEntity.ok(CosplayRestMapper.toResponse(updated));
    }
}
