package com.cosplayjournal.infrastructure.adapter.in.rest;

import com.cosplayjournal.application.port.in.CreateCosplayUseCase;
import com.cosplayjournal.application.port.in.GetCosplayUseCase;
import com.cosplayjournal.domain.model.Cosplay;
import com.cosplayjournal.infrastructure.adapter.in.rest.dto.CosplayResponse;
import com.cosplayjournal.infrastructure.adapter.in.rest.dto.CreateCosplayRequest;
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
@Tag(name = "Cosplays", description = "Cosplays del usuario: nombre y foto, para la galería y las grupales")
public class CosplayController {

    private final CreateCosplayUseCase createCosplayUseCase;
    private final GetCosplayUseCase getCosplayUseCase;

    public CosplayController(CreateCosplayUseCase createCosplayUseCase, GetCosplayUseCase getCosplayUseCase) {
        this.createCosplayUseCase = createCosplayUseCase;
        this.getCosplayUseCase = getCosplayUseCase;
    }

    @PostMapping
    @Operation(summary = "Crear un cosplay", description = "Un cosplay es un nombre y una foto")
    @ApiResponse(responseCode = "201", description = "Cosplay creado")
    @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos")
    @ApiResponse(responseCode = "401", description = "No autenticado")
    public ResponseEntity<CosplayResponse> createCosplay(@Valid @RequestBody CreateCosplayRequest request) {
        Cosplay created = createCosplayUseCase.createCosplay(CosplayRestMapper.toCommand(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(CosplayRestMapper.toResponse(created));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener un cosplay por ID")
    @ApiResponse(responseCode = "200", description = "Cosplay encontrado")
    @ApiResponse(responseCode = "404", description = "Cosplay no encontrado")
    public ResponseEntity<CosplayResponse> getCosplayById(@PathVariable Long id) {
        return ResponseEntity.ok(CosplayRestMapper.toResponse(getCosplayUseCase.getCosplayById(id)));
    }

    @GetMapping
    @Operation(summary = "Listar cosplays")
    @ApiResponse(responseCode = "200", description = "Listado de cosplays")
    public ResponseEntity<List<CosplayResponse>> getAllCosplays() {
        List<CosplayResponse> list = getCosplayUseCase.getAllCosplays().stream()
                .map(CosplayRestMapper::toResponse)
                .toList();
        return ResponseEntity.ok(list);
    }
}
