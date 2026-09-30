package com.cosplayjournal.infrastructure.adapter.out.security;

import com.cosplayjournal.domain.model.user.User;
import com.cosplayjournal.domain.model.user.UserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderAdapterTest {

    private JwtTokenProviderAdapter jwtTokenProviderAdapter;

    @BeforeEach
    void setUp() {
        String secretKey = "superSecretKeyForTestingCosplayJournalBackend2026!";
        long expirationInMs = 3600000L;
        jwtTokenProviderAdapter = new JwtTokenProviderAdapter(secretKey, expirationInMs);
    }

    @Test
    @DisplayName("Debe generar un token JWT válido y extraer los claims correctamente")
    void shouldGenerateAndValidateToken() {
        UserId userId = UserId.of("usr-12345");
        User user = User.create(userId, "beatriz", "beatriz@example.com", "$2a$10$hash");

        String token = jwtTokenProviderAdapter.generateToken(user);

        assertNotNull(token);
        assertTrue(jwtTokenProviderAdapter.validateToken(token));
        assertEquals("usr-12345", jwtTokenProviderAdapter.getUserIdFromToken(token));
        assertEquals("beatriz", jwtTokenProviderAdapter.getUsernameFromToken(token));
        assertEquals("USER", jwtTokenProviderAdapter.getRoleFromToken(token));
    }

    @Test
    @DisplayName("Debe retornar false para un token JWT inválido o manipulado")
    void shouldReturnFalseForInvalidToken() {
        assertFalse(jwtTokenProviderAdapter.validateToken("invalid.token.str"));
    }
}
