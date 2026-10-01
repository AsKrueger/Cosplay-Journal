package com.cosplayjournal.infrastructure.adapter.in.rest;

import com.cosplayjournal.domain.model.user.User;
import com.cosplayjournal.domain.model.user.UserId;
import com.cosplayjournal.infrastructure.adapter.in.rest.dto.CreateCosplayRequest;
import com.cosplayjournal.infrastructure.adapter.out.persistence.adapter.JpaUserRepositoryAdapter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockUser(username = "testuser", roles = {"USER"})
class CosplayControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JpaUserRepositoryAdapter userRepositoryAdapter;

    @BeforeEach
    void setUp() {
        if (userRepositoryAdapter.findById(UserId.of("testuser")).isEmpty()) {
            userRepositoryAdapter.save(User.create(UserId.of("testuser"), "testuser", "testuser@example.com", "$2a$10$hash"));
        }
    }

    @Test
    @DisplayName("POST /api/v1/cosplays debe crear un cosplay correctamente asociando el ownerId del usuario autenticado")
    void shouldCreateCosplayViaRest() throws Exception {
        CreateCosplayRequest request = new CreateCosplayRequest(
                "Link BotW",
                "Túnica del elegido",
                "Link",
                "Zelda BotW"
        );

        mockMvc.perform(post("/api/v1/cosplays")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.name", is("Link BotW")))
                .andExpect(jsonPath("$.description", is("Túnica del elegido")))
                .andExpect(jsonPath("$.characterName", is("Link")))
                .andExpect(jsonPath("$.originSeries", is("Zelda BotW")))
                .andExpect(jsonPath("$.status", is("IDEA")));
    }

    @Test
    @DisplayName("POST /api/v1/cosplays debe retornar 400 Bad Request si el nombre es inválido")
    void shouldReturnBadRequestWhenNameIsInvalid() throws Exception {
        CreateCosplayRequest request = new CreateCosplayRequest(
                "",
                "Descripción",
                "Personaje",
                "Serie"
        );

        mockMvc.perform(post("/api/v1/cosplays")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.details", not(empty())));
    }

    @Test
    @DisplayName("GET /api/v1/cosplays/{id} debe retornar 404 Not Found si no existe")
    void shouldReturnNotFoundWhenCosplayDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/v1/cosplays/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.message", containsString("999999")));
    }

    @Test
    @DisplayName("GET /api/v1/health debe retornar status UP")
    void shouldReturnHealthUp() throws Exception {
        mockMvc.perform(get("/api/v1/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("UP")));
    }
}
