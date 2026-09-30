package com.cosplayjournal.application.port.in;

import com.cosplayjournal.domain.model.participation.ParticipationId;
import com.cosplayjournal.domain.model.photo.Photo;

import java.util.List;

public interface GetParticipationPhotosUseCase {
    List<Photo> getPhotosByParticipationId(ParticipationId participationId);
}
