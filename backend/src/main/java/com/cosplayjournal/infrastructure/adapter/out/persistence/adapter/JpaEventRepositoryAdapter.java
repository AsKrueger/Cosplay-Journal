package com.cosplayjournal.infrastructure.adapter.out.persistence.adapter;

import com.cosplayjournal.application.port.out.EventRepositoryPort;
import com.cosplayjournal.domain.model.event.Event;
import com.cosplayjournal.domain.model.event.EventId;
import com.cosplayjournal.infrastructure.adapter.out.persistence.entity.EventJpaEntity;
import com.cosplayjournal.infrastructure.adapter.out.persistence.mapper.EventPersistenceMapper;
import com.cosplayjournal.infrastructure.adapter.out.persistence.repository.SpringDataEventRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public class JpaEventRepositoryAdapter implements EventRepositoryPort {

    private final SpringDataEventRepository springDataEventRepository;

    public JpaEventRepositoryAdapter(SpringDataEventRepository springDataEventRepository) {
        this.springDataEventRepository = springDataEventRepository;
    }

    @Override
    @Transactional
    public Event save(Event event) {
        EventJpaEntity entity = EventPersistenceMapper.toJpaEntity(event);
        EventJpaEntity saved = springDataEventRepository.save(entity);
        return EventPersistenceMapper.toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Event> findById(EventId id) {
        if (id == null) return Optional.empty();
        return springDataEventRepository.findById(id.value())
                .map(EventPersistenceMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Event> findAll() {
        return springDataEventRepository.findAll().stream()
                .map(EventPersistenceMapper::toDomain)
                .toList();
    }
}
