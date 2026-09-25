package com.cosplayjournal.infrastructure.adapter.out.persistence.adapter;

import com.cosplayjournal.application.port.out.ParticipationRepositoryPort;
import com.cosplayjournal.domain.model.participation.Participation;
import com.cosplayjournal.domain.model.participation.ParticipationId;
import com.cosplayjournal.infrastructure.adapter.out.persistence.entity.ParticipationJpaEntity;
import com.cosplayjournal.infrastructure.adapter.out.persistence.mapper.ParticipationPersistenceMapper;
import com.cosplayjournal.infrastructure.adapter.out.persistence.repository.SpringDataParticipationRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public class JpaParticipationRepositoryAdapter implements ParticipationRepositoryPort {

    private final SpringDataParticipationRepository springDataParticipationRepository;

    public JpaParticipationRepositoryAdapter(SpringDataParticipationRepository springDataParticipationRepository) {
        this.springDataParticipationRepository = springDataParticipationRepository;
    }

    @Override
    @Transactional
    public Participation save(Participation participation) {
        ParticipationJpaEntity entity = ParticipationPersistenceMapper.toJpaEntity(participation);
        ParticipationJpaEntity saved = springDataParticipationRepository.save(entity);
        return ParticipationPersistenceMapper.toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Participation> findById(ParticipationId id) {
        if (id == null) return Optional.empty();
        return springDataParticipationRepository.findById(id.value())
                .map(ParticipationPersistenceMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Participation> findAll() {
        return springDataParticipationRepository.findAll().stream()
                .map(ParticipationPersistenceMapper::toDomain)
                .toList();
    }
}
