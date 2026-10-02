package com.cosplayjournal.infrastructure.adapter.in.rest.mapper;

import com.cosplayjournal.application.port.in.ChangeCosplayStatusCommand;
import com.cosplayjournal.application.port.in.CreateCosplayCommand;
import com.cosplayjournal.domain.model.Cosplay;
import com.cosplayjournal.infrastructure.adapter.in.rest.dto.ChangeCosplayStatusRequest;
import com.cosplayjournal.infrastructure.adapter.in.rest.dto.CosplayResponse;
import com.cosplayjournal.infrastructure.adapter.in.rest.dto.CreateCosplayRequest;

public class CosplayRestMapper {

    public static CreateCosplayCommand toCommand(CreateCosplayRequest request) {
        return new CreateCosplayCommand(
                request.name(),
                request.description(),
                request.characterName(),
                request.originSeries()
        );
    }

    public static ChangeCosplayStatusCommand toStatusCommand(Long id, ChangeCosplayStatusRequest request) {
        return new ChangeCosplayStatusCommand(id, request.status());
    }

    public static CosplayResponse toResponse(Cosplay cosplay) {
        return new CosplayResponse(
                cosplay.getId(),
                cosplay.getName(),
                cosplay.getDescription(),
                cosplay.getCharacterName(),
                cosplay.getOriginSeries(),
                cosplay.getStatus().name(),
                cosplay.getCreatedAt(),
                cosplay.getUpdatedAt()
        );
    }
}
