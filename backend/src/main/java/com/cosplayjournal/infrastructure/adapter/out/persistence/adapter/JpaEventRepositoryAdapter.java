package com.cosplayjournal.infrastructure.adapter.out.persistence.adapter;

import com.cosplayjournal.application.dto.EventSearchCriteria;
import com.cosplayjournal.application.dto.PageResult;
import com.cosplayjournal.application.port.out.EventRepositoryPort;
import com.cosplayjournal.domain.model.event.Event;
import com.cosplayjournal.domain.model.event.EventId;
import com.cosplayjournal.domain.model.event.EventSource;
import com.cosplayjournal.infrastructure.adapter.out.persistence.entity.EventJpaEntity;
import com.cosplayjournal.infrastructure.adapter.out.persistence.mapper.EventPersistenceMapper;
import com.cosplayjournal.infrastructure.adapter.out.persistence.repository.SpringDataEventRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
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
    public Optional<Event> findBySourceAndExternalId(EventSource source, String externalId) {
        if (source == null || externalId == null) return Optional.empty();
        return springDataEventRepository.findBySourceAndExternalId(source.name(), externalId.trim())
                .map(EventPersistenceMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Event> findAll() {
        return springDataEventRepository.findAll().stream()
                .map(EventPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<Event> search(EventSearchCriteria criteria) {
        Specification<EventJpaEntity> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (criteria.city() != null) {
                predicates.add(cb.like(cb.lower(root.get("city")), "%" + criteria.city().toLowerCase() + "%"));
            }

            if (criteria.from() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("startDate"), criteria.from()));
            }

            if (criteria.to() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("endDate"), criteria.to()));
            }

            if (criteria.source() != null) {
                predicates.add(cb.equal(root.get("source"), criteria.source().name()));
            }

            if (criteria.status() != null) {
                predicates.add(cb.equal(root.get("status"), criteria.status().name()));
            }

            if (criteria.query() != null) {
                String searchTerm = "%" + criteria.query().toLowerCase() + "%";
                Predicate nameLike = cb.like(cb.lower(root.get("name")), searchTerm);
                Predicate descLike = cb.like(cb.lower(root.get("description")), searchTerm);
                predicates.add(cb.or(nameLike, descLike));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Pageable pageable = PageRequest.of(
                criteria.page(),
                criteria.size(),
                Sort.by("startDate").ascending().and(Sort.by("id").ascending())
        );

        Page<EventJpaEntity> jpaPage = springDataEventRepository.findAll(spec, pageable);

        List<Event> domainEvents = jpaPage.getContent().stream()
                .map(EventPersistenceMapper::toDomain)
                .toList();

        return new PageResult<>(
                domainEvents,
                jpaPage.getNumber(),
                jpaPage.getSize(),
                jpaPage.getTotalElements(),
                jpaPage.getTotalPages(),
                jpaPage.isLast()
        );
    }
}
