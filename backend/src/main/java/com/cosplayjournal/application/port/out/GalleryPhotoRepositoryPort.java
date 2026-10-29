package com.cosplayjournal.application.port.out;

import com.cosplayjournal.domain.model.gallery.GalleryPhoto;
import com.cosplayjournal.domain.model.gallery.GalleryTag;

import java.util.List;

public interface GalleryPhotoRepositoryPort {

    GalleryPhoto save(GalleryPhoto photo);

    /** Fotos de un propietario, filtradas por etiqueta y evento si se indican. Las más recientes primero. */
    List<GalleryPhoto> findByOwner(String ownerId, GalleryTag tag, String eventId);
}
