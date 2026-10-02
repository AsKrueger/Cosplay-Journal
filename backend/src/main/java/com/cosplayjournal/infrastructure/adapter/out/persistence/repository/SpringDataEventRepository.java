package com.cosplayjournal.infrastructure.adapter.out.persistence.repository;

import com.cosplayjournal.infrastructure.adapter.out.persistence.entity.EventJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface SpringDataEventRepository extends JpaRepository<EventJpaEntity, String>, JpaSpecificationExecutor<EventJpaEntity> {
    Optional<EventJpaEntity> findBySourceAndExternalId(String source, String externalId);
}
