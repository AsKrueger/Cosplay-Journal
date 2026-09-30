package com.cosplayjournal.infrastructure.adapter.out.persistence.mapper;

import com.cosplayjournal.domain.model.user.User;
import com.cosplayjournal.domain.model.user.UserId;
import com.cosplayjournal.domain.model.user.UserRole;
import com.cosplayjournal.domain.model.user.UserStatus;
import com.cosplayjournal.infrastructure.adapter.out.persistence.entity.UserJpaEntity;

public class UserPersistenceMapper {

    public static UserJpaEntity toJpaEntity(User user) {
        if (user == null) return null;
        return new UserJpaEntity(
                user.getId().value(),
                user.getUsername(),
                user.getEmail(),
                user.getPasswordHash(),
                user.getRole().name(),
                user.getStatus().name(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

    public static User toDomain(UserJpaEntity entity) {
        if (entity == null) return null;
        return new User(
                UserId.of(entity.getId()),
                entity.getUsername(),
                entity.getEmail(),
                entity.getPasswordHash(),
                UserRole.valueOf(entity.getRole()),
                UserStatus.valueOf(entity.getStatus()),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
