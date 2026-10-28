package com.cosplayjournal.infrastructure.adapter.in.rest.mapper;

import com.cosplayjournal.application.port.in.CreateCosplayCommand;
import com.cosplayjournal.domain.model.Cosplay;
import com.cosplayjournal.infrastructure.adapter.in.rest.dto.CosplayResponse;
import com.cosplayjournal.infrastructure.adapter.in.rest.dto.CreateCosplayRequest;

public class CosplayRestMapper {

    public static CreateCosplayCommand toCommand(CreateCosplayRequest request) {
        return new CreateCosplayCommand(request.name(), request.photoUrl());
    }

    public static CosplayResponse toResponse(Cosplay cosplay) {
        return new CosplayResponse(
                cosplay.getId(),
                cosplay.getName(),
                cosplay.getPhotoUrl(),
                cosplay.getCreatedAt(),
                cosplay.getUpdatedAt()
        );
    }
}
