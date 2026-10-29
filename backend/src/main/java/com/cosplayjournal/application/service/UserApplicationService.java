package com.cosplayjournal.application.service;

import com.cosplayjournal.application.port.in.*;
import com.cosplayjournal.application.port.out.PasswordHasherPort;
import com.cosplayjournal.application.port.out.TokenProviderPort;
import com.cosplayjournal.application.port.out.UserRepositoryPort;
import com.cosplayjournal.application.dto.OwnProfile;
import com.cosplayjournal.application.dto.PublicUserProfile;
import com.cosplayjournal.domain.exception.UserAlreadyExistsException;
import com.cosplayjournal.domain.exception.UserNotFoundException;
import com.cosplayjournal.domain.model.user.User;
import com.cosplayjournal.domain.model.user.UserId;

public class UserApplicationService implements RegisterUserUseCase, AuthenticateUserUseCase, GetPublicProfileUseCase, ManageOwnProfileUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final PasswordHasherPort passwordHasherPort;
    private final TokenProviderPort tokenProviderPort;

    public UserApplicationService(
            UserRepositoryPort userRepositoryPort,
            PasswordHasherPort passwordHasherPort,
            TokenProviderPort tokenProviderPort
    ) {
        this.userRepositoryPort = userRepositoryPort;
        this.passwordHasherPort = passwordHasherPort;
        this.tokenProviderPort = tokenProviderPort;
    }

    @Override
    public User registerUser(RegisterUserCommand command) {
        if (userRepositoryPort.existsByEmail(command.email())) {
            throw new UserAlreadyExistsException("Ya existe un usuario registrado con el correo: " + command.email());
        }
        if (userRepositoryPort.existsByUsername(command.username())) {
            throw new UserAlreadyExistsException("Ya existe un usuario registrado con el nombre de usuario: " + command.username());
        }

        String passwordHash = passwordHasherPort.hash(command.rawPassword());
        User user = User.create(UserId.generate(), command.username(), command.email(), passwordHash);

        return userRepositoryPort.save(user);
    }

    @Override
    public AuthenticationResult authenticateUser(AuthenticateUserCommand command) {
        User user = userRepositoryPort.findByEmail(command.email().trim().toLowerCase())
                .orElseThrow(() -> new IllegalArgumentException("Credenciales inválidas"));

        if (!passwordHasherPort.matches(command.rawPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Credenciales inválidas");
        }

        String token = tokenProviderPort.generateToken(user);
        return new AuthenticationResult(user, token, 3600L);
    }

    @Override
    public PublicUserProfile getPublicProfile(String nickname) {
        User user = userRepositoryPort.findByUsername(nickname)
                .orElseThrow(() -> new UserNotFoundException("No existe ningún usuario con el nickname: " + nickname));
        return new PublicUserProfile(user.getUsername(), user.getAvatarUrl(), user.getCreatedAt());
    }

    @Override
    public OwnProfile getOwnProfile(UserId userId) {
        return toOwnProfile(findById(userId));
    }

    @Override
    public OwnProfile changeAvatar(UserId userId, String avatarUrl) {
        User user = findById(userId);
        user.changeAvatar(avatarUrl);
        return toOwnProfile(userRepositoryPort.save(user));
    }

    private User findById(UserId userId) {
        return userRepositoryPort.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));
    }

    private OwnProfile toOwnProfile(User user) {
        return new OwnProfile(user.getUsername(), user.getEmail(), user.getAvatarUrl(), user.getCreatedAt());
    }
}
