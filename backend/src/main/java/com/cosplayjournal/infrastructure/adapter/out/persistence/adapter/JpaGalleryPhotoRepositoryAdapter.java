package com.cosplayjournal.infrastructure.adapter.out.persistence.adapter;

import com.cosplayjournal.application.port.out.GalleryPhotoRepositoryPort;
import com.cosplayjournal.domain.model.gallery.GalleryPhoto;
import com.cosplayjournal.domain.model.gallery.GalleryTag;
import com.cosplayjournal.infrastructure.adapter.out.persistence.entity.GalleryPhotoJpaEntity;
import com.cosplayjournal.infrastructure.adapter.out.persistence.repository.SpringDataGalleryPhotoRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class JpaGalleryPhotoRepositoryAdapter implements GalleryPhotoRepositoryPort {

    private final SpringDataGalleryPhotoRepository repository;

    public JpaGalleryPhotoRepositoryAdapter(SpringDataGalleryPhotoRepository repository) {
        this.repository = repository;
    }

    @Override
    public GalleryPhoto save(GalleryPhoto photo) {
        repository.save(toEntity(photo));
        return photo;
    }

    @Override
    public List<GalleryPhoto> findByOwner(String ownerId, GalleryTag tag, String eventId) {
        return repository.findFiltered(ownerId, tag == null ? null : tag.name(), eventId).stream()
                .map(this::toDomain)
                .toList();
    }

    private GalleryPhotoJpaEntity toEntity(GalleryPhoto photo) {
        return new GalleryPhotoJpaEntity(
                photo.getId(),
                photo.getOwnerId(),
                photo.getTag().name(),
                photo.getEventId(),
                photo.getUrl(),
                photo.getUploadedAt()
        );
    }

    private GalleryPhoto toDomain(GalleryPhotoJpaEntity entity) {
        return GalleryPhoto.restore(
                entity.getId(),
                entity.getOwnerId(),
                GalleryTag.valueOf(entity.getTag()),
                entity.getEventId(),
                entity.getUrl(),
                entity.getUploadedAt()
        );
    }
}
