package com.cosplayjournal.infrastructure.adapter.out.persistence.repository;

import com.cosplayjournal.infrastructure.adapter.out.persistence.entity.MessageJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface SpringDataMessageRepository extends JpaRepository<MessageJpaEntity, String> {

    @Query("select m from MessageJpaEntity m where m.conversationId = :conversationId and m.createdAt > :since order by m.createdAt asc")
    List<MessageJpaEntity> findSince(@Param("conversationId") String conversationId, @Param("since") Instant since);
}
