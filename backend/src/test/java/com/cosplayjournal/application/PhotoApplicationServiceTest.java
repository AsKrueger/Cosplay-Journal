package com.cosplayjournal.application;

import com.cosplayjournal.application.port.in.AddPhotoCommand;
import com.cosplayjournal.application.port.out.CurrentUserPort;
import com.cosplayjournal.application.port.out.DomainEventPublisherPort;
import com.cosplayjournal.application.port.out.ParticipationRepositoryPort;
import com.cosplayjournal.application.port.out.PhotoRepositoryPort;
import com.cosplayjournal.application.service.PhotoApplicationService;
import com.cosplayjournal.domain.event.PhotoUploadedEvent;
import com.cosplayjournal.domain.exception.ForbiddenAccessException;
import com.cosplayjournal.domain.exception.ParticipationNotFoundException;
import com.cosplayjournal.domain.model.event.EventId;
import com.cosplayjournal.domain.model.participation.*;
import com.cosplayjournal.domain.model.photo.Photo;
import com.cosplayjournal.domain.model.user.UserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PhotoApplicationServiceTest {

    @Mock
    private PhotoRepositoryPort photoRepositoryPort;

    @Mock
    private ParticipationRepositoryPort participationRepositoryPort;

    @Mock
    private DomainEventPublisherPort eventPublisherPort;

    @Mock
    private CurrentUserPort currentUserPort;

    private PhotoApplicationService photoApplicationService;

    @BeforeEach
    void setUp() {
        photoApplicationService = new PhotoApplicationService(photoRepositoryPort, participationRepositoryPort, eventPublisherPort, currentUserPort);
    }

    @Test
    @DisplayName("Debe añadir una fotografía asignando uploaderUserId del usuario autenticado si es participante")
    void shouldAddPhotoSuccessfully() {
        ParticipationId participationId = ParticipationId.of("part-photo-1");
        UserId userId = UserId.of("u1");

        Participation participation = Participation.create(
                participationId, userId, EventId.of("evt-1"), 10L, ParticipationType.INDIVIDUAL, Participant.createLeader("u1", "Carlos")
        );

        when(participationRepositoryPort.findById(participationId)).thenReturn(Optional.of(participation));
        when(currentUserPort.getRequiredCurrentUserId()).thenReturn(userId);
        when(photoRepositoryPort.save(any(Photo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AddPhotoCommand command = new AddPhotoCommand(
                participationId, "s3://cosplay/img.jpg", "Foto grupal", "u1"
        );

        Photo photo = photoApplicationService.addPhoto(command);

        assertNotNull(photo);
        assertEquals("part-photo-1", photo.getParticipationId().value());
        assertEquals("s3://cosplay/img.jpg", photo.getStorageReference());
        assertEquals("u1", photo.getUploadedByUserId());

        verify(eventPublisherPort, times(1)).publish(any(PhotoUploadedEvent.class));
    }

    @Test
    @DisplayName("Debe lanzar ForbiddenAccessException si un usuario ajeno a la participación intenta subir fotos")
    void shouldThrowExceptionWhenUserIsNotParticipant() {
        ParticipationId participationId = ParticipationId.of("part-photo-1");
        UserId strangerUser = UserId.of("stranger");

        Participation participation = Participation.create(
                participationId, UserId.of("creator"), EventId.of("evt-1"), 10L, ParticipationType.INDIVIDUAL, Participant.createLeader("creator", "Carlos")
        );

        when(participationRepositoryPort.findById(participationId)).thenReturn(Optional.of(participation));
        when(currentUserPort.getRequiredCurrentUserId()).thenReturn(strangerUser);
        when(currentUserPort.isAdmin()).thenReturn(false);

        AddPhotoCommand command = new AddPhotoCommand(
                participationId, "s3://cosplay/img.jpg", "Foto intruza", "stranger"
        );

        assertThrows(ForbiddenAccessException.class, () -> photoApplicationService.addPhoto(command));
    }
}
