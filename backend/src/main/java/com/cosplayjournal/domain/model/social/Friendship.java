package com.cosplayjournal.domain.model.social;

import com.cosplayjournal.domain.exception.InvalidFriendshipException;
import com.cosplayjournal.domain.model.user.UserId;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Relación de amistad entre dos usuarios. No existen seguidores: la relación es mutua
 * y requiere que el destinatario acepte la solicitud.
 */
public class Friendship {

    private final String id;
    private final UserId requesterId;
    private final UserId addresseeId;
    private FriendshipStatus status;
    private final Instant createdAt;
    private Instant respondedAt;

    private Friendship(String id, UserId requesterId, UserId addresseeId, FriendshipStatus status,
                       Instant createdAt, Instant respondedAt) {
        this.id = id;
        this.requesterId = requesterId;
        this.addresseeId = addresseeId;
        this.status = status;
        this.createdAt = createdAt;
        this.respondedAt = respondedAt;
    }

    public static Friendship request(UserId requesterId, UserId addresseeId) {
        Objects.requireNonNull(requesterId, "El solicitante es obligatorio");
        Objects.requireNonNull(addresseeId, "El destinatario es obligatorio");
        if (requesterId.equals(addresseeId)) {
            throw new InvalidFriendshipException("No puedes enviarte una solicitud de amistad a ti mismo");
        }
        return new Friendship(UUID.randomUUID().toString(), requesterId, addresseeId,
                FriendshipStatus.PENDING, Instant.now(), null);
    }

    public static Friendship restore(String id, UserId requesterId, UserId addresseeId, FriendshipStatus status,
                                     Instant createdAt, Instant respondedAt) {
        return new Friendship(id, requesterId, addresseeId, status, createdAt, respondedAt);
    }

    /** Solo el destinatario puede aceptar, y solo una vez. */
    public void accept(UserId actor) {
        if (!addresseeId.equals(actor)) {
            throw new InvalidFriendshipException("Solo el destinatario puede aceptar la solicitud");
        }
        if (status != FriendshipStatus.PENDING) {
            throw new InvalidFriendshipException("La solicitud ya fue respondida");
        }
        this.status = FriendshipStatus.ACCEPTED;
        this.respondedAt = Instant.now();
    }

    public boolean involves(UserId userId) {
        return requesterId.equals(userId) || addresseeId.equals(userId);
    }

    /** El otro miembro de la relación, visto desde {@code userId}. */
    public UserId otherParty(UserId userId) {
        if (!involves(userId)) {
            throw new InvalidFriendshipException("El usuario no forma parte de esta relación");
        }
        return requesterId.equals(userId) ? addresseeId : requesterId;
    }

    public boolean isPending() {
        return status == FriendshipStatus.PENDING;
    }

    public boolean isAccepted() {
        return status == FriendshipStatus.ACCEPTED;
    }

    public String getId() { return id; }
    public UserId getRequesterId() { return requesterId; }
    public UserId getAddresseeId() { return addresseeId; }
    public FriendshipStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getRespondedAt() { return respondedAt; }
}
