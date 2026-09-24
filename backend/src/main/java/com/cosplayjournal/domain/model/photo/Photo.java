package com.cosplayjournal.domain.model.photo;

import com.cosplayjournal.domain.exception.InvalidParticipationDataException;
import com.cosplayjournal.domain.model.participation.ParticipationId;

import java.time.Instant;
import java.util.Objects;

public class Photo {

    private final PhotoId id;
    private final ParticipationId participationId;
    private final String storageReference;
    private String caption;
    private final String uploadedByUserId;
    private final Instant uploadedAt;

    public Photo(
            PhotoId id,
            ParticipationId participationId,
            String storageReference,
            String caption,
            String uploadedByUserId,
            Instant uploadedAt
    ) {
        if (id == null) {
            throw new InvalidParticipationDataException("El ID de la fotografía no puede ser nulo");
        }
        if (participationId == null) {
            throw new InvalidParticipationDataException("La participación asociada a la fotografía no puede ser nula");
        }
        if (storageReference == null || storageReference.trim().isEmpty()) {
            throw new InvalidParticipationDataException("La referencia de almacenamiento de la fotografía no puede estar vacía");
        }

        this.id = id;
        this.participationId = participationId;
        this.storageReference = storageReference.trim();
        this.caption = caption != null ? caption.trim() : "";
        this.uploadedByUserId = uploadedByUserId != null ? uploadedByUserId.trim() : "";
        this.uploadedAt = uploadedAt != null ? uploadedAt : Instant.now();
    }

    public static Photo create(
            PhotoId id,
            ParticipationId participationId,
            String storageReference,
            String caption,
            String uploadedByUserId
    ) {
        return new Photo(id, participationId, storageReference, caption, uploadedByUserId, Instant.now());
    }

    public void updateCaption(String newCaption) {
        this.caption = newCaption != null ? newCaption.trim() : "";
    }

    public PhotoId getId() {
        return id;
    }

    public ParticipationId getParticipationId() {
        return participationId;
    }

    public String getStorageReference() {
        return storageReference;
    }

    public String getCaption() {
        return caption;
    }

    public String getUploadedByUserId() {
        return uploadedByUserId;
    }

    public Instant getUploadedAt() {
        return uploadedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Photo photo = (Photo) o;
        return Objects.equals(id, photo.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Photo{" +
                "id=" + id +
                ", participationId=" + participationId +
                ", ref='" + storageReference + '\'' +
                '}';
    }
}
