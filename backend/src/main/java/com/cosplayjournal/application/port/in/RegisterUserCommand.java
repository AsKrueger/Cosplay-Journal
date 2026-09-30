package com.cosplayjournal.application.port.in;

public record RegisterUserCommand(
        String username,
        String email,
        String rawPassword
) {
    public RegisterUserCommand {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de usuario es obligatorio");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("El correo electrónico es obligatorio");
        }
        if (rawPassword == null || rawPassword.trim().isEmpty() || rawPassword.length() < 6) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 6 caracteres");
        }
    }
}
