package com.cosplayjournal.application.port.in;

import com.cosplayjournal.application.dto.PublicUserProfile;

public interface GetPublicProfileUseCase {
    PublicUserProfile getPublicProfile(String nickname);
}
