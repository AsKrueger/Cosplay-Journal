package com.cosplayjournal.application.port.out;

import com.cosplayjournal.domain.model.Cosplay;

import java.util.List;
import java.util.Optional;

public interface CosplayRepositoryPort {
    Cosplay save(Cosplay cosplay);
    Optional<Cosplay> findById(Long id);
    List<Cosplay> findAll();
}
