package com.cosplayjournal.infrastructure.adapter.out.persistence.mapper;

import com.cosplayjournal.domain.model.event.EventId;
import com.cosplayjournal.domain.model.participation.*;
import com.cosplayjournal.domain.model.user.UserId;
import com.cosplayjournal.infrastructure.adapter.out.persistence.entity.ParticipantJpaEntity;
import com.cosplayjournal.infrastructure.adapter.out.persistence.entity.ParticipantJpaEntityId;
import com.cosplayjournal.infrastructure.adapter.out.persistence.entity.ParticipationJpaEntity;

import java.util.ArrayList;
import java.util.List;

public class ParticipationPersistenceMapper {

    public static ParticipationJpaEntity toJpaEntity(Participation participation) {
        if (participation == null) return null;

        ParticipationJpaEntity entity = new ParticipationJpaEntity(
                participation.getId().value(),
                participation.getCreatorId().value(),
                participation.getEventId().value(),
                participation.getCosplayId(),
                participation.getType().name(),
                participation.getStatus().name(),
                participation.getGroupName(),
                participation.getCreatedAt(),
                participation.getUpdatedAt()
        );

        if (participation.getParticipants() != null) {
            for (Participant p : participation.getParticipants()) {
                ParticipantJpaEntityId pId = new ParticipantJpaEntityId(p.getUserId(), participation.getId().value());
                ParticipantJpaEntity pEntity = new ParticipantJpaEntity(
                        pId,
                        p.getName(),
                        p.getRole().name(),
                        p.getAssignedCharacter(),
                        p.getJoinedAt(),
                        entity
                );
                entity.addParticipant(pEntity);
            }
        }

        return entity;
    }

    public static Participation toDomain(ParticipationJpaEntity entity) {
        if (entity == null) return null;

        List<Participant> domainParticipants = new ArrayList<>();
        if (entity.getParticipants() != null) {
            for (ParticipantJpaEntity pEntity : entity.getParticipants()) {
                Participant p = new Participant(
                        pEntity.getId().getUserId(),
                        pEntity.getName(),
                        ParticipantRole.valueOf(pEntity.getRole()),
                        pEntity.getAssignedCharacter(),
                        pEntity.getJoinedAt()
                );
                domainParticipants.add(p);
            }
        }

        return new Participation(
                ParticipationId.of(entity.getId()),
                UserId.of(entity.getCreatorId() != null ? entity.getCreatorId() : "system-default"),
                EventId.of(entity.getEventId()),
                entity.getCosplayId(),
                ParticipationType.valueOf(entity.getType()),
                ParticipationStatus.valueOf(entity.getStatus()),
                entity.getGroupName(),
                domainParticipants,
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
