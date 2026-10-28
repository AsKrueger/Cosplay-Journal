package com.cosplayjournal.infrastructure.adapter.out.persistence.adapter;

import com.cosplayjournal.application.port.out.FriendshipRepositoryPort;
import com.cosplayjournal.domain.model.social.Friendship;
import com.cosplayjournal.domain.model.user.UserId;
import com.cosplayjournal.infrastructure.adapter.out.persistence.entity.FriendshipJpaEntity;
import com.cosplayjournal.infrastructure.adapter.out.persistence.mapper.FriendshipPersistenceMapper;
import com.cosplayjournal.infrastructure.adapter.out.persistence.repository.SpringDataFriendshipRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public class JpaFriendshipRepositoryAdapter implements FriendshipRepositoryPort {

    private final SpringDataFriendshipRepository springDataFriendshipRepository;

    public JpaFriendshipRepositoryAdapter(SpringDataFriendshipRepository springDataFriendshipRepository) {
        this.springDataFriendshipRepository = springDataFriendshipRepository;
    }

    @Override
    @Transactional
    public Friendship save(Friendship friendship) {
        FriendshipJpaEntity saved = springDataFriendshipRepository.save(
                FriendshipPersistenceMapper.toJpaEntity(friendship));
        return FriendshipPersistenceMapper.toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Friendship> findById(String id) {
        if (id == null) return Optional.empty();
        return springDataFriendshipRepository.findById(id).map(FriendshipPersistenceMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Friendship> findBetween(UserId first, UserId second) {
        return springDataFriendshipRepository.findBetween(first.value(), second.value())
                .map(FriendshipPersistenceMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Friendship> findAllInvolving(UserId userId) {
        return springDataFriendshipRepository.findInvolving(userId.value()).stream()
                .map(FriendshipPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    @Transactional
    public void delete(Friendship friendship) {
        springDataFriendshipRepository.deleteById(friendship.getId());
    }
}
