package com.cosplayjournal.application.port.in;

import com.cosplayjournal.domain.model.participation.Participation;

public interface CreateParticipationUseCase {
    Participation createParticipation(CreateParticipationCommand command);
}
