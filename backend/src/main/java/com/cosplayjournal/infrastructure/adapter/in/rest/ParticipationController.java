package com.cosplayjournal.infrastructure.adapter.in.rest;

import com.cosplayjournal.application.port.in.*;
import com.cosplayjournal.domain.model.participation.Participation;
import com.cosplayjournal.domain.model.participation.ParticipationId;
import com.cosplayjournal.infrastructure.adapter.in.rest.dto.*;
import com.cosplayjournal.infrastructure.adapter.in.rest.mapper.ParticipationRestMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/participations")
@Tag(name = "Participations", description = "Coordinación de participaciones individuales, dúos y grupales para eventos")
public class ParticipationController {

    private final CreateParticipationUseCase createParticipationUseCase;
    private final GetParticipationUseCase getParticipationUseCase;
    private final JoinParticipationUseCase joinParticipationUseCase;
    private final LeaveParticipationUseCase leaveParticipationUseCase;
    private final AssignCharacterUseCase assignCharacterUseCase;

    public ParticipationController(
            CreateParticipationUseCase createParticipationUseCase,
            GetParticipationUseCase getParticipationUseCase,
            JoinParticipationUseCase joinParticipationUseCase,
            LeaveParticipationUseCase leaveParticipationUseCase,
            AssignCharacterUseCase assignCharacterUseCase
    ) {
        this.createParticipationUseCase = createParticipationUseCase;
        this.getParticipationUseCase = getParticipationUseCase;
        this.joinParticipationUseCase = joinParticipationUseCase;
        this.leaveParticipationUseCase = leaveParticipationUseCase;
        this.assignCharacterUseCase = assignCharacterUseCase;
    }

    @PostMapping
    @Operation(summary = "Crear participación en evento", description = "Organiza una participación Individual, Dúo o Grupal asignando creatorId del usuario autenticado")
    @ApiResponse(responseCode = "201", description = "Participación creada exitosamente")
    @ApiResponse(responseCode = "400", description = "Invariantes de participación no cumplidas")
    @ApiResponse(responseCode = "401", description = "No autenticado")
    public ResponseEntity<ParticipationResponse> createParticipation(@Valid @RequestBody CreateParticipationRequest request) {
        Participation created = createParticipationUseCase.createParticipation(ParticipationRestMapper.toCommand(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(ParticipationRestMapper.toResponse(created));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener participación por ID", description = "Devuelve los detalles y lista de participantes")
    @ApiResponse(responseCode = "200", description = "Participación encontrada")
    @ApiResponse(responseCode = "404", description = "Participación no encontrada")
    public ResponseEntity<ParticipationResponse> getParticipationById(@PathVariable String id) {
        Participation participation = getParticipationUseCase.getParticipationById(ParticipationId.of(id));
        return ResponseEntity.ok(ParticipationRestMapper.toResponse(participation));
    }

    @GetMapping
    @Operation(summary = "Listar participaciones", description = "Consulta el listado general de participaciones")
    @ApiResponse(responseCode = "200", description = "Listado de participaciones")
    public ResponseEntity<List<ParticipationResponse>> getAllParticipations() {
        List<ParticipationResponse> list = getParticipationUseCase.getAllParticipations().stream()
                .map(ParticipationRestMapper::toResponse)
                .toList();
        return ResponseEntity.ok(list);
    }

    @PostMapping("/{id}/members")
    @Operation(summary = "Unirse a una participación", description = "Añade un nuevo participante al grupo o dúo respetando los límites del tipo de participación")
    @ApiResponse(responseCode = "200", description = "Miembro añadido correctamente")
    @ApiResponse(responseCode = "400", description = "Usuario ya registrado o límite excedido")
    @ApiResponse(responseCode = "403", description = "No autorizado para unir a este usuario")
    public ResponseEntity<ParticipationResponse> joinParticipation(
            @PathVariable String id,
            @Valid @RequestBody JoinParticipationRequest request
    ) {
        Participation updated = joinParticipationUseCase.joinParticipation(
                ParticipationRestMapper.toJoinCommand(id, request)
        );
        return ResponseEntity.ok(ParticipationRestMapper.toResponse(updated));
    }

    @DeleteMapping("/{id}/members/{userId}")
    @Operation(summary = "Retirar miembro de participación", description = "Elimina un participante de la participación")
    @ApiResponse(responseCode = "200", description = "Miembro eliminado correctamente")
    @ApiResponse(responseCode = "403", description = "No autorizado para retirar a este usuario")
    public ResponseEntity<ParticipationResponse> leaveParticipation(
            @PathVariable String id,
            @PathVariable String userId
    ) {
        Participation updated = leaveParticipationUseCase.leaveParticipation(
                new LeaveParticipationCommand(ParticipationId.of(id), userId)
        );
        return ResponseEntity.ok(ParticipationRestMapper.toResponse(updated));
    }

    @PutMapping("/{id}/characters")
    @Operation(summary = "Asignar personaje a participante", description = "Establece el personaje de cosplay asignado a un participante del grupo")
    @ApiResponse(responseCode = "200", description = "Personaje asignado correctamente")
    @ApiResponse(responseCode = "403", description = "No autorizado")
    public ResponseEntity<ParticipationResponse> assignCharacter(
            @PathVariable String id,
            @Valid @RequestBody AssignCharacterRequest request
    ) {
        Participation updated = assignCharacterUseCase.assignCharacter(
                ParticipationRestMapper.toAssignCharacterCommand(id, request)
        );
        return ResponseEntity.ok(ParticipationRestMapper.toResponse(updated));
    }
}
