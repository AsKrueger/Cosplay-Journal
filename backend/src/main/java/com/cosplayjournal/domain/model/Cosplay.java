package com.cosplayjournal.domain.model;

import com.cosplayjournal.domain.exception.InvalidCosplayDataException;
import com.cosplayjournal.domain.model.user.UserId;

import java.time.Instant;
import java.util.Objects;

/**
 * Un cosplay es solo un nombre y una foto. Se usa en la galería y para participar en grupales.
 */
public class Cosplay {

    private final Long id;
    private final UserId ownerId;
    private final String name;
    private String photoUrl;
    private final Instant createdAt;
    private Instant updatedAt;

    public Cosplay(Long id, UserId ownerId, String name, String photoUrl, Instant createdAt, Instant updatedAt) {
        validateName(name);
        validatePhotoUrl(photoUrl);
        this.id = id;
        this.ownerId = ownerId;
        this.name = name.trim();
        this.photoUrl = blankToNull(photoUrl);
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.updatedAt = updatedAt != null ? updatedAt : this.createdAt;
    }

    public static Cosplay createNew(String name, String photoUrl, UserId ownerId) {
        Instant now = Instant.now();
        return new Cosplay(null, ownerId, name, photoUrl, now, now);
    }

    public static Cosplay createNew(String name, String photoUrl) {
        return createNew(name, photoUrl, UserId.of("system-default"));
    }

    public Cosplay withId(Long newId) {
        return new Cosplay(newId, ownerId, name, photoUrl, createdAt, updatedAt);
    }

    public boolean isOwnedBy(UserId userId) {
        return ownerId != null && ownerId.equals(userId);
    }

    /** Cambia la foto. Una URL vacía la elimina. */
    public void changePhoto(String newPhotoUrl) {
        validatePhotoUrl(newPhotoUrl);
        this.photoUrl = blankToNull(newPhotoUrl);
        this.updatedAt = Instant.now();
    }

    private static void validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new InvalidCosplayDataException("El nombre del cosplay es obligatorio");
        }
        if (name.trim().length() > 100) {
            throw new InvalidCosplayDataException("El nombre del cosplay no puede superar 100 caracteres");
        }
    }

    private static void validatePhotoUrl(String photoUrl) {
        if (photoUrl == null || photoUrl.isBlank()) return;
        String trimmed = photoUrl.trim();
        if (trimmed.length() > 500) {
            throw new InvalidCosplayDataException("La URL de la foto no puede superar 500 caracteres");
        }
        if (!trimmed.startsWith("https://") && !trimmed.startsWith("http://")) {
            throw new InvalidCosplayDataException("La foto debe ser una URL http o https");
        }
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    public Long getId() { return id; }
    public UserId getOwnerId() { return ownerId; }
    public String getName() { return name; }
    public String getPhotoUrl() { return photoUrl; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cosplay cosplay = (Cosplay) o;
        return Objects.equals(id, cosplay.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Cosplay{id=" + id + ", name='" + name + "'}";
    }
}
