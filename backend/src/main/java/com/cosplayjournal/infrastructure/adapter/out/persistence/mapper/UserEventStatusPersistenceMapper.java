package com.cosplayjournal.infrastructure.adapter.out.persistence.mapper;

import com.cosplayjournal.domain.model.social.UserEventStatus;
import com.cosplayjournal.domain.model.user.UserId;
import com.cosplayjournal.infrastructure.adapter.out.persistence.entity.UserEventStatusJpaEntity;

public class UserEventStatusPersistenceMapper {

    public static UserEventStatusJpaEntity toJpaEntity(UserEventStatus status) {
        return new UserEventStatusJpaEntity(
                key(status.userId().value(), status.eventId()),
                status.userId().value(),
                status.eventId(),
                status.status(),
                status.updatedAt()
        );
    }

    public static UserEventStatus toDomain(UserEventStatusJpaEntity entity) {
        return new UserEventStatus(
                UserId.of(entity.getUserId()),
                entity.getEventId(),
                entity.getStatus(),
                entity.getUpdatedAt()
        );
    }

    public static String key(String userId, String eventId) {
        return userId + "|" + eventId;
    }
}
