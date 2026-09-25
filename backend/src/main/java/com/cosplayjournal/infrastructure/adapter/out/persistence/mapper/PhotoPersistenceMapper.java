package com.cosplayjournal.infrastructure.adapter.out.persistence.mapper;

import com.cosplayjournal.domain.model.participation.ParticipationId;
import com.cosplayjournal.domain.model.photo.Photo;
import com.cosplayjournal.domain.model.photo.PhotoId;
import com.cosplayjournal.infrastructure.adapter.out.persistence.entity.PhotoJpaEntity;

public class PhotoPersistenceMapper {

    public static PhotoJpaEntity toJpaEntity(Photo photo) {
        if (photo == null) return null;
        return new PhotoJpaEntity(
                photo.getId().value(),
                photo.getParticipationId().value(),
                photo.getStorageReference(),
                photo.getCaption(),
                photo.getUploadedByUserId(),
                photo.getUploadedAt()
        );
    }

    public static Photo toDomain(PhotoJpaEntity entity) {
        if (entity == null) return null;
        return new Photo(
                PhotoId.of(entity.getId()),
                ParticipationId.of(entity.getParticipationId()),
                entity.getStorageReference(),
                entity.getCaption(),
                entity.getUploadedByUserId(),
                entity.getUploadedAt()
        );
    }
}
