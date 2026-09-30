package com.cosplayjournal.infrastructure.adapter.in.rest;

import com.cosplayjournal.application.port.in.*;
import com.cosplayjournal.domain.model.participation.Participation;
import com.cosplayjournal.domain.model.participation.ParticipationId;
import com.cosplayjournal.infrastructure.adapter.in.rest.dto.*;
import com.cosplayjournal.infrastructure.adapter.in.rest.mapper.ParticipationRestMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/participations")
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
    public ResponseEntity<ParticipationResponse> createParticipation(@Valid @RequestBody CreateParticipationRequest request) {
        Participation created = createParticipationUseCase.createParticipation(ParticipationRestMapper.toCommand(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(ParticipationRestMapper.toResponse(created));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ParticipationResponse> getParticipationById(@PathVariable String id) {
        Participation participation = getParticipationUseCase.getParticipationById(ParticipationId.of(id));
        return ResponseEntity.ok(ParticipationRestMapper.toResponse(participation));
    }

    @GetMapping
    public ResponseEntity<List<ParticipationResponse>> getAllParticipations() {
        List<ParticipationResponse> list = getParticipationUseCase.getAllParticipations().stream()
                .map(ParticipationRestMapper::toResponse)
                .toList();
        return ResponseEntity.ok(list);
    }

    @PostMapping("/{id}/members")
    public ResponseEntity<ParticipationResponse> joinParticipation(
            @PathVariable String id,
            @Valid @RequestBody JoinParticipationRequest request
    ) {
        Participation updated = joinParticipationUseCase.joinParticipation(
                new JoinParticipationCommand(ParticipationId.of(id), request.userId(), request.name(), request.role())
        );
        return ResponseEntity.ok(ParticipationRestMapper.toResponse(updated));
    }

    @DeleteMapping("/{id}/members/{userId}")
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
    public ResponseEntity<ParticipationResponse> assignCharacter(
            @PathVariable String id,
            @Valid @RequestBody AssignCharacterRequest request
    ) {
        Participation updated = assignCharacterUseCase.assignCharacter(
                new AssignCharacterCommand(ParticipationId.of(id), request.userId(), request.characterName())
        );
        return ResponseEntity.ok(ParticipationRestMapper.toResponse(updated));
    }
}
