package com.cosplayjournal.application.port.in;

public interface AuthenticateUserUseCase {
    AuthenticationResult authenticateUser(AuthenticateUserCommand command);
}
