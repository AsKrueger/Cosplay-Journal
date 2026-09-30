package com.cosplayjournal.domain.model.user;

import com.cosplayjournal.domain.exception.InvalidUserDataException;

import java.time.Instant;
import java.util.Objects;

public class User {

    private final UserId id;
    private String username;
    private String email;
    private String passwordHash;
    private UserRole role;
    private UserStatus status;
    private final Instant createdAt;
    private Instant updatedAt;

    public User(
            UserId id,
            String username,
            String email,
            String passwordHash,
            UserRole role,
            UserStatus status,
            Instant createdAt,
            Instant updatedAt
    ) {
        validateUsername(username);
        validateEmail(email);
        if (id == null) {
            throw new InvalidUserDataException("El ID del usuario no puede ser nulo");
        }
        if (passwordHash == null || passwordHash.trim().isEmpty()) {
            throw new InvalidUserDataException("El hash de contraseña es obligatorio");
        }

        this.id = id;
        this.username = username.trim();
        this.email = email.trim().toLowerCase();
        this.passwordHash = passwordHash;
        this.role = role != null ? role : UserRole.USER;
        this.status = status != null ? status : UserStatus.ACTIVE;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.updatedAt = updatedAt != null ? updatedAt : this.createdAt;
    }

    public static User create(
            UserId id,
            String username,
            String email,
            String passwordHash
    ) {
        return new User(id, username, email, passwordHash, UserRole.USER, UserStatus.ACTIVE, Instant.now(), Instant.now());
    }

    public void disable() {
        this.status = UserStatus.DISABLED;
        this.updatedAt = Instant.now();
    }

    public void activate() {
        this.status = UserStatus.ACTIVE;
        this.updatedAt = Instant.now();
    }

    private static void validateUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new InvalidUserDataException("El nombre de usuario es obligatorio");
        }
        String trimmed = username.trim();
        if (trimmed.length() < 3 || trimmed.length() > 50) {
            throw new InvalidUserDataException("El nombre de usuario debe tener entre 3 y 50 caracteres");
        }
    }

    private static void validateEmail(String email) {
        if (email == null || email.trim().isEmpty() || !email.contains("@")) {
            throw new InvalidUserDataException("El formato del correo electrónico no es válido");
        }
    }

    public UserId getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public UserRole getRole() {
        return role;
    }

    public UserStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", role=" + role +
                '}';
    }
}
