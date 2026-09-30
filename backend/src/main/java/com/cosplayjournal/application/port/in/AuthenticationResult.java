package com.cosplayjournal.application.port.in;

import com.cosplayjournal.domain.model.user.User;

public record AuthenticationResult(
        User user,
        String token,
        long expiresInSeconds
) {
}
