package com.cosplayjournal.domain.model.gallery;

import com.cosplayjournal.domain.exception.InvalidGalleryDataException;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Foto de la galería personal. Pertenece a un usuario y lleva una etiqueta: evento, cosplay o grupal.
 * El evento es obligatorio solo cuando la etiqueta es EVENTO.
 */
public final class GalleryPhoto {

    private static final int MAX_URL_LENGTH = 500;

    private final String id;
    private final String ownerId;
    private final GalleryTag tag;
    private final String eventId;
    private final String url;
    private final Instant uploadedAt;

    private GalleryPhoto(String id, String ownerId, GalleryTag tag, String eventId, String url, Instant uploadedAt) {
        if (id == null || id.isBlank()) {
            throw new InvalidGalleryDataException("El identificador de la foto no puede estar vacío");
        }
        if (ownerId == null || ownerId.isBlank()) {
            throw new InvalidGalleryDataException("La foto debe tener propietario");
        }
        if (tag == null) {
            throw new InvalidGalleryDataException("Indica si la foto es de evento, cosplay o grupal");
        }
        if (tag == GalleryTag.EVENTO && (eventId == null || eventId.isBlank())) {
            throw new InvalidGalleryDataException("Una foto de evento necesita el evento al que pertenece");
        }
        if (url == null || url.isBlank() || url.length() > MAX_URL_LENGTH) {
            throw new InvalidGalleryDataException("La URL de la foto no es válida");
        }
        this.id = id;
        this.ownerId = ownerId;
        this.tag = tag;
        this.eventId = eventId == null || eventId.isBlank() ? null : eventId.trim();
        this.url = url.trim();
        this.uploadedAt = Objects.requireNonNull(uploadedAt, "uploadedAt");
    }

    /** Crea una foto nueva con un identificador generado. */
    public static GalleryPhoto create(String ownerId, GalleryTag tag, String eventId, String url) {
        return new GalleryPhoto(UUID.randomUUID().toString(), ownerId, tag, eventId, url, Instant.now());
    }

    /** Reconstruye una foto guardada. */
    public static GalleryPhoto restore(String id, String ownerId, GalleryTag tag, String eventId, String url, Instant uploadedAt) {
        return new GalleryPhoto(id, ownerId, tag, eventId, url, uploadedAt);
    }

    public String getId() {
        return id;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public GalleryTag getTag() {
        return tag;
    }

    public String getEventId() {
        return eventId;
    }

    public String getUrl() {
        return url;
    }

    public Instant getUploadedAt() {
        return uploadedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GalleryPhoto other)) return false;
        return id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
