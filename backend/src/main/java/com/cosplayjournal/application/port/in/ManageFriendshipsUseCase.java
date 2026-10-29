package com.cosplayjournal.application.port.in;

import com.cosplayjournal.application.dto.FriendRequestSummary;
import com.cosplayjournal.application.dto.PublicUserProfile;
import com.cosplayjournal.domain.model.social.Friendship;
import com.cosplayjournal.domain.model.user.UserId;

import java.util.List;

public interface ManageFriendshipsUseCase {
    Friendship sendRequest(UserId actor, String nickname);
    void accept(UserId actor, String friendshipId);
    void reject(UserId actor, String friendshipId);
    /** Quita una amistad, o cancela una solicitud pendiente enviada por el propio usuario. */
    void removeFriend(UserId actor, String nickname);
    List<PublicUserProfile> listFriends(UserId actor);
    List<FriendRequestSummary> listIncomingRequests(UserId actor);
}
