package com.cosplayjournal.infrastructure.adapter.out.persistence.repository;

import com.cosplayjournal.infrastructure.adapter.out.persistence.entity.FriendshipJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SpringDataFriendshipRepository extends JpaRepository<FriendshipJpaEntity, String> {

    @Query("select f from FriendshipJpaEntity f where " +
           "(f.requesterId = :first and f.addresseeId = :second) or " +
           "(f.requesterId = :second and f.addresseeId = :first)")
    Optional<FriendshipJpaEntity> findBetween(@Param("first") String first, @Param("second") String second);

    @Query("select f from FriendshipJpaEntity f where f.requesterId = :userId or f.addresseeId = :userId")
    List<FriendshipJpaEntity> findInvolving(@Param("userId") String userId);
}
