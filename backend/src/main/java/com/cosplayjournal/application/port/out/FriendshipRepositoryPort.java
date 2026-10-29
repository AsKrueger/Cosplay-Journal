package com.cosplayjournal.application.port.out;

import com.cosplayjournal.domain.model.social.Friendship;
import com.cosplayjournal.domain.model.user.UserId;

import java.util.List;
import java.util.Optional;

public interface FriendshipRepositoryPort {
    Friendship save(Friendship friendship);
    Optional<Friendship> findById(String id);
    /** La relación entre dos usuarios, en cualquier sentido. */
    Optional<Friendship> findBetween(UserId first, UserId second);
    List<Friendship> findAllInvolving(UserId userId);
    void delete(Friendship friendship);
}
