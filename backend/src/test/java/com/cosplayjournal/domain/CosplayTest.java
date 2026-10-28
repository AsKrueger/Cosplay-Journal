package com.cosplayjournal.domain;

import com.cosplayjournal.domain.exception.InvalidCosplayDataException;
import com.cosplayjournal.domain.model.Cosplay;
import com.cosplayjournal.domain.model.user.UserId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CosplayTest {

    @Test
    @DisplayName("Un cosplay se crea con nombre y foto opcional")
    void shouldCreateCosplayWithNameAndPhoto() {
        Cosplay cosplay = Cosplay.createNew("Goku", "https://img.example.com/goku.jpg", UserId.of("ana"));

        assertEquals("Goku", cosplay.getName());
        assertEquals("https://img.example.com/goku.jpg", cosplay.getPhotoUrl());
        assertTrue(cosplay.isOwnedBy(UserId.of("ana")));
    }

    @Test
    @DisplayName("La foto puede faltar")
    void photoIsOptional() {
        Cosplay cosplay = Cosplay.createNew("Goku", null);

        assertNull(cosplay.getPhotoUrl());
    }

    @Test
    @DisplayName("Rechaza nombres vacíos o demasiado largos")
    void rejectsInvalidName() {
        assertThrows(InvalidCosplayDataException.class, () -> Cosplay.createNew("", null));
        assertThrows(InvalidCosplayDataException.class, () -> Cosplay.createNew("   ", null));
        assertThrows(InvalidCosplayDataException.class, () -> Cosplay.createNew("x".repeat(101), null));
    }

    @Test
    @DisplayName("Cambiar la foto valida la URL")
    void changingPhotoValidatesUrl() {
        Cosplay cosplay = Cosplay.createNew("Goku", null);

        cosplay.changePhoto("https://img.example.com/nueva.jpg");
        assertEquals("https://img.example.com/nueva.jpg", cosplay.getPhotoUrl());

        assertThrows(InvalidCosplayDataException.class, () -> cosplay.changePhoto("file:///etc/passwd"));
    }
}
