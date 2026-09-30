package com.cosplayjournal.application.port.out;

import com.cosplayjournal.domain.model.user.User;

public interface TokenProviderPort {
    String generateToken(User user);
    boolean validateToken(String token);
    String getUserIdFromToken(String token);
    String getUsernameFromToken(String token);
    String getRoleFromToken(String token);
}
