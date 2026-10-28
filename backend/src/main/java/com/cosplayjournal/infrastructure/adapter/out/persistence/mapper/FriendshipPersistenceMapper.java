package com.cosplayjournal.infrastructure.adapter.out.persistence.mapper;

import com.cosplayjournal.domain.model.social.Friendship;
import com.cosplayjournal.domain.model.social.FriendshipStatus;
import com.cosplayjournal.domain.model.user.UserId;
import com.cosplayjournal.infrastructure.adapter.out.persistence.entity.FriendshipJpaEntity;

public class FriendshipPersistenceMapper {

    public static FriendshipJpaEntity toJpaEntity(Friendship friendship) {
        if (friendship == null) return null;
        return new FriendshipJpaEntity(
                friendship.getId(),
                friendship.getRequesterId().value(),
                friendship.getAddresseeId().value(),
                friendship.getStatus().name(),
                friendship.getCreatedAt(),
                friendship.getRespondedAt()
        );
    }

    public static Friendship toDomain(FriendshipJpaEntity entity) {
        if (entity == null) return null;
        return Friendship.restore(
                entity.getId(),
                UserId.of(entity.getRequesterId()),
                UserId.of(entity.getAddresseeId()),
                FriendshipStatus.valueOf(entity.getStatus()),
                entity.getCreatedAt(),
                entity.getRespondedAt()
        );
    }
}
