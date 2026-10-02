package com.cosplayjournal.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "participation")
public class ParticipationJpaEntity {

    @Id
    @Column(name = "id", nullable = false, length = 100)
    private String id;

    @Column(name = "creator_id", nullable = false, length = 100)
    private String creatorId;

    @Column(name = "event_id", nullable = false, length = 100)
    private String eventId;

    @Column(name = "cosplay_id", nullable = false)
    private Long cosplayId;

    @Column(name = "type", nullable = false, length = 30)
    private String type;

    @Column(name = "status", nullable = false, length = 30)
    private String status;

    @Column(name = "group_name", length = 150)
    private String groupName;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "participation", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<ParticipantJpaEntity> participants = new ArrayList<>();

    public ParticipationJpaEntity() {
    }

    public ParticipationJpaEntity(String id, String creatorId, String eventId, Long cosplayId, String type, String status, String groupName, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.creatorId = creatorId;
        this.eventId = eventId;
        this.cosplayId = cosplayId;
        this.type = type;
        this.status = status;
        this.groupName = groupName;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public void addParticipant(ParticipantJpaEntity participant) {
        participants.add(participant);
        participant.setParticipation(this);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(String creatorId) {
        this.creatorId = creatorId;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public Long getCosplayId() {
        return cosplayId;
    }

    public void setCosplayId(Long cosplayId) {
        this.cosplayId = cosplayId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<ParticipantJpaEntity> getParticipants() {
        return participants;
    }

    public void setParticipants(List<ParticipantJpaEntity> participants) {
        this.participants = participants;
    }
}
