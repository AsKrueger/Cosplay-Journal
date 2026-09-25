package com.cosplayjournal.infrastructure.adapter.out.persistence.adapter;

import com.cosplayjournal.application.port.out.PhotoRepositoryPort;
import com.cosplayjournal.domain.model.participation.ParticipationId;
import com.cosplayjournal.domain.model.photo.Photo;
import com.cosplayjournal.domain.model.photo.PhotoId;
import com.cosplayjournal.infrastructure.adapter.out.persistence.entity.PhotoJpaEntity;
import com.cosplayjournal.infrastructure.adapter.out.persistence.mapper.PhotoPersistenceMapper;
import com.cosplayjournal.infrastructure.adapter.out.persistence.repository.SpringDataPhotoRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public class JpaPhotoRepositoryAdapter implements PhotoRepositoryPort {

    private final SpringDataPhotoRepository springDataPhotoRepository;

    public JpaPhotoRepositoryAdapter(SpringDataPhotoRepository springDataPhotoRepository) {
        this.springDataPhotoRepository = springDataPhotoRepository;
    }

    @Override
    @Transactional
    public Photo save(Photo photo) {
        PhotoJpaEntity entity = PhotoPersistenceMapper.toJpaEntity(photo);
        PhotoJpaEntity saved = springDataPhotoRepository.save(entity);
        return PhotoPersistenceMapper.toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Photo> findById(PhotoId id) {
        if (id == null) return Optional.empty();
        return springDataPhotoRepository.findById(id.value())
                .map(PhotoPersistenceMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Photo> findByParticipationId(ParticipationId participationId) {
        if (participationId == null) return List.of();
        return springDataPhotoRepository.findByParticipationId(participationId.value()).stream()
                .map(PhotoPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    @Transactional
    public void deleteById(PhotoId id) {
        if (id != null) {
            springDataPhotoRepository.deleteById(id.value());
        }
    }
}
