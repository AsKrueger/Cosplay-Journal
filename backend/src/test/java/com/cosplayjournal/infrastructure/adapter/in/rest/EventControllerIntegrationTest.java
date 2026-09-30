package com.cosplayjournal.infrastructure.adapter.in.rest;

import com.cosplayjournal.domain.model.event.EventSource;
import com.cosplayjournal.infrastructure.adapter.in.rest.dto.CreateEventRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockUser(username = "testuser", roles = {"USER"})
class EventControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("POST /api/v1/events debe crear un evento correctamente")
    void shouldCreateEventViaRest() throws Exception {
        CreateEventRequest request = new CreateEventRequest(
                "Japan Weekend Madrid",
                "Gran evento en IFEMA",
                LocalDate.now().plusDays(10),
                LocalDate.now().plusDays(12),
                "Madrid",
                "IFEMA",
                "Madrid",
                "España",
                "Av. del Partenón, 5",
                40.465,
                -3.618,
                "https://japanweekend.com",
                EventSource.LISTADOMANGA
        );

        mockMvc.perform(post("/api/v1/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.name", is("Japan Weekend Madrid")))
                .andExpect(jsonPath("$.city", is("Madrid")))
                .andExpect(jsonPath("$.venue", is("IFEMA")))
                .andExpect(jsonPath("$.source", is("LISTADOMANGA")))
                .andExpect(jsonPath("$.status", is("SCHEDULED")));
    }

    @Test
    @DisplayName("GET /api/v1/events/{id} debe retornar 404 Not Found si no existe")
    void shouldReturnNotFoundWhenEventDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/v1/events/non-existing-evt-id"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)));
    }
}
