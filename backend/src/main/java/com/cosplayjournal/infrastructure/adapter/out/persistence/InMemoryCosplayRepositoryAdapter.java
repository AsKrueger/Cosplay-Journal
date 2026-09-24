package com.cosplayjournal.infrastructure.adapter.out.persistence;

import com.cosplayjournal.application.port.out.CosplayRepositoryPort;
import com.cosplayjournal.domain.model.Cosplay;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class InMemoryCosplayRepositoryAdapter implements CosplayRepositoryPort {

    private final Map<Long, Cosplay> storage = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    @Override
    public Cosplay save(Cosplay cosplay) {
        if (cosplay.getId() == null) {
            Long newId = idGenerator.getAndIncrement();
            Cosplay saved = cosplay.withId(newId);
            storage.put(newId, saved);
            return saved;
        } else {
            storage.put(cosplay.getId(), cosplay);
            return cosplay;
        }
    }

    @Override
    public Optional<Cosplay> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<Cosplay> findAll() {
        return new ArrayList<>(storage.values());
    }

    public void clear() {
        storage.clear();
        idGenerator.set(1);
    }
}
