package com.cosplayjournal.infrastructure.adapter.out.persistence.repository;

import com.cosplayjournal.infrastructure.adapter.out.persistence.entity.ConversationJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpringDataConversationRepository extends JpaRepository<ConversationJpaEntity, String> {

    @Query("select distinct c from ConversationJpaEntity c join c.members m where m = :userId order by c.createdAt desc")
    List<ConversationJpaEntity> findAllByMember(@Param("userId") String userId);
}
