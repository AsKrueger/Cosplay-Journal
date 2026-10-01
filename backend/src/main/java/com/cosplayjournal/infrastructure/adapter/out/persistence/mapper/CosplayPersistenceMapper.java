package com.cosplayjournal.infrastructure.adapter.out.persistence.mapper;

import com.cosplayjournal.domain.model.Cosplay;
import com.cosplayjournal.domain.model.CosplayStatus;
import com.cosplayjournal.domain.model.user.UserId;
import com.cosplayjournal.infrastructure.adapter.out.persistence.entity.CosplayJpaEntity;

public class CosplayPersistenceMapper {

    public static CosplayJpaEntity toJpaEntity(Cosplay cosplay) {
        if (cosplay == null) return null;
        return new CosplayJpaEntity(
                cosplay.getId(),
                cosplay.getOwnerId().value(),
                cosplay.getName(),
                cosplay.getDescription(),
                cosplay.getCharacterName(),
                cosplay.getOriginSeries(),
                cosplay.getStatus().name(),
                cosplay.getCreatedAt(),
                cosplay.getUpdatedAt()
        );
    }

    public static Cosplay toDomain(CosplayJpaEntity entity) {
        if (entity == null) return null;
        return new Cosplay(
                entity.getId(),
                UserId.of(entity.getOwnerId() != null ? entity.getOwnerId() : "system-default"),
                entity.getName(),
                entity.getDescription(),
                entity.getCharacterName(),
                entity.getOriginSeries(),
                CosplayStatus.valueOf(entity.getStatus()),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
