package com.cosplayjournal.application.port.in;

import com.cosplayjournal.domain.model.photo.Photo;

public interface AddPhotoUseCase {
    Photo addPhoto(AddPhotoCommand command);
}
