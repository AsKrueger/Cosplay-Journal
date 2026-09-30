package com.cosplayjournal.application.port.out;

import com.cosplayjournal.domain.model.user.User;
import com.cosplayjournal.domain.model.user.UserId;

import java.util.Optional;

public interface UserRepositoryPort {
    User save(User user);
    Optional<User> findById(UserId id);
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
}
