package com.cosplayjournal.application.port.in;

import com.cosplayjournal.domain.model.participation.Participation;
import com.cosplayjournal.domain.model.participation.ParticipationId;

import java.util.List;

public interface GetParticipationUseCase {
    Participation getParticipationById(ParticipationId id);
    List<Participation> getAllParticipations();
}
