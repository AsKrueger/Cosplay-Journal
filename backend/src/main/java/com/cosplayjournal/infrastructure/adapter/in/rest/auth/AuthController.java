package com.cosplayjournal.infrastructure.adapter.in.rest.auth;

import com.cosplayjournal.application.port.in.*;
import com.cosplayjournal.domain.model.user.User;
import com.cosplayjournal.infrastructure.adapter.in.rest.dto.AuthTokenResponse;
import com.cosplayjournal.infrastructure.adapter.in.rest.dto.LoginRequest;
import com.cosplayjournal.infrastructure.adapter.in.rest.dto.RegisterUserRequest;
import com.cosplayjournal.infrastructure.adapter.in.rest.dto.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Endpoints de registro e inicio de sesión de usuarios")
public class AuthController {

    private final RegisterUserUseCase registerUserUseCase;
    private final AuthenticateUserUseCase authenticateUserUseCase;

    public AuthController(RegisterUserUseCase registerUserUseCase, AuthenticateUserUseCase authenticateUserUseCase) {
        this.registerUserUseCase = registerUserUseCase;
        this.authenticateUserUseCase = authenticateUserUseCase;
    }

    @PostMapping("/register")
    @Operation(summary = "Registrar nuevo usuario", description = "Crea una cuenta de usuario con hash de contraseña seguro mediante BCrypt")
    @ApiResponse(responseCode = "201", description = "Usuario creado exitosamente")
    @ApiResponse(responseCode = "400", description = "Petición o campos inválidos")
    @ApiResponse(responseCode = "409", description = "Nombre de usuario o correo electrónico ya registrado")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterUserRequest request) {
        User user = registerUserUseCase.registerUser(
                new RegisterUserCommand(request.username(), request.email(), request.password())
        );

        UserResponse response = new UserResponse(
                user.getId().value(),
                user.getUsername(),
                user.getEmail(),
                user.getRole().name(),
                user.getStatus().name(),
                user.getCreatedAt()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión", description = "Autentica credenciales de usuario y retorna un token Bearer JWT")
    @ApiResponse(responseCode = "200", description = "Autenticación exitosa")
    @ApiResponse(responseCode = "400", description = "Formato de petición inválido")
    @ApiResponse(responseCode = "401", description = "Credenciales incorrectas")
    public ResponseEntity<AuthTokenResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthenticationResult result = authenticateUserUseCase.authenticateUser(
                new AuthenticateUserCommand(request.email(), request.password())
        );

        return ResponseEntity.ok(new AuthTokenResponse(result.token(), result.expiresInSeconds()));
    }
}
