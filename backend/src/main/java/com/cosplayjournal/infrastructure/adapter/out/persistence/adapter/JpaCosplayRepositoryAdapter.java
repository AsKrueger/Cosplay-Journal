package com.cosplayjournal.infrastructure.adapter.out.persistence.adapter;

import com.cosplayjournal.application.port.out.CosplayRepositoryPort;
import com.cosplayjournal.domain.model.Cosplay;
import com.cosplayjournal.infrastructure.adapter.out.persistence.entity.CosplayJpaEntity;
import com.cosplayjournal.infrastructure.adapter.out.persistence.mapper.CosplayPersistenceMapper;
import com.cosplayjournal.infrastructure.adapter.out.persistence.repository.SpringDataCosplayRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@Primary
public class JpaCosplayRepositoryAdapter implements CosplayRepositoryPort {

    private final SpringDataCosplayRepository springDataCosplayRepository;

    public JpaCosplayRepositoryAdapter(SpringDataCosplayRepository springDataCosplayRepository) {
        this.springDataCosplayRepository = springDataCosplayRepository;
    }

    @Override
    @Transactional
    public Cosplay save(Cosplay cosplay) {
        CosplayJpaEntity entity = CosplayPersistenceMapper.toJpaEntity(cosplay);
        CosplayJpaEntity saved = springDataCosplayRepository.save(entity);
        return CosplayPersistenceMapper.toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Cosplay> findById(Long id) {
        return springDataCosplayRepository.findById(id)
                .map(CosplayPersistenceMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Cosplay> findAll() {
        return springDataCosplayRepository.findAll().stream()
                .map(CosplayPersistenceMapper::toDomain)
                .toList();
    }
}
