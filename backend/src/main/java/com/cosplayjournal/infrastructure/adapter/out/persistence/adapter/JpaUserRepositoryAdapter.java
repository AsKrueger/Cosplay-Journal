package com.cosplayjournal.infrastructure.adapter.out.persistence.adapter;

import com.cosplayjournal.application.port.out.UserRepositoryPort;
import com.cosplayjournal.domain.model.user.User;
import com.cosplayjournal.domain.model.user.UserId;
import com.cosplayjournal.infrastructure.adapter.out.persistence.entity.UserJpaEntity;
import com.cosplayjournal.infrastructure.adapter.out.persistence.mapper.UserPersistenceMapper;
import com.cosplayjournal.infrastructure.adapter.out.persistence.repository.SpringDataUserRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public class JpaUserRepositoryAdapter implements UserRepositoryPort {

    private final SpringDataUserRepository springDataUserRepository;

    public JpaUserRepositoryAdapter(SpringDataUserRepository springDataUserRepository) {
        this.springDataUserRepository = springDataUserRepository;
    }

    @Override
    @Transactional
    public User save(User user) {
        UserJpaEntity entity = UserPersistenceMapper.toJpaEntity(user);
        UserJpaEntity saved = springDataUserRepository.save(entity);
        return UserPersistenceMapper.toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findById(UserId id) {
        if (id == null) return Optional.empty();
        return springDataUserRepository.findById(id.value())
                .map(UserPersistenceMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        if (email == null) return Optional.empty();
        return springDataUserRepository.findByEmailIgnoreCase(email.trim())
                .map(UserPersistenceMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        if (username == null) return Optional.empty();
        return springDataUserRepository.findByUsernameIgnoreCase(username.trim())
                .map(UserPersistenceMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return email != null && springDataUserRepository.existsByEmailIgnoreCase(email.trim());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return username != null && springDataUserRepository.existsByUsernameIgnoreCase(username.trim());
    }
}
