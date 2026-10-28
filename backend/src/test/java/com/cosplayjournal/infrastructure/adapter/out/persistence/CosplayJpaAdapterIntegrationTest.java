package com.cosplayjournal.infrastructure.adapter.out.persistence;

import com.cosplayjournal.domain.model.Cosplay;
import com.cosplayjournal.infrastructure.adapter.out.persistence.adapter.JpaCosplayRepositoryAdapter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class CosplayJpaAdapterIntegrationTest {

    @Autowired
    private JpaCosplayRepositoryAdapter cosplayRepositoryAdapter;

    @Test
    @DisplayName("Debe guardar y recuperar un Cosplay en la base de datos relacional (JPA + Flyway)")
    void shouldSaveAndFindCosplayInDatabase() {
        Cosplay cosplay = Cosplay.createNew("Batman Begins", null);

        Cosplay saved = cosplayRepositoryAdapter.save(cosplay);

        assertNotNull(saved.getId());
        assertEquals("Batman Begins", saved.getName());
        assertNull(saved.getPhotoUrl());

        Optional<Cosplay> found = cosplayRepositoryAdapter.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals("Batman Begins", found.get().getName());

        List<Cosplay> all = cosplayRepositoryAdapter.findAll();
        assertFalse(all.isEmpty());
    }
}
