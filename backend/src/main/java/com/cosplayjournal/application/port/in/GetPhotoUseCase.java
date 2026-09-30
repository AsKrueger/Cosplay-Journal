package com.cosplayjournal.application.port.in;

import com.cosplayjournal.domain.model.photo.Photo;
import com.cosplayjournal.domain.model.photo.PhotoId;

public interface GetPhotoUseCase {
    Photo getPhotoById(PhotoId id);
}
