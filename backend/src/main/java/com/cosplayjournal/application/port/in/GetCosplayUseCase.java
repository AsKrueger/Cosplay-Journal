package com.cosplayjournal.application.port.in;

import com.cosplayjournal.domain.model.Cosplay;

import java.util.List;

public interface GetCosplayUseCase {
    Cosplay getCosplayById(Long id);
    List<Cosplay> getAllCosplays();
}
