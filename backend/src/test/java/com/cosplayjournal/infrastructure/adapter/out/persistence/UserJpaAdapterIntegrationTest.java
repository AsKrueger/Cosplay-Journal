package com.cosplayjournal.infrastructure.adapter.out.persistence;

import com.cosplayjournal.domain.model.user.User;
import com.cosplayjournal.domain.model.user.UserId;
import com.cosplayjournal.infrastructure.adapter.out.persistence.adapter.JpaUserRepositoryAdapter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class UserJpaAdapterIntegrationTest {

    @Autowired
    private JpaUserRepositoryAdapter userRepositoryAdapter;

    @Test
    @DisplayName("Debe guardar y consultar User en base de datos PostgreSQL/H2 mediante JPA")
    void shouldSaveAndFindUserInDatabase() {
        UserId userId = UserId.generate();
        User user = User.create(userId, "cosplayer1", "cosplayer1@example.com", "$2a$10$hash");

        User saved = userRepositoryAdapter.save(user);

        assertNotNull(saved);
        assertEquals("cosplayer1", saved.getUsername());

        Optional<User> foundByEmail = userRepositoryAdapter.findByEmail("cosplayer1@example.com");
        assertTrue(foundByEmail.isPresent());
        assertEquals("cosplayer1", foundByEmail.get().getUsername());

        assertTrue(userRepositoryAdapter.existsByUsername("cosplayer1"));
    }
}
