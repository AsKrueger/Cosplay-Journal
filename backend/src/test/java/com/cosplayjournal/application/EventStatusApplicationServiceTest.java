package com.cosplayjournal.application;

import com.cosplayjournal.application.port.out.UserEventStatusRepositoryPort;
import com.cosplayjournal.application.service.EventStatusApplicationService;
import com.cosplayjournal.domain.exception.InvalidUserDataException;
import com.cosplayjournal.domain.model.social.UserEventStatus;
import com.cosplayjournal.domain.model.user.UserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventStatusApplicationServiceTest {

    @Mock
    private UserEventStatusRepositoryPort repositoryPort;

    private EventStatusApplicationService service;
    private final UserId ana = UserId.of("ana");

    @BeforeEach
    void setUp() {
        service = new EventStatusApplicationService(repositoryPort);
    }

    @Test
    @DisplayName("Guardar un estado persiste la participación del usuario")
    void savesStatus() {
        service.setStatus(ana, "evt-1", "ATTENDING");

        verify(repositoryPort).save(argThat(s -> s.userId().equals(ana) && s.eventId().equals("evt-1") && s.status().equals("ATTENDING")));
    }

    @Test
    @DisplayName("NONE borra la selección en lugar de guardarla")
    void noneDeletes() {
        service.setStatus(ana, "evt-1", "NONE");

        verify(repositoryPort).delete(ana, "evt-1");
        verify(repositoryPort, never()).save(any());
    }

    @Test
    @DisplayName("Un estado desconocido no se acepta")
    void rejectsUnknownStatus() {
        assertThrows(InvalidUserDataException.class,
                () -> new UserEventStatus(ana, "evt-1", "EN_SECRETO", Instant.now()));
    }

    @Test
    @DisplayName("Los estados válidos se reconocen como guardables")
    void storableStatuses() {
        assertTrue(UserEventStatus.isStorable("ATTENDED"));
        assertTrue(UserEventStatus.isStorable("PLANNING"));
        assertFalse(UserEventStatus.isStorable("NONE"));
    }
}
