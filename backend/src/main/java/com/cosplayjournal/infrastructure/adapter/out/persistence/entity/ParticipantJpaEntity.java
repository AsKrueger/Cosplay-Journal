package com.cosplayjournal.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "participant")
public class ParticipantJpaEntity {

    @EmbeddedId
    private ParticipantJpaEntityId id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "role", nullable = false, length = 30)
    private String role;

    @Column(name = "assigned_character", length = 100)
    private String assignedCharacter;

    @Column(name = "joined_at", nullable = false)
    private Instant joinedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("participationId")
    @JoinColumn(name = "participation_id", nullable = false)
    private ParticipationJpaEntity participation;

    public ParticipantJpaEntity() {
    }

    public ParticipantJpaEntity(ParticipantJpaEntityId id, String name, String role, String assignedCharacter, Instant joinedAt, ParticipationJpaEntity participation) {
        this.id = id;
        this.name = name;
        this.role = role;
        this.assignedCharacter = assignedCharacter;
        this.joinedAt = joinedAt;
        this.participation = participation;
    }

    public ParticipantJpaEntityId getId() {
        return id;
    }

    public void setId(ParticipantJpaEntityId id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getAssignedCharacter() {
        return assignedCharacter;
    }

    public void setAssignedCharacter(String assignedCharacter) {
        this.assignedCharacter = assignedCharacter;
    }

    public Instant getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(Instant joinedAt) {
        this.joinedAt = joinedAt;
    }

    public ParticipationJpaEntity getParticipation() {
        return participation;
    }

    public void setParticipation(ParticipationJpaEntity participation) {
        this.participation = participation;
    }
}
