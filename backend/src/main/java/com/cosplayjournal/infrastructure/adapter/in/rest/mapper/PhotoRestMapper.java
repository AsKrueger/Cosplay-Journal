package com.cosplayjournal.infrastructure.adapter.in.rest.mapper;

import com.cosplayjournal.application.port.in.AddPhotoCommand;
import com.cosplayjournal.domain.model.participation.ParticipationId;
import com.cosplayjournal.domain.model.photo.Photo;
import com.cosplayjournal.infrastructure.adapter.in.rest.dto.AddPhotoRequest;
import com.cosplayjournal.infrastructure.adapter.in.rest.dto.PhotoResponse;

public class PhotoRestMapper {

    public static AddPhotoCommand toCommand(AddPhotoRequest request) {
        return new AddPhotoCommand(
                ParticipationId.of(request.participationId()),
                request.storageReference(),
                request.caption(),
                request.uploadedByUserId()
        );
    }

    public static PhotoResponse toResponse(Photo photo) {
        if (photo == null) return null;
        return new PhotoResponse(
                photo.getId().value(),
                photo.getParticipationId().value(),
                photo.getStorageReference(),
                photo.getCaption(),
                photo.getUploadedByUserId(),
                photo.getUploadedAt()
        );
    }
}
