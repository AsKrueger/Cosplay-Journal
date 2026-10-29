package com.cosplayjournal.application.port.in;

import com.cosplayjournal.application.dto.GalleryPhotoView;

import java.util.List;

public interface ManageGalleryUseCase {

    GalleryPhotoView upload(String ownerId, UploadGalleryPhotoCommand command);

    List<GalleryPhotoView> list(String ownerId, String tag, String eventId);

    record UploadGalleryPhotoCommand(String tag, String eventId, String contentType, byte[] content) {
    }
}
