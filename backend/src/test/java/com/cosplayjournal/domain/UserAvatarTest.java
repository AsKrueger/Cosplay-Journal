package com.cosplayjournal.domain;

import com.cosplayjournal.domain.exception.InvalidUserDataException;
import com.cosplayjournal.domain.model.user.User;
import com.cosplayjournal.domain.model.user.UserId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserAvatarTest {

    @Test
    @DisplayName("Un usuario sin foto la tiene a null y puede añadirla con una URL https")
    void shouldAcceptHttpsAvatar() {
        User user = User.create(UserId.generate(), "cosplayer_1", "a@example.com", "hash");
        assertNull(user.getAvatarUrl());

        user.changeAvatar("https://img.example.com/yo.jpg");

        assertEquals("https://img.example.com/yo.jpg", user.getAvatarUrl());
    }

    @Test
    @DisplayName("Una URL vacía elimina la foto")
    void blankAvatarRemovesPhoto() {
        User user = User.create(UserId.generate(), "cosplayer_1", "a@example.com", "hash");
        user.changeAvatar("https://img.example.com/yo.jpg");

        user.changeAvatar("   ");

        assertNull(user.getAvatarUrl());
    }

    @Test
    @DisplayName("Rechaza esquemas que no son http o https")
    void rejectsNonWebScheme() {
        User user = User.create(UserId.generate(), "cosplayer_1", "a@example.com", "hash");

        assertThrows(InvalidUserDataException.class, () -> user.changeAvatar("file:///etc/passwd"));
    }
}
