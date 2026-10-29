package com.cosplayjournal.application;

import com.cosplayjournal.application.dto.PublicUserProfile;
import com.cosplayjournal.application.port.out.PasswordHasherPort;
import com.cosplayjournal.application.port.out.TokenProviderPort;
import com.cosplayjournal.application.port.out.UserRepositoryPort;
import com.cosplayjournal.application.service.UserApplicationService;
import com.cosplayjournal.domain.exception.UserNotFoundException;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserPublicProfileTest {

    @Mock
    private UserRepositoryPort userRepositoryPort;
    @Mock
    private PasswordHasherPort passwordHasherPort;
    @Mock
    private TokenProviderPort tokenProviderPort;

    private UserApplicationService service;

    @BeforeEach
    void setUp() {
        service = new UserApplicationService(userRepositoryPort, passwordHasherPort, tokenProviderPort);
    }

    @Test
    @DisplayName("El perfil público devuelve nickname y foto, sin email")
    void publicProfileHasNoEmail() {
        User user = User.create(UserId.generate(), "cosplayer_1", "secreto@example.com", "hash");
        user.changeAvatar("https://img.example.com/yo.jpg");
        when(userRepositoryPort.findByUsername("cosplayer_1")).thenReturn(Optional.of(user));

        PublicUserProfile profile = service.getPublicProfile("cosplayer_1");

        assertEquals("cosplayer_1", profile.nickname());
        assertEquals("https://img.example.com/yo.jpg", profile.avatarUrl());
        assertNotNull(profile.memberSince());
        assertFalse(profile.toString().contains("secreto@example.com"));
    }

    @Test
    @DisplayName("Un nickname inexistente lanza UserNotFoundException")
    void unknownNicknameIsNotFound() {
        when(userRepositoryPort.findByUsername("nadie")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> service.getPublicProfile("nadie"));
    }
}
