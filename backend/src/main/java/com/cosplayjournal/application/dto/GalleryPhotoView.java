package com.cosplayjournal.application.dto;

import com.cosplayjournal.domain.model.gallery.GalleryPhoto;

import java.time.Instant;

public record GalleryPhotoView(String id, String tag, String eventId, String url, Instant uploadedAt) {

    public static GalleryPhotoView from(GalleryPhoto photo) {
        return new GalleryPhotoView(
                photo.getId(),
                photo.getTag().name(),
                photo.getEventId(),
                photo.getUrl(),
                photo.getUploadedAt()
        );
    }
}
