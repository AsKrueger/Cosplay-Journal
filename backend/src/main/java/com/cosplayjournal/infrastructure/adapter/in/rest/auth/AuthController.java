package com.cosplayjournal.infrastructure.adapter.in.rest.auth;

import com.cosplayjournal.application.port.in.*;
import com.cosplayjournal.domain.model.user.User;
import com.cosplayjournal.infrastructure.adapter.in.rest.dto.AuthTokenResponse;
import com.cosplayjournal.infrastructure.adapter.in.rest.dto.LoginRequest;
import com.cosplayjournal.infrastructure.adapter.in.rest.dto.RegisterUserRequest;
import com.cosplayjournal.infrastructure.adapter.in.rest.dto.UserResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final RegisterUserUseCase registerUserUseCase;
    private final AuthenticateUserUseCase authenticateUserUseCase;

    public AuthController(RegisterUserUseCase registerUserUseCase, AuthenticateUserUseCase authenticateUserUseCase) {
        this.registerUserUseCase = registerUserUseCase;
        this.authenticateUserUseCase = authenticateUserUseCase;
    }

    @PostMapping("/register")
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
    public ResponseEntity<AuthTokenResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthenticationResult result = authenticateUserUseCase.authenticateUser(
                new AuthenticateUserCommand(request.email(), request.password())
        );

        return ResponseEntity.ok(new AuthTokenResponse(result.token(), result.expiresInSeconds()));
    }
}
