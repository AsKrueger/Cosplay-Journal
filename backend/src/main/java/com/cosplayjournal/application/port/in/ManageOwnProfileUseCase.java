package com.cosplayjournal.application.port.in;

import com.cosplayjournal.application.dto.OwnProfile;
import com.cosplayjournal.domain.model.user.UserId;

public interface ManageOwnProfileUseCase {
    OwnProfile getOwnProfile(UserId userId);
    OwnProfile changeAvatar(UserId userId, String avatarUrl);
}
