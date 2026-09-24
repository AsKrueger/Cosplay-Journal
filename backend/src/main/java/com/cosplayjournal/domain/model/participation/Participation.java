package com.cosplayjournal.domain.model.participation;

import com.cosplayjournal.domain.exception.InvalidParticipationDataException;
import com.cosplayjournal.domain.model.event.EventId;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Participation {

    private final ParticipationId id;
    private final EventId eventId;
    private final Long cosplayId;
    private final ParticipationType type;
    private ParticipationStatus status;
    private String groupName;
    private final List<Participant> participants = new ArrayList<>();
    private final Instant createdAt;
    private Instant updatedAt;

    public Participation(
            ParticipationId id,
            EventId eventId,
            Long cosplayId,
            ParticipationType type,
            ParticipationStatus status,
            String groupName,
            List<Participant> initialParticipants,
            Instant createdAt,
            Instant updatedAt
    ) {
        if (id == null) {
            throw new InvalidParticipationDataException("El ID de la participación no puede ser nulo");
        }
        if (eventId == null) {
            throw new InvalidParticipationDataException("El evento asociado a la participación no puede ser nulo");
        }
        if (cosplayId == null) {
            throw new InvalidParticipationDataException("El cosplay asociado a la participación no puede ser nulo");
        }
        if (type == null) {
            throw new InvalidParticipationDataException("El tipo de participación no puede ser nulo");
        }

        this.id = id;
        this.eventId = eventId;
        this.cosplayId = cosplayId;
        this.type = type;
        this.status = status != null ? status : ParticipationStatus.PLANNED;
        this.groupName = groupName != null ? groupName.trim() : "";
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.updatedAt = updatedAt != null ? updatedAt : this.createdAt;

        if (initialParticipants != null) {
            for (Participant p : initialParticipants) {
                addParticipant(p);
            }
        }
    }

    public static Participation create(
            ParticipationId id,
            EventId eventId,
            Long cosplayId,
            ParticipationType type,
            Participant leader
    ) {
        Participation participation = new Participation(
                id, eventId, cosplayId, type, ParticipationStatus.PLANNED, "", null, Instant.now(), Instant.now()
        );
        if (leader != null) {
            participation.addParticipant(leader);
        }
        return participation;
    }

    public void addParticipant(Participant participant) {
        if (participant == null) {
            throw new InvalidParticipationDataException("El participante a añadir no puede ser nulo");
        }

        boolean alreadyExists = participants.stream()
                .anyMatch(p -> p.getUserId().equalsIgnoreCase(participant.getUserId()));

        if (alreadyExists) {
            throw new InvalidParticipationDataException("El usuario '" + participant.getUserId() + "' ya forma parte de esta participación");
        }

        if (type == ParticipationType.INDIVIDUAL && participants.size() >= 1) {
            throw new InvalidParticipationDataException("Una participación individual no puede tener más de 1 participante");
        }
        if (type == ParticipationType.DUO && participants.size() >= 2) {
            throw new InvalidParticipationDataException("Una participación en dúo no puede tener más de 2 participantes");
        }

        this.participants.add(participant);
        this.updatedAt = Instant.now();
    }

    public void removeParticipant(String userId) {
        if (userId == null) return;
        boolean removed = participants.removeIf(p -> p.getUserId().equalsIgnoreCase(userId.trim()));
        if (removed) {
            this.updatedAt = Instant.now();
        }
    }

    public void assignCharacterToParticipant(String userId, String characterName) {
        if (userId == null) return;
        Participant participant = participants.stream()
                .filter(p -> p.getUserId().equalsIgnoreCase(userId.trim()))
                .findFirst()
                .orElseThrow(() -> new InvalidParticipationDataException("El participante con ID '" + userId + "' no se encuentra en esta participación"));

        participant.assignCharacter(characterName);
        this.updatedAt = Instant.now();
    }

    public void changeStatus(ParticipationStatus newStatus) {
        if (newStatus == null) {
            throw new InvalidParticipationDataException("El estado de la participación no puede ser nulo");
        }

        if (newStatus == ParticipationStatus.CONFIRMED || newStatus == ParticipationStatus.FINISHED) {
            validateParticipantCountForStatus();
        }

        this.status = newStatus;
        this.updatedAt = Instant.now();
    }

    private void validateParticipantCountForStatus() {
        int count = participants.size();
        if (type == ParticipationType.INDIVIDUAL && count != 1) {
            throw new InvalidParticipationDataException("Una participación individual confirmada debe tener exactamente 1 participante (actual: " + count + ")");
        }
        if (type == ParticipationType.DUO && count != 2) {
            throw new InvalidParticipationDataException("Una participación en dúo confirmada debe tener exactamente 2 participantes (actual: " + count + ")");
        }
        if (type == ParticipationType.GROUP && count < 3) {
            throw new InvalidParticipationDataException("Una participación grupal confirmada debe tener al menos 3 participantes (actual: " + count + ")");
        }
    }

    public ParticipationId getId() {
        return id;
    }

    public EventId getEventId() {
        return eventId;
    }

    public Long getCosplayId() {
        return cosplayId;
    }

    public ParticipationType getType() {
        return type;
    }

    public ParticipationStatus getStatus() {
        return status;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName != null ? groupName.trim() : "";
        this.updatedAt = Instant.now();
    }

    public List<Participant> getParticipants() {
        return Collections.unmodifiableList(participants);
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
        Participation that = (Participation) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Participation{" +
                "id=" + id +
                ", eventId=" + eventId +
                ", type=" + type +
                ", participants=" + participants.size() +
                ", status=" + status +
                '}';
    }
}
