package com.cosplayjournal.application;

import com.cosplayjournal.application.port.in.AuthenticateUserCommand;
import com.cosplayjournal.application.port.in.AuthenticationResult;
import com.cosplayjournal.application.port.in.RegisterUserCommand;
import com.cosplayjournal.application.port.out.PasswordHasherPort;
import com.cosplayjournal.application.port.out.TokenProviderPort;
import com.cosplayjournal.application.port.out.UserRepositoryPort;
import com.cosplayjournal.application.service.UserApplicationService;
import com.cosplayjournal.domain.exception.UserAlreadyExistsException;
import com.cosplayjournal.domain.model.user.User;
import com.cosplayjournal.domain.model.user.UserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserApplicationServiceTest {

    @Mock
    private UserRepositoryPort userRepositoryPort;

    @Mock
    private PasswordHasherPort passwordHasherPort;

    @Mock
    private TokenProviderPort tokenProviderPort;

    private UserApplicationService userApplicationService;

    @BeforeEach
    void setUp() {
        userApplicationService = new UserApplicationService(userRepositoryPort, passwordHasherPort, tokenProviderPort);
    }

    @Test
    @DisplayName("Debe registrar un usuario correctamente cuando no existe duplicado")
    void shouldRegisterUserSuccessfully() {
        RegisterUserCommand command = new RegisterUserCommand("alonso", "alonso@example.com", "secret123");

        when(userRepositoryPort.existsByEmail("alonso@example.com")).thenReturn(false);
        when(userRepositoryPort.existsByUsername("alonso")).thenReturn(false);
        when(passwordHasherPort.hash("secret123")).thenReturn("$2a$10$hashed");
        when(userRepositoryPort.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userApplicationService.registerUser(command);

        assertNotNull(result);
        assertEquals("alonso", result.getUsername());
        assertEquals("alonso@example.com", result.getEmail());
        verify(userRepositoryPort, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Debe lanzar UserAlreadyExistsException si el correo ya existe")
    void shouldThrowExceptionWhenEmailAlreadyExists() {
        RegisterUserCommand command = new RegisterUserCommand("alonso", "alonso@example.com", "secret123");
        when(userRepositoryPort.existsByEmail("alonso@example.com")).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> userApplicationService.registerUser(command));
    }

    @Test
    @DisplayName("Debe autenticar un usuario con credenciales correctas y devolver JWT")
    void shouldAuthenticateUserSuccessfully() {
        AuthenticateUserCommand command = new AuthenticateUserCommand("alonso@example.com", "secret123");
        User user = User.create(UserId.generate(), "alonso", "alonso@example.com", "$2a$10$hashed");

        when(userRepositoryPort.findByEmail("alonso@example.com")).thenReturn(Optional.of(user));
        when(passwordHasherPort.matches("secret123", "$2a$10$hashed")).thenReturn(true);
        when(tokenProviderPort.generateToken(user)).thenReturn("mock.jwt.token");

        AuthenticationResult result = userApplicationService.authenticateUser(command);

        assertNotNull(result);
        assertEquals("mock.jwt.token", result.token());
        assertEquals("alonso", result.user().getUsername());
    }
}
