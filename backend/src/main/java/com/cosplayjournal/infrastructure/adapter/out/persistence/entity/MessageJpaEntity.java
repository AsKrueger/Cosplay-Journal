package com.cosplayjournal.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "message")
public class MessageJpaEntity {

    @Id
    @Column(name = "id", nullable = false, length = 100)
    private String id;

    @Column(name = "conversation_id", nullable = false, length = 100)
    private String conversationId;

    @Column(name = "sender_id", nullable = false, length = 100)
    private String senderId;

    @Column(name = "kind", nullable = false, length = 10)
    private String kind;

    @Column(name = "body", nullable = false, length = 2000)
    private String body;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public MessageJpaEntity() {
    }

    public MessageJpaEntity(String id, String conversationId, String senderId, String kind, String body, Instant createdAt) {
        this.id = id;
        this.conversationId = conversationId;
        this.senderId = senderId;
        this.kind = kind;
        this.body = body;
        this.createdAt = createdAt;
    }

    public String getId() { return id; }
    public String getConversationId() { return conversationId; }
    public String getSenderId() { return senderId; }
    public String getKind() { return kind; }
    public String getBody() { return body; }
    public Instant getCreatedAt() { return createdAt; }
}
