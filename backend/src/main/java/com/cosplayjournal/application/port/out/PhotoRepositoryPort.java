package com.cosplayjournal.application.port.out;

import com.cosplayjournal.domain.model.participation.ParticipationId;
import com.cosplayjournal.domain.model.photo.Photo;
import com.cosplayjournal.domain.model.photo.PhotoId;

import java.util.List;
import java.util.Optional;

public interface PhotoRepositoryPort {
    Photo save(Photo photo);
    Optional<Photo> findById(PhotoId id);
    List<Photo> findByParticipationId(ParticipationId participationId);
    void deleteById(PhotoId id);
}
