package com.cosplayjournal.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "cosplay")
public class CosplayJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "owner_id", nullable = false, length = 100)
    private String ownerId;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "photo_url", length = 500)
    private String photoUrl;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public CosplayJpaEntity() {
    }

    public CosplayJpaEntity(Long id, String ownerId, String name, String photoUrl, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.ownerId = ownerId;
        this.name = name;
        this.photoUrl = photoUrl;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getOwnerId() { return ownerId; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
