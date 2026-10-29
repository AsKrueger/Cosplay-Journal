package com.cosplayjournal.infrastructure.adapter.out.persistence.repository;

import com.cosplayjournal.infrastructure.adapter.out.persistence.entity.GalleryPhotoJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SpringDataGalleryPhotoRepository extends JpaRepository<GalleryPhotoJpaEntity, String> {

    @Query("""
            select p from GalleryPhotoJpaEntity p
            where p.ownerId = :ownerId
              and (:tag is null or p.tag = :tag)
              and (:eventId is null or p.eventId = :eventId)
            order by p.uploadedAt desc
            """)
    List<GalleryPhotoJpaEntity> findFiltered(
            @Param("ownerId") String ownerId,
            @Param("tag") String tag,
            @Param("eventId") String eventId
    );
}
