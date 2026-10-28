package com.cosplayjournal.infrastructure.adapter.out.persistence.adapter;

import com.cosplayjournal.application.port.out.UserEventStatusRepositoryPort;
import com.cosplayjournal.domain.model.social.UserEventStatus;
import com.cosplayjournal.domain.model.user.UserId;
import com.cosplayjournal.infrastructure.adapter.out.persistence.mapper.UserEventStatusPersistenceMapper;
import com.cosplayjournal.infrastructure.adapter.out.persistence.repository.SpringDataUserEventStatusRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public class JpaUserEventStatusRepositoryAdapter implements UserEventStatusRepositoryPort {

    private final SpringDataUserEventStatusRepository springDataRepository;

    public JpaUserEventStatusRepositoryAdapter(SpringDataUserEventStatusRepository springDataRepository) {
        this.springDataRepository = springDataRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserEventStatus> findAllByUser(UserId userId) {
        return springDataRepository.findByUserId(userId.value()).stream()
                .map(UserEventStatusPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserEventStatus> findByUserAndEvent(UserId userId, String eventId) {
        return springDataRepository.findByUserIdAndEventId(userId.value(), eventId)
                .map(UserEventStatusPersistenceMapper::toDomain);
    }

    @Override
    @Transactional
    public UserEventStatus save(UserEventStatus status) {
        return UserEventStatusPersistenceMapper.toDomain(
                springDataRepository.save(UserEventStatusPersistenceMapper.toJpaEntity(status)));
    }

    @Override
    @Transactional
    public void delete(UserId userId, String eventId) {
        springDataRepository.deleteByUserIdAndEventId(userId.value(), eventId);
    }
}
