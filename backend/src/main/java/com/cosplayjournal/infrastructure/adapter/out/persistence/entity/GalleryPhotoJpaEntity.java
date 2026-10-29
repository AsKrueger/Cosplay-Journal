package com.cosplayjournal.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "gallery_photo")
public class GalleryPhotoJpaEntity {

    @Id
    @Column(name = "id", nullable = false, length = 100)
    private String id;

    @Column(name = "owner_id", nullable = false, length = 100)
    private String ownerId;

    @Column(name = "tag", nullable = false, length = 20)
    private String tag;

    @Column(name = "event_id", length = 100)
    private String eventId;

    @Column(name = "url", nullable = false, length = 500)
    private String url;

    @Column(name = "uploaded_at", nullable = false)
    private Instant uploadedAt;

    protected GalleryPhotoJpaEntity() {
    }

    public GalleryPhotoJpaEntity(String id, String ownerId, String tag, String eventId, String url, Instant uploadedAt) {
        this.id = id;
        this.ownerId = ownerId;
        this.tag = tag;
        this.eventId = eventId;
        this.url = url;
        this.uploadedAt = uploadedAt;
    }

    public String getId() {
        return id;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public String getTag() {
        return tag;
    }

    public String getEventId() {
        return eventId;
    }

    public String getUrl() {
        return url;
    }

    public Instant getUploadedAt() {
        return uploadedAt;
    }
}
