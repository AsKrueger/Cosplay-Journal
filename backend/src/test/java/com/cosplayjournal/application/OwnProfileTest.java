package com.cosplayjournal.application;

import com.cosplayjournal.application.dto.OwnProfile;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OwnProfileTest {

    @Mock
    private UserRepositoryPort userRepositoryPort;
    @Mock
    private PasswordHasherPort passwordHasherPort;
    @Mock
    private TokenProviderPort tokenProviderPort;

    private UserApplicationService service;
    private final UserId ana = UserId.of("ana");

    @BeforeEach
    void setUp() {
        service = new UserApplicationService(userRepositoryPort, passwordHasherPort, tokenProviderPort);
    }

    @Test
    @DisplayName("Mi perfil incluye mi email, que solo veo yo")
    void ownProfileIncludesEmail() {
        when(userRepositoryPort.findById(ana)).thenReturn(Optional.of(User.create(ana, "ana_1", "ana@example.com", "hash")));

        OwnProfile profile = service.getOwnProfile(ana);

        assertEquals("ana_1", profile.nickname());
        assertEquals("ana@example.com", profile.email());
    }

    @Test
    @DisplayName("Cambiar la foto guarda la nueva URL")
    void changeAvatarPersists() {
        User user = User.create(ana, "ana_1", "ana@example.com", "hash");
        when(userRepositoryPort.findById(ana)).thenReturn(Optional.of(user));
        when(userRepositoryPort.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OwnProfile profile = service.changeAvatar(ana, "https://img.example.com/ana.jpg");

        assertEquals("https://img.example.com/ana.jpg", profile.avatarUrl());
        verify(userRepositoryPort).save(user);
    }

    @Test
    @DisplayName("Un usuario inexistente lanza UserNotFoundException")
    void missingUserIsNotFound() {
        when(userRepositoryPort.findById(ana)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> service.getOwnProfile(ana));
    }
}
