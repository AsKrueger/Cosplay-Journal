package com.cosplayjournal.domain.model;

import com.cosplayjournal.domain.exception.InvalidCosplayDataException;
import com.cosplayjournal.domain.exception.InvalidStateTransitionException;
import com.cosplayjournal.domain.model.user.UserId;

import java.time.Instant;
import java.util.Objects;
import java.util.Set;

public class Cosplay {

    private final Long id;
    private final UserId ownerId;
    private final String name;
    private final String description;
    private final String characterName;
    private final String originSeries;
    private CosplayStatus status;
    private final Instant createdAt;
    private Instant updatedAt;

    public Cosplay(Long id, UserId ownerId, String name, String description, String characterName, String originSeries, CosplayStatus status, Instant createdAt, Instant updatedAt) {
        validateName(name);
        if (ownerId == null) {
            throw new InvalidCosplayDataException("El propietario (ownerId) del cosplay es obligatorio");
        }

        this.id = id;
        this.ownerId = ownerId;
        this.name = name.trim();
        this.description = description != null ? description.trim() : "";
        this.characterName = characterName != null ? characterName.trim() : "";
        this.originSeries = originSeries != null ? originSeries.trim() : "";
        this.status = status != null ? status : CosplayStatus.IDEA;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.updatedAt = updatedAt != null ? updatedAt : this.createdAt;
    }

    public Cosplay(Long id, String name, String description, String characterName, String originSeries, CosplayStatus status, Instant createdAt, Instant updatedAt) {
        this(id, UserId.of("system-default"), name, description, characterName, originSeries, status, createdAt, updatedAt);
    }

    public static Cosplay createNew(String name, String description, String characterName, String originSeries, UserId ownerId) {
        return new Cosplay(null, ownerId, name, description, characterName, originSeries, CosplayStatus.IDEA, Instant.now(), Instant.now());
    }

    public static Cosplay createNew(String name, String description, String characterName, String originSeries) {
        return createNew(name, description, characterName, originSeries, UserId.of("system-default"));
    }

    public Cosplay withId(Long newId) {
        return new Cosplay(newId, this.ownerId, this.name, this.description, this.characterName, this.originSeries, this.status, this.createdAt, Instant.now());
    }

    public boolean isOwnedBy(UserId userId) {
        return userId != null && this.ownerId.equals(userId);
    }

    public void changeStatus(CosplayStatus newStatus) {
        if (newStatus == null) {
            throw new InvalidCosplayDataException("El estado del cosplay no puede ser nulo");
        }
        if (this.status == newStatus) {
            return;
        }

        validateTransition(this.status, newStatus);
        this.status = newStatus;
        this.updatedAt = Instant.now();
    }

    public void updateStatus(CosplayStatus newStatus) {
        changeStatus(newStatus);
    }

    private void validateTransition(CosplayStatus current, CosplayStatus target) {
        Set<CosplayStatus> allowedTargets = switch (current) {
            case IDEA -> Set.of(CosplayStatus.IN_PLANNING, CosplayStatus.IN_PROGRESS, CosplayStatus.ABANDONED);
            case IN_PLANNING -> Set.of(CosplayStatus.IN_PROGRESS, CosplayStatus.ABANDONED, CosplayStatus.IDEA);
            case IN_PROGRESS -> Set.of(CosplayStatus.COMPLETED, CosplayStatus.ABANDONED, CosplayStatus.IN_PLANNING);
            case COMPLETED -> Set.of(CosplayStatus.ARCHIVED, CosplayStatus.IN_PROGRESS);
            case ABANDONED -> Set.of(CosplayStatus.IDEA, CosplayStatus.IN_PLANNING);
            case ARCHIVED -> Set.of(CosplayStatus.COMPLETED, CosplayStatus.IDEA);
        };

        if (!allowedTargets.contains(target)) {
            throw new InvalidStateTransitionException(current.name(), target.name());
        }
    }

    private static void validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new InvalidCosplayDataException("El nombre del cosplay no puede estar vacío");
        }
        if (name.trim().length() < 2 || name.trim().length() > 100) {
            throw new InvalidCosplayDataException("El nombre del cosplay debe tener entre 2 y 100 caracteres");
        }
    }

    public Long getId() {
        return id;
    }

    public UserId getOwnerId() {
        return ownerId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getCharacterName() {
        return characterName;
    }

    public String getOriginSeries() {
        return originSeries;
    }

    public CosplayStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

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
        return "Cosplay{" +
                "id=" + id +
                ", ownerId=" + ownerId +
                ", name='" + name + '\'' +
                ", status=" + status +
                '}';
    }
}
