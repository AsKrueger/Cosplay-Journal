package com.cosplayjournal.infrastructure.adapter.out.persistence.repository;

import com.cosplayjournal.infrastructure.adapter.out.persistence.entity.PhotoJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataPhotoRepository extends JpaRepository<PhotoJpaEntity, String> {
    List<PhotoJpaEntity> findByParticipationId(String participationId);
}
