package com.cosplayjournal.infrastructure.adapter.out.persistence.repository;

import com.cosplayjournal.infrastructure.adapter.out.persistence.entity.EventJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpringDataEventRepository extends JpaRepository<EventJpaEntity, String> {
    Optional<EventJpaEntity> findBySourceAndExternalId(String source, String externalId);
}
