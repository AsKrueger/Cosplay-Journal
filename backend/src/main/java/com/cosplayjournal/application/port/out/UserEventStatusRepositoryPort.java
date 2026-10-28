package com.cosplayjournal.application.port.out;

import com.cosplayjournal.domain.model.social.UserEventStatus;
import com.cosplayjournal.domain.model.user.UserId;

import java.util.List;
import java.util.Optional;

public interface UserEventStatusRepositoryPort {
    List<UserEventStatus> findAllByUser(UserId userId);
    Optional<UserEventStatus> findByUserAndEvent(UserId userId, String eventId);
    UserEventStatus save(UserEventStatus status);
    void delete(UserId userId, String eventId);
}
