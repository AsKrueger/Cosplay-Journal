package com.cosplayjournal.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "conversation")
public class ConversationJpaEntity {

    @Id
    @Column(name = "id", nullable = false, length = 100)
    private String id;

    @Column(name = "type", nullable = false, length = 20)
    private String type;

    @Column(name = "name", length = 60)
    private String name;

    @Column(name = "created_by", nullable = false, length = 100)
    private String createdBy;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "conversation_member", joinColumns = @JoinColumn(name = "conversation_id"))
    @Column(name = "user_id", nullable = false, length = 100)
    private Set<String> members = new HashSet<>();

    public ConversationJpaEntity() {
    }

    public ConversationJpaEntity(String id, String type, String name, String createdBy, Instant createdAt, Set<String> members) {
        this.id = id;
        this.type = type;
        this.name = name;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.members = new HashSet<>(members);
    }

    public String getId() { return id; }
    public String getType() { return type; }
    public String getName() { return name; }
    public String getCreatedBy() { return createdBy; }
    public Instant getCreatedAt() { return createdAt; }
    public Set<String> getMembers() { return members; }
}
