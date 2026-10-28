package com.cosplayjournal.infrastructure.adapter.in.rest;

import com.cosplayjournal.application.dto.FriendRequestSummary;
import com.cosplayjournal.application.dto.PublicUserProfile;
import com.cosplayjournal.application.port.in.ManageFriendshipsUseCase;
import com.cosplayjournal.application.port.out.CurrentUserPort;
import com.cosplayjournal.domain.model.social.Friendship;
import com.cosplayjournal.infrastructure.adapter.in.rest.dto.FriendshipResponse;
import com.cosplayjournal.infrastructure.adapter.in.rest.dto.SendFriendRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Amistades entre cosplayers. No hay seguidores: una relación requiere la aceptación del destinatario.
 */
@RestController
@RequestMapping("/api/v1/friends")
@Tag(name = "Amistades", description = "Solicitudes y amistades entre usuarios")
public class FriendshipController {

    private final ManageFriendshipsUseCase manageFriendshipsUseCase;
    private final CurrentUserPort currentUserPort;

    public FriendshipController(ManageFriendshipsUseCase manageFriendshipsUseCase, CurrentUserPort currentUserPort) {
        this.manageFriendshipsUseCase = manageFriendshipsUseCase;
        this.currentUserPort = currentUserPort;
    }

    @GetMapping
    @Operation(summary = "Lista de amigos", description = "Perfiles públicos de los amigos del usuario autenticado.")
    public ResponseEntity<List<PublicUserProfile>> listFriends() {
        return ResponseEntity.ok(manageFriendshipsUseCase.listFriends(currentUserPort.getRequiredCurrentUserId()));
    }

    @GetMapping("/requests")
    @Operation(summary = "Solicitudes recibidas pendientes")
    public ResponseEntity<List<FriendRequestSummary>> listIncomingRequests() {
        return ResponseEntity.ok(manageFriendshipsUseCase.listIncomingRequests(currentUserPort.getRequiredCurrentUserId()));
    }

    @PostMapping("/requests")
    @Operation(summary = "Enviar solicitud de amistad por nickname")
    public ResponseEntity<FriendshipResponse> sendRequest(@Valid @RequestBody SendFriendRequest request) {
        Friendship friendship = manageFriendshipsUseCase.sendRequest(
                currentUserPort.getRequiredCurrentUserId(), request.nickname());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new FriendshipResponse(friendship.getId(), friendship.getStatus().name()));
    }

    @PostMapping("/requests/{id}/accept")
    @Operation(summary = "Aceptar una solicitud recibida")
    public ResponseEntity<Void> accept(@PathVariable String id) {
        manageFriendshipsUseCase.accept(currentUserPort.getRequiredCurrentUserId(), id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/requests/{id}/reject")
    @Operation(summary = "Rechazar una solicitud recibida")
    public ResponseEntity<Void> reject(@PathVariable String id) {
        manageFriendshipsUseCase.reject(currentUserPort.getRequiredCurrentUserId(), id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{nickname}")
    @Operation(summary = "Quitar una amistad o cancelar una solicitud propia")
    public ResponseEntity<Void> removeFriend(@PathVariable String nickname) {
        manageFriendshipsUseCase.removeFriend(currentUserPort.getRequiredCurrentUserId(), nickname);
        return ResponseEntity.noContent().build();
    }
}
