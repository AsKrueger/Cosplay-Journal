package com.cosplayjournal.application.port.in;

import com.cosplayjournal.domain.model.Cosplay;

public interface CreateCosplayUseCase {
    Cosplay createCosplay(CreateCosplayCommand command);
}
