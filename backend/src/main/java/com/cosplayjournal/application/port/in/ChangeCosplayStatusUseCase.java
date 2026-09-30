package com.cosplayjournal.application.port.in;

import com.cosplayjournal.domain.model.Cosplay;

public interface ChangeCosplayStatusUseCase {
    Cosplay changeCosplayStatus(ChangeCosplayStatusCommand command);
}
