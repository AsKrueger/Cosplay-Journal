package com.cosplayjournal.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "user_event_status")
public class UserEventStatusJpaEntity {

    /** Clave compuesta en una sola columna: userId + "|" + eventId. */
    @Id
    @Column(name = "id", nullable = false, length = 250)
    private String id;

    @Column(name = "user_id", nullable = false, length = 100)
    private String userId;

    @Column(name = "event_id", nullable = false, length = 150)
    private String eventId;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public UserEventStatusJpaEntity() {
    }

    public UserEventStatusJpaEntity(String id, String userId, String eventId, String status, Instant updatedAt) {
        this.id = id;
        this.userId = userId;
        this.eventId = eventId;
        this.status = status;
        this.updatedAt = updatedAt;
    }

    public String getId() { return id; }
    public String getUserId() { return userId; }
    public String getEventId() { return eventId; }
    public String getStatus() { return status; }
    public Instant getUpdatedAt() { return updatedAt; }
}
