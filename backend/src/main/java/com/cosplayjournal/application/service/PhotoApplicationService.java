package com.cosplayjournal.application.service;

import com.cosplayjournal.application.port.in.AddPhotoCommand;
import com.cosplayjournal.application.port.in.AddPhotoUseCase;
import com.cosplayjournal.application.port.in.GetParticipationPhotosUseCase;
import com.cosplayjournal.application.port.in.GetPhotoUseCase;
import com.cosplayjournal.application.port.out.CurrentUserPort;
import com.cosplayjournal.application.port.out.DomainEventPublisherPort;
import com.cosplayjournal.application.port.out.ParticipationRepositoryPort;
import com.cosplayjournal.application.port.out.PhotoRepositoryPort;
import com.cosplayjournal.domain.event.PhotoUploadedEvent;
import com.cosplayjournal.domain.exception.ForbiddenAccessException;
import com.cosplayjournal.domain.exception.ParticipationNotFoundException;
import com.cosplayjournal.domain.exception.PhotoNotFoundException;
import com.cosplayjournal.domain.model.participation.Participation;
import com.cosplayjournal.domain.model.participation.ParticipationId;
import com.cosplayjournal.domain.model.photo.Photo;
import com.cosplayjournal.domain.model.photo.PhotoId;
import com.cosplayjournal.domain.model.user.UserId;

import java.util.List;

public class PhotoApplicationService implements AddPhotoUseCase, GetPhotoUseCase, GetParticipationPhotosUseCase {

    private final PhotoRepositoryPort photoRepositoryPort;
    private final ParticipationRepositoryPort participationRepositoryPort;
    private final DomainEventPublisherPort eventPublisherPort;
    private final CurrentUserPort currentUserPort;

    public PhotoApplicationService(
            PhotoRepositoryPort photoRepositoryPort,
            ParticipationRepositoryPort participationRepositoryPort,
            DomainEventPublisherPort eventPublisherPort,
            CurrentUserPort currentUserPort
    ) {
        this.photoRepositoryPort = photoRepositoryPort;
        this.participationRepositoryPort = participationRepositoryPort;
        this.eventPublisherPort = eventPublisherPort;
        this.currentUserPort = currentUserPort;
    }

    public PhotoApplicationService(
            PhotoRepositoryPort photoRepositoryPort,
            ParticipationRepositoryPort participationRepositoryPort
    ) {
        this(
                photoRepositoryPort, participationRepositoryPort, event -> {},
                new CurrentUserPort() {
                    @Override public java.util.Optional<UserId> getCurrentUserId() { return java.util.Optional.of(UserId.of("system-default")); }
                    @Override public UserId getRequiredCurrentUserId() { return UserId.of("system-default"); }
                    @Override public boolean isAuthenticated() { return true; }
                    @Override public boolean isAdmin() { return false; }
                }
        );
    }

    @Override
    public Photo addPhoto(AddPhotoCommand command) {
        Participation participation = participationRepositoryPort.findById(command.participationId())
                .orElseThrow(() -> new ParticipationNotFoundException(command.participationId()));

        UserId currentUserId = currentUserPort.getRequiredCurrentUserId();

        // Verificar que el usuario pertenece a la participación, la creó o es ADMIN
        if (!participation.isParticipant(currentUserId.value()) && !participation.isCreatedBy(currentUserId) && !currentUserPort.isAdmin()) {
            throw new ForbiddenAccessException("No dispone de permisos para añadir fotografías a esta participación");
        }

        Photo photo = Photo.create(
                PhotoId.generate(),
                command.participationId(),
                command.storageReference(),
                command.caption(),
                currentUserId.value()
        );

        Photo saved = photoRepositoryPort.save(photo);
        eventPublisherPort.publish(new PhotoUploadedEvent(saved.getId().value(), saved.getParticipationId().value(), saved.getUploadedByUserId(), saved.getStorageReference()));
        return saved;
    }

    @Override
    public Photo getPhotoById(PhotoId id) {
        return photoRepositoryPort.findById(id)
                .orElseThrow(() -> new PhotoNotFoundException(id));
    }

    @Override
    public List<Photo> getPhotosByParticipationId(ParticipationId participationId) {
        return photoRepositoryPort.findByParticipationId(participationId);
    }
}
