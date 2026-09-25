package com.cosplayjournal.application.port.out;

import com.cosplayjournal.domain.model.participation.Participation;
import com.cosplayjournal.domain.model.participation.ParticipationId;

import java.util.List;
import java.util.Optional;

public interface ParticipationRepositoryPort {
    Participation save(Participation participation);
    Optional<Participation> findById(ParticipationId id);
    List<Participation> findAll();
}
