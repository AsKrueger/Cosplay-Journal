package com.cosplayjournal.domain;

import com.cosplayjournal.domain.exception.InvalidCosplayDataException;
import com.cosplayjournal.domain.exception.InvalidStateTransitionException;
import com.cosplayjournal.domain.model.Cosplay;
import com.cosplayjournal.domain.model.CosplayStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CosplayTest {

    @Test
    @DisplayName("Debe crear un cosplay con datos válidos")
    void shouldCreateCosplayWithValidData() {
        Cosplay cosplay = Cosplay.createNew(
                "Spider-Man Classic",
                "Traje rojo y azul de Peter Parker",
                "Spider-Man",
                "Marvel Comics"
        );

        assertNotNull(cosplay);
        assertNull(cosplay.getId());
        assertEquals("Spider-Man Classic", cosplay.getName());
        assertEquals("Traje rojo y azul de Peter Parker", cosplay.getDescription());
        assertEquals("Spider-Man", cosplay.getCharacterName());
        assertEquals("Marvel Comics", cosplay.getOriginSeries());
        assertEquals(CosplayStatus.IDEA, cosplay.getStatus());
        assertNotNull(cosplay.getCreatedAt());
        assertNotNull(cosplay.getUpdatedAt());
    }

    @Test
    @DisplayName("Debe lanzar excepción si el nombre es nulo o vacío")
    void shouldThrowExceptionWhenNameIsInvalid() {
        assertThrows(InvalidCosplayDataException.class, () ->
                Cosplay.createNew("", "Descripción", "Personaje", "Serie")
        );

        assertThrows(InvalidCosplayDataException.class, () ->
                Cosplay.createNew("   ", "Descripción", "Personaje", "Serie")
        );

        assertThrows(InvalidCosplayDataException.class, () ->
                Cosplay.createNew(null, "Descripción", "Personaje", "Serie")
        );
    }

    @Test
    @DisplayName("Debe permitir transiciones válidas de estado (IDEA -> IN_PLANNING -> IN_PROGRESS -> COMPLETED -> ARCHIVED)")
    void shouldAllowValidStatusTransitions() {
        Cosplay cosplay = Cosplay.createNew("Goku", "Saiya-jin", "Goku", "Dragon Ball");
        assertEquals(CosplayStatus.IDEA, cosplay.getStatus());

        cosplay.changeStatus(CosplayStatus.IN_PLANNING);
        assertEquals(CosplayStatus.IN_PLANNING, cosplay.getStatus());

        cosplay.changeStatus(CosplayStatus.IN_PROGRESS);
        assertEquals(CosplayStatus.IN_PROGRESS, cosplay.getStatus());

        cosplay.changeStatus(CosplayStatus.COMPLETED);
        assertEquals(CosplayStatus.COMPLETED, cosplay.getStatus());

        cosplay.changeStatus(CosplayStatus.ARCHIVED);
        assertEquals(CosplayStatus.ARCHIVED, cosplay.getStatus());
    }

    @Test
    @DisplayName("Debe lanzar InvalidStateTransitionException ante transiciones inválidas (ej: COMPLETED -> IDEA)")
    void shouldThrowExceptionForInvalidTransitions() {
        Cosplay cosplay = Cosplay.createNew("Goku", "Saiya-jin", "Goku", "Dragon Ball");
        cosplay.changeStatus(CosplayStatus.IN_PROGRESS);
        cosplay.changeStatus(CosplayStatus.COMPLETED);

        // De COMPLETED no se puede pasar directamente a IDEA ni a IN_PLANNING
        assertThrows(InvalidStateTransitionException.class, () -> cosplay.changeStatus(CosplayStatus.IDEA));
        assertThrows(InvalidStateTransitionException.class, () -> cosplay.changeStatus(CosplayStatus.IN_PLANNING));
    }

    @Test
    @DisplayName("Debe lanzar excepción si el nuevo estado es nulo")
    void shouldThrowExceptionWhenUpdatingToNullStatus() {
        Cosplay cosplay = Cosplay.createNew("Goku", "Saiya-jin", "Goku", "Dragon Ball");

        assertThrows(InvalidCosplayDataException.class, () -> cosplay.changeStatus(null));
    }
}
