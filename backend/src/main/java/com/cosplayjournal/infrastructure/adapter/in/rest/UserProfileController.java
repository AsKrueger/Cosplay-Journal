package com.cosplayjournal.infrastructure.adapter.in.rest;

import com.cosplayjournal.application.dto.PublicUserProfile;
import com.cosplayjournal.application.port.in.GetPublicProfileUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Usuarios", description = "Perfiles públicos de cosplayers")
public class UserProfileController {

    private final GetPublicProfileUseCase getPublicProfileUseCase;

    public UserProfileController(GetPublicProfileUseCase getPublicProfileUseCase) {
        this.getPublicProfileUseCase = getPublicProfileUseCase;
    }

    @GetMapping("/{nickname}")
    @Operation(summary = "Perfil público por nickname", description = "Devuelve nickname, foto y fecha de alta. No devuelve email ni datos de contacto.")
    @ApiResponse(responseCode = "200", description = "Perfil público")
    @ApiResponse(responseCode = "404", description = "No existe ningún usuario con ese nickname")
    public ResponseEntity<PublicUserProfile> getProfile(@PathVariable String nickname) {
        return ResponseEntity.ok(getPublicProfileUseCase.getPublicProfile(nickname));
    }
}
