package com.cosplayjournal.application.port.out;

import com.cosplayjournal.domain.model.user.UserId;

import java.util.Optional;

public interface CurrentUserPort {
    Optional<UserId> getCurrentUserId();
    UserId getRequiredCurrentUserId();
    boolean isAuthenticated();
    boolean isAdmin();
}
