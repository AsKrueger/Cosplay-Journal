package com.cosplayjournal.application.service;

import com.cosplayjournal.application.dto.FriendRequestSummary;
import com.cosplayjournal.application.dto.PublicUserProfile;
import com.cosplayjournal.application.port.in.ManageFriendshipsUseCase;
import com.cosplayjournal.application.port.out.FriendshipRepositoryPort;
import com.cosplayjournal.application.port.out.UserRepositoryPort;
import com.cosplayjournal.domain.exception.FriendshipNotFoundException;
import com.cosplayjournal.domain.exception.InvalidFriendshipException;
import com.cosplayjournal.domain.exception.UserNotFoundException;
import com.cosplayjournal.domain.model.social.Friendship;
import com.cosplayjournal.domain.model.user.User;
import com.cosplayjournal.domain.model.user.UserId;

import java.util.List;

public class FriendshipApplicationService implements ManageFriendshipsUseCase {

    private final FriendshipRepositoryPort friendshipRepositoryPort;
    private final UserRepositoryPort userRepositoryPort;

    public FriendshipApplicationService(FriendshipRepositoryPort friendshipRepositoryPort,
                                        UserRepositoryPort userRepositoryPort) {
        this.friendshipRepositoryPort = friendshipRepositoryPort;
        this.userRepositoryPort = userRepositoryPort;
    }

    @Override
    public Friendship sendRequest(UserId actor, String nickname) {
        User target = findUserByNickname(nickname);
        Friendship friendship = Friendship.request(actor, target.getId());
        if (friendshipRepositoryPort.findBetween(actor, target.getId()).isPresent()) {
            throw new InvalidFriendshipException("Ya existe una solicitud o amistad con este usuario");
        }
        return friendshipRepositoryPort.save(friendship);
    }

    @Override
    public void accept(UserId actor, String friendshipId) {
        Friendship friendship = findVisibleTo(actor, friendshipId);
        friendship.accept(actor);
        friendshipRepositoryPort.save(friendship);
    }

    @Override
    public void reject(UserId actor, String friendshipId) {
        Friendship friendship = findVisibleTo(actor, friendshipId);
        if (!friendship.getAddresseeId().equals(actor) || !friendship.isPending()) {
            throw new InvalidFriendshipException("Solo el destinatario puede rechazar una solicitud pendiente");
        }
        friendshipRepositoryPort.delete(friendship);
    }

    @Override
    public void removeFriend(UserId actor, String nickname) {
        User other = findUserByNickname(nickname);
        Friendship friendship = friendshipRepositoryPort.findBetween(actor, other.getId())
                .orElseThrow(() -> new FriendshipNotFoundException("No tenéis ninguna amistad ni solicitud"));
        if (friendship.isPending() && friendship.getAddresseeId().equals(actor)) {
            throw new InvalidFriendshipException("Para una solicitud recibida usa rechazar");
        }
        friendshipRepositoryPort.delete(friendship);
    }

    @Override
    public List<PublicUserProfile> listFriends(UserId actor) {
        return friendshipRepositoryPort.findAllInvolving(actor).stream()
                .filter(Friendship::isAccepted)
                .map(f -> profileOf(f.otherParty(actor)))
                .flatMap(java.util.Optional::stream)
                .toList();
    }

    @Override
    public List<FriendRequestSummary> listIncomingRequests(UserId actor) {
        return friendshipRepositoryPort.findAllInvolving(actor).stream()
                .filter(f -> f.isPending() && f.getAddresseeId().equals(actor))
                .map(f -> profileOf(f.getRequesterId()).map(p -> new FriendRequestSummary(f.getId(), p)))
                .flatMap(java.util.Optional::stream)
                .toList();
    }

    private User findUserByNickname(String nickname) {
        return userRepositoryPort.findByUsername(nickname)
                .orElseThrow(() -> new UserNotFoundException("No existe ningún usuario con el nickname: " + nickname));
    }

    /** Una solicitud ajena no se revela: se responde como si no existiera. */
    private Friendship findVisibleTo(UserId actor, String friendshipId) {
        return friendshipRepositoryPort.findById(friendshipId)
                .filter(f -> f.involves(actor))
                .orElseThrow(() -> new FriendshipNotFoundException("La solicitud no existe"));
    }

    private java.util.Optional<PublicUserProfile> profileOf(UserId userId) {
        return userRepositoryPort.findById(userId)
                .map(u -> new PublicUserProfile(u.getUsername(), u.getAvatarUrl(), u.getCreatedAt()));
    }
}
