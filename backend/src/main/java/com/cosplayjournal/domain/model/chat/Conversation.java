package com.cosplayjournal.domain.model.chat;

import com.cosplayjournal.domain.exception.InvalidChatDataException;
import com.cosplayjournal.domain.model.user.UserId;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Conversación entre amigos (PRIVATE, dos miembros) o grupo (GROUP, con nombre y varios miembros).
 */
public class Conversation {

    public static final int MAX_NAME_LENGTH = 60;
    public static final int MAX_GROUP_MEMBERS = 30;

    private final String id;
    private final ConversationType type;
    private final String name;
    private final UserId createdBy;
    private final Instant createdAt;
    private final Set<UserId> members;

    private Conversation(String id, ConversationType type, String name, UserId createdBy,
                         Instant createdAt, Set<UserId> members) {
        this.id = id;
        this.type = type;
        this.name = name;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.members = members;
    }

    public static Conversation privateBetween(UserId first, UserId second) {
        Objects.requireNonNull(first, "El usuario es obligatorio");
        Objects.requireNonNull(second, "El usuario es obligatorio");
        if (first.equals(second)) {
            throw new InvalidChatDataException("No puedes abrir un chat contigo mismo");
        }
        Set<UserId> members = new LinkedHashSet<>();
        members.add(first);
        members.add(second);
        return new Conversation(UUID.randomUUID().toString(), ConversationType.PRIVATE, null, first, Instant.now(), members);
    }

    public static Conversation group(UserId creator, String name, Set<UserId> others) {
        Objects.requireNonNull(creator, "El creador es obligatorio");
        if (name == null || name.isBlank() || name.trim().length() > MAX_NAME_LENGTH) {
            throw new InvalidChatDataException("El nombre del grupo debe tener entre 1 y " + MAX_NAME_LENGTH + " caracteres");
        }
        Set<UserId> members = new LinkedHashSet<>();
        members.add(creator);
        for (UserId other : others) {
            if (!other.equals(creator)) {
                members.add(other);
            }
        }
        if (members.size() < 2) {
            throw new InvalidChatDataException("Un grupo necesita al menos otro miembro además de ti");
        }
        if (members.size() > MAX_GROUP_MEMBERS) {
            throw new InvalidChatDataException("Un grupo admite como máximo " + MAX_GROUP_MEMBERS + " miembros");
        }
        return new Conversation(UUID.randomUUID().toString(), ConversationType.GROUP, name.trim(), creator, Instant.now(), members);
    }

    public static Conversation restore(String id, ConversationType type, String name, UserId createdBy,
                                       Instant createdAt, Set<UserId> members) {
        return new Conversation(id, type, name, createdBy, createdAt, new LinkedHashSet<>(members));
    }

    public boolean hasMember(UserId userId) {
        return members.contains(userId);
    }

    public String getId() { return id; }
    public ConversationType getType() { return type; }
    public String getName() { return name; }
    public UserId getCreatedBy() { return createdBy; }
    public Instant getCreatedAt() { return createdAt; }
    public Set<UserId> getMembers() { return members; }
}
