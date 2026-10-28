package com.cosplayjournal.infrastructure.adapter.in.rest;

import com.cosplayjournal.application.dto.OwnProfile;
import com.cosplayjournal.application.port.in.ManageOwnProfileUseCase;
import com.cosplayjournal.application.port.out.CurrentUserPort;
import com.cosplayjournal.infrastructure.adapter.in.rest.dto.ChangeAvatarRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/me")
@Tag(name = "Mi perfil", description = "Perfil del usuario autenticado")
public class MyProfileController {

    private final ManageOwnProfileUseCase manageOwnProfileUseCase;
    private final CurrentUserPort currentUserPort;

    public MyProfileController(ManageOwnProfileUseCase manageOwnProfileUseCase, CurrentUserPort currentUserPort) {
        this.manageOwnProfileUseCase = manageOwnProfileUseCase;
        this.currentUserPort = currentUserPort;
    }

    @GetMapping
    @Operation(summary = "Mi perfil", description = "Nickname, email, foto y fecha de alta del usuario autenticado")
    public ResponseEntity<OwnProfile> myProfile() {
        return ResponseEntity.ok(manageOwnProfileUseCase.getOwnProfile(currentUserPort.getRequiredCurrentUserId()));
    }

    @PutMapping("/avatar")
    @Operation(summary = "Cambiar mi foto de perfil", description = "Una URL vacía elimina la foto")
    public ResponseEntity<OwnProfile> changeAvatar(@Valid @RequestBody ChangeAvatarRequest request) {
        return ResponseEntity.ok(manageOwnProfileUseCase.changeAvatar(
                currentUserPort.getRequiredCurrentUserId(), request.avatarUrl()));
    }
}
