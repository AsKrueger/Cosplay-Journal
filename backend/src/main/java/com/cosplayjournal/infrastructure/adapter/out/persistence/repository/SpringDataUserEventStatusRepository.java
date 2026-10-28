package com.cosplayjournal.infrastructure.adapter.out.persistence.repository;

import com.cosplayjournal.infrastructure.adapter.out.persistence.entity.UserEventStatusJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SpringDataUserEventStatusRepository extends JpaRepository<UserEventStatusJpaEntity, String> {

    List<UserEventStatusJpaEntity> findByUserId(String userId);

    Optional<UserEventStatusJpaEntity> findByUserIdAndEventId(String userId, String eventId);

    void deleteByUserIdAndEventId(String userId, String eventId);
}
