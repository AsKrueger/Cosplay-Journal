package com.cosplayjournal.application.port.in;

import com.cosplayjournal.domain.model.user.User;

public interface RegisterUserUseCase {
    User registerUser(RegisterUserCommand command);
}
