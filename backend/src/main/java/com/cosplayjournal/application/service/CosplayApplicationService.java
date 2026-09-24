package com.cosplayjournal.application.service;

import com.cosplayjournal.application.port.in.CreateCosplayCommand;
import com.cosplayjournal.application.port.in.CreateCosplayUseCase;
import com.cosplayjournal.application.port.in.GetCosplayUseCase;
import com.cosplayjournal.application.port.out.CosplayRepositoryPort;
import com.cosplayjournal.domain.exception.CosplayNotFoundException;
import com.cosplayjournal.domain.model.Cosplay;

import java.util.List;

public class CosplayApplicationService implements CreateCosplayUseCase, GetCosplayUseCase {

    private final CosplayRepositoryPort cosplayRepositoryPort;

    public CosplayApplicationService(CosplayRepositoryPort cosplayRepositoryPort) {
        this.cosplayRepositoryPort = cosplayRepositoryPort;
    }

    @Override
    public Cosplay createCosplay(CreateCosplayCommand command) {
        Cosplay cosplay = Cosplay.createNew(
                command.name(),
                command.description(),
                command.characterName(),
                command.originSeries()
        );
        return cosplayRepositoryPort.save(cosplay);
    }

    @Override
    public Cosplay getCosplayById(Long id) {
        return cosplayRepositoryPort.findById(id)
                .orElseThrow(() -> new CosplayNotFoundException(id));
    }

    @Override
    public List<Cosplay> getAllCosplays() {
        return cosplayRepositoryPort.findAll();
    }
}
