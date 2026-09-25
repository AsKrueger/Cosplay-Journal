package com.cosplayjournal.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class ParticipantJpaEntityId implements Serializable {

    private String userId;
    private String participationId;

    public ParticipantJpaEntityId() {
    }

    public ParticipantJpaEntityId(String userId, String participationId) {
        this.userId = userId;
        this.participationId = participationId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getParticipationId() {
        return participationId;
    }

    public void setParticipationId(String participationId) {
        this.participationId = participationId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ParticipantJpaEntityId that = (ParticipantJpaEntityId) o;
        return Objects.equals(userId, that.userId) && Objects.equals(participationId, that.participationId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, participationId);
    }
}
