package com.cosplayjournal.application;

import com.cosplayjournal.application.port.in.CreateCosplayCommand;
import com.cosplayjournal.application.port.out.CosplayRepositoryPort;
import com.cosplayjournal.application.port.out.CurrentUserPort;
import com.cosplayjournal.application.port.out.DomainEventPublisherPort;
import com.cosplayjournal.application.service.CosplayApplicationService;
import com.cosplayjournal.domain.event.CosplayCreatedEvent;
import com.cosplayjournal.domain.exception.CosplayNotFoundException;
import com.cosplayjournal.domain.model.Cosplay;
import com.cosplayjournal.domain.model.user.UserId;
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

    @Mock
    private DomainEventPublisherPort eventPublisherPort;

    @Mock
    private CurrentUserPort currentUserPort;

    private CosplayApplicationService service;
    private final UserId owner = UserId.of("ana");

    @BeforeEach
    void setUp() {
        service = new CosplayApplicationService(cosplayRepositoryPort, eventPublisherPort, currentUserPort);
    }

    @Test
    @DisplayName("Crear un cosplay guarda nombre y foto, asigna al usuario actual y publica el evento")
    void createCosplaySavesAndPublishes() {
        when(currentUserPort.getRequiredCurrentUserId()).thenReturn(owner);
        when(cosplayRepositoryPort.save(any(Cosplay.class))).thenAnswer(invocation -> ((Cosplay) invocation.getArgument(0)).withId(7L));

        Cosplay saved = service.createCosplay(new CreateCosplayCommand("Naruto", "https://img.example.com/naruto.jpg"));

        assertEquals(7L, saved.getId());
        assertEquals("Naruto", saved.getName());
        assertEquals(owner, saved.getOwnerId());
        verify(eventPublisherPort).publish(any(CosplayCreatedEvent.class));
    }

    @Test
    @DisplayName("Un id inexistente lanza CosplayNotFoundException")
    void unknownIdIsNotFound() {
        when(cosplayRepositoryPort.findById(99L)).thenReturn(Optional.empty());

        assertThrows(CosplayNotFoundException.class, () -> service.getCosplayById(99L));
    }

    @Test
    @DisplayName("Listar devuelve todos los cosplays del repositorio")
    void listsAll() {
        when(cosplayRepositoryPort.findAll()).thenReturn(List.of(
                Cosplay.createNew("C1", null).withId(1L),
                Cosplay.createNew("C2", null).withId(2L)
        ));

        assertEquals(2, service.getAllCosplays().size());
    }
}
