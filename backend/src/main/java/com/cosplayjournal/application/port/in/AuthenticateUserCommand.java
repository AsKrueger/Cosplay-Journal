package com.cosplayjournal.application.port.in;

public record AuthenticateUserCommand(
        String email,
        String rawPassword
) {
    public AuthenticateUserCommand {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("El correo electrónico es obligatorio");
        }
        if (rawPassword == null || rawPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("La contraseña es obligatoria");
        }
    }
}
