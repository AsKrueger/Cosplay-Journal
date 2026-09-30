package com.cosplayjournal.application;

import com.cosplayjournal.application.port.in.AddPhotoCommand;
import com.cosplayjournal.application.port.out.DomainEventPublisherPort;
import com.cosplayjournal.application.port.out.ParticipationRepositoryPort;
import com.cosplayjournal.application.port.out.PhotoRepositoryPort;
import com.cosplayjournal.application.service.PhotoApplicationService;
import com.cosplayjournal.domain.event.PhotoUploadedEvent;
import com.cosplayjournal.domain.exception.ParticipationNotFoundException;
import com.cosplayjournal.domain.exception.PhotoNotFoundException;
import com.cosplayjournal.domain.model.event.EventId;
import com.cosplayjournal.domain.model.participation.*;
import com.cosplayjournal.domain.model.photo.Photo;
import com.cosplayjournal.domain.model.photo.PhotoId;
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
class PhotoApplicationServiceTest {

    @Mock
    private PhotoRepositoryPort photoRepositoryPort;

    @Mock
    private ParticipationRepositoryPort participationRepositoryPort;

    @Mock
    private DomainEventPublisherPort eventPublisherPort;

    private PhotoApplicationService photoApplicationService;

    @BeforeEach
    void setUp() {
        photoApplicationService = new PhotoApplicationService(photoRepositoryPort, participationRepositoryPort, eventPublisherPort);
    }

    @Test
    @DisplayName("Debe añadir una fotografía si la participación existe")
    void shouldAddPhotoSuccessfully() {
        ParticipationId participationId = ParticipationId.of("part-photo-1");
        Participation participation = Participation.create(
                participationId, EventId.of("evt-1"), 10L, ParticipationType.INDIVIDUAL, Participant.createLeader("u1", "Carlos")
        );

        when(participationRepositoryPort.findById(participationId)).thenReturn(Optional.of(participation));
        when(photoRepositoryPort.save(any(Photo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AddPhotoCommand command = new AddPhotoCommand(
                participationId, "s3://cosplay/img.jpg", "Foto grupal", "u1"
        );

        Photo photo = photoApplicationService.addPhoto(command);

        assertNotNull(photo);
        assertEquals("part-photo-1", photo.getParticipationId().value());
        assertEquals("s3://cosplay/img.jpg", photo.getStorageReference());

        verify(eventPublisherPort, times(1)).publish(any(PhotoUploadedEvent.class));
    }

    @Test
    @DisplayName("Debe lanzar ParticipationNotFoundException si la participación no existe al subir foto")
    void shouldThrowExceptionWhenParticipationNotFound() {
        ParticipationId participationId = ParticipationId.of("part-999");
        when(participationRepositoryPort.findById(participationId)).thenReturn(Optional.empty());

        AddPhotoCommand command = new AddPhotoCommand(
                participationId, "s3://cosplay/img.jpg", "Foto grupal", "u1"
        );

        assertThrows(ParticipationNotFoundException.class, () -> photoApplicationService.addPhoto(command));
    }
}
