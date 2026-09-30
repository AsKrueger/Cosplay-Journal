package com.cosplayjournal.application.service;

import com.cosplayjournal.application.port.in.AddPhotoCommand;
import com.cosplayjournal.application.port.in.AddPhotoUseCase;
import com.cosplayjournal.application.port.in.GetParticipationPhotosUseCase;
import com.cosplayjournal.application.port.in.GetPhotoUseCase;
import com.cosplayjournal.application.port.out.DomainEventPublisherPort;
import com.cosplayjournal.application.port.out.ParticipationRepositoryPort;
import com.cosplayjournal.application.port.out.PhotoRepositoryPort;
import com.cosplayjournal.domain.event.PhotoUploadedEvent;
import com.cosplayjournal.domain.exception.ParticipationNotFoundException;
import com.cosplayjournal.domain.exception.PhotoNotFoundException;
import com.cosplayjournal.domain.model.participation.ParticipationId;
import com.cosplayjournal.domain.model.photo.Photo;
import com.cosplayjournal.domain.model.photo.PhotoId;

import java.util.List;

public class PhotoApplicationService implements AddPhotoUseCase, GetPhotoUseCase, GetParticipationPhotosUseCase {

    private final PhotoRepositoryPort photoRepositoryPort;
    private final ParticipationRepositoryPort participationRepositoryPort;
    private final DomainEventPublisherPort eventPublisherPort;

    public PhotoApplicationService(
            PhotoRepositoryPort photoRepositoryPort,
            ParticipationRepositoryPort participationRepositoryPort,
            DomainEventPublisherPort eventPublisherPort
    ) {
        this.photoRepositoryPort = photoRepositoryPort;
        this.participationRepositoryPort = participationRepositoryPort;
        this.eventPublisherPort = eventPublisherPort;
    }

    public PhotoApplicationService(
            PhotoRepositoryPort photoRepositoryPort,
            ParticipationRepositoryPort participationRepositoryPort
    ) {
        this(photoRepositoryPort, participationRepositoryPort, event -> {});
    }

    @Override
    public Photo addPhoto(AddPhotoCommand command) {
        // Verifica existencia de la participación
        participationRepositoryPort.findById(command.participationId())
                .orElseThrow(() -> new ParticipationNotFoundException(command.participationId()));

        Photo photo = Photo.create(
                PhotoId.generate(),
                command.participationId(),
                command.storageReference(),
                command.caption(),
                command.uploadedByUserId()
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
