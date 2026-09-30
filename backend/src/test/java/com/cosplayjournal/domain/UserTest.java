package com.cosplayjournal.domain;

import com.cosplayjournal.domain.exception.InvalidUserDataException;
import com.cosplayjournal.domain.model.user.User;
import com.cosplayjournal.domain.model.user.UserId;
import com.cosplayjournal.domain.model.user.UserRole;
import com.cosplayjournal.domain.model.user.UserStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    @DisplayName("Debe crear un usuario con datos válidos")
    void shouldCreateUserWithValidData() {
        UserId id = UserId.generate();
        User user = User.create(id, "alonso", "alonso@example.com", "$2a$10$hashedpassword");

        assertNotNull(user);
        assertEquals(id, user.getId());
        assertEquals("alonso", user.getUsername());
        assertEquals("alonso@example.com", user.getEmail());
        assertEquals(UserRole.USER, user.getRole());
        assertEquals(UserStatus.ACTIVE, user.getStatus());
    }

    @Test
    @DisplayName("Debe lanzar excepción si el correo es inválido")
    void shouldThrowExceptionWhenEmailIsInvalid() {
        UserId id = UserId.generate();
        assertThrows(InvalidUserDataException.class, () ->
                User.create(id, "alonso", "invalid-email", "hash")
        );
    }

    @Test
    @DisplayName("Debe lanzar excepción si el nombre de usuario es demasiado corto")
    void shouldThrowExceptionWhenUsernameIsTooShort() {
        UserId id = UserId.generate();
        assertThrows(InvalidUserDataException.class, () ->
                User.create(id, "al", "user@example.com", "hash")
        );
    }
}
