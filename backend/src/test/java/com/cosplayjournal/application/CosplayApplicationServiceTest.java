package com.cosplayjournal.application;

import com.cosplayjournal.application.port.in.CreateCosplayCommand;
import com.cosplayjournal.application.port.out.CosplayRepositoryPort;
import com.cosplayjournal.application.service.CosplayApplicationService;
import com.cosplayjournal.domain.exception.CosplayNotFoundException;
import com.cosplayjournal.domain.model.Cosplay;
import com.cosplayjournal.domain.model.CosplayStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CosplayApplicationServiceTest {

    @Mock
    private CosplayRepositoryPort cosplayRepositoryPort;

    private CosplayApplicationService cosplayApplicationService;

    @BeforeEach
    void setUp() {
        cosplayApplicationService = new CosplayApplicationService(cosplayRepositoryPort);
    }

    @Test
    @DisplayName("Debe crear un cosplay utilizando el puerto de salida")
    void shouldCreateCosplaySuccessfully() {
        CreateCosplayCommand command = new CreateCosplayCommand(
                "Batman", "Traje táctico", "Bruce Wayne", "DC Comics"
        );

        Cosplay savedCosplay = Cosplay.createNew(command.name(), command.description(), command.characterName(), command.originSeries())
                .withId(1L);

        when(cosplayRepositoryPort.save(any(Cosplay.class))).thenReturn(savedCosplay);

        Cosplay result = cosplayApplicationService.createCosplay(command);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Batman", result.getName());
        verify(cosplayRepositoryPort, times(1)).save(any(Cosplay.class));
    }

    @Test
    @DisplayName("Debe obtener un cosplay existente por ID")
    void shouldGetCosplayByIdWhenExists() {
        Cosplay cosplay = Cosplay.createNew("Naruto", "Modo Sabio", "Naruto Uzumaki", "Naruto Shippuden")
                .withId(2L);

        when(cosplayRepositoryPort.findById(2L)).thenReturn(Optional.of(cosplay));

        Cosplay result = cosplayApplicationService.getCosplayById(2L);

        assertNotNull(result);
        assertEquals(2L, result.getId());
        assertEquals("Naruto", result.getName());
    }

    @Test
    @DisplayName("Debe lanzar CosplayNotFoundException si el cosplay no existe")
    void shouldThrowExceptionWhenCosplayDoesNotExist() {
        when(cosplayRepositoryPort.findById(99L)).thenReturn(Optional.empty());

        assertThrows(CosplayNotFoundException.class, () -> cosplayApplicationService.getCosplayById(99L));
    }

    @Test
    @DisplayName("Debe obtener todos los cosplays")
    void shouldGetAllCosplays() {
        Cosplay c1 = Cosplay.createNew("C1", "D1", "Char1", "Series1").withId(1L);
        Cosplay c2 = Cosplay.createNew("C2", "D2", "Char2", "Series2").withId(2L);

        when(cosplayRepositoryPort.findAll()).thenReturn(List.of(c1, c2));

        List<Cosplay> list = cosplayApplicationService.getAllCosplays();

        assertEquals(2, list.size());
        verify(cosplayRepositoryPort, times(1)).findAll();
    }
}
