package com.cosplayjournal.domain.model.participation;

import com.cosplayjournal.domain.exception.InvalidParticipationDataException;

import java.time.Instant;
import java.util.Objects;

public class Participant {

    private final String userId;
    private final String name;
    private ParticipantRole role;
    private String assignedCharacter;
    private final Instant joinedAt;

    public Participant(String userId, String name, ParticipantRole role, String assignedCharacter, Instant joinedAt) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new InvalidParticipationDataException("El ID del usuario participante no puede estar vacío");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new InvalidParticipationDataException("El nombre del participante no puede estar vacío");
        }

        this.userId = userId.trim();
        this.name = name.trim();
        this.role = role != null ? role : ParticipantRole.MEMBER;
        this.assignedCharacter = assignedCharacter != null ? assignedCharacter.trim() : "";
        this.joinedAt = joinedAt != null ? joinedAt : Instant.now();
    }

    public static Participant create(String userId, String name, ParticipantRole role) {
        return new Participant(userId, name, role, "", Instant.now());
    }

    public static Participant createLeader(String userId, String name) {
        return new Participant(userId, name, ParticipantRole.LEADER, "", Instant.now());
    }

    public void assignCharacter(String characterName) {
        this.assignedCharacter = characterName != null ? characterName.trim() : "";
    }

    public void changeRole(ParticipantRole newRole) {
        if (newRole == null) {
            throw new InvalidParticipationDataException("El rol del participante no puede ser nulo");
        }
        this.role = newRole;
    }

    public String getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public ParticipantRole getRole() {
        return role;
    }

    public String getAssignedCharacter() {
        return assignedCharacter;
    }

    public Instant getJoinedAt() {
        return joinedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Participant that = (Participant) o;
        return Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }

    @Override
    public String toString() {
        return "Participant{" +
                "userId='" + userId + '\'' +
                ", name='" + name + '\'' +
                ", role=" + role +
                ", character='" + assignedCharacter + '\'' +
                '}';
    }
}
