package com.cosplayjournal.infrastructure.adapter.out.persistence.repository;

import com.cosplayjournal.infrastructure.adapter.out.persistence.entity.ParticipationJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataParticipationRepository extends JpaRepository<ParticipationJpaEntity, String> {
}
