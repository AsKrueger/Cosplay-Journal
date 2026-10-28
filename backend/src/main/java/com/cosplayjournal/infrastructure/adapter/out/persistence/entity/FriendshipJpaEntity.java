package com.cosplayjournal.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "friendship")
public class FriendshipJpaEntity {

    @Id
    @Column(name = "id", nullable = false, length = 100)
    private String id;

    @Column(name = "requester_id", nullable = false, length = 100)
    private String requesterId;

    @Column(name = "addressee_id", nullable = false, length = 100)
    private String addresseeId;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "responded_at")
    private Instant respondedAt;

    public FriendshipJpaEntity() {
    }

    public FriendshipJpaEntity(String id, String requesterId, String addresseeId, String status,
                               Instant createdAt, Instant respondedAt) {
        this.id = id;
        this.requesterId = requesterId;
        this.addresseeId = addresseeId;
        this.status = status;
        this.createdAt = createdAt;
        this.respondedAt = respondedAt;
    }

    public String getId() { return id; }
    public String getRequesterId() { return requesterId; }
    public String getAddresseeId() { return addresseeId; }
    public String getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getRespondedAt() { return respondedAt; }
}
