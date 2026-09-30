package com.cosplayjournal.infrastructure.adapter.in.rest;

import com.cosplayjournal.domain.model.Cosplay;
import com.cosplayjournal.domain.model.event.Event;
import com.cosplayjournal.domain.model.event.EventId;
import com.cosplayjournal.domain.model.event.EventLocation;
import com.cosplayjournal.domain.model.event.EventSource;
import com.cosplayjournal.domain.model.participation.ParticipantRole;
import com.cosplayjournal.domain.model.participation.ParticipationType;
import com.cosplayjournal.infrastructure.adapter.in.rest.dto.AssignCharacterRequest;
import com.cosplayjournal.infrastructure.adapter.in.rest.dto.CreateParticipationRequest;
import com.cosplayjournal.infrastructure.adapter.in.rest.dto.JoinParticipationRequest;
import com.cosplayjournal.infrastructure.adapter.out.persistence.adapter.JpaCosplayRepositoryAdapter;
import com.cosplayjournal.infrastructure.adapter.out.persistence.adapter.JpaEventRepositoryAdapter;
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

import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockUser(username = "testuser", roles = {"USER"})
class ParticipationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JpaEventRepositoryAdapter eventRepositoryAdapter;

    @Autowired
    private JpaCosplayRepositoryAdapter cosplayRepositoryAdapter;

    private Event event;
    private Cosplay cosplay;

    @BeforeEach
    void setUp() {
        event = eventRepositoryAdapter.save(Event.create(
                EventId.of("evt-part-rest-1"), "Comic Con Sevilla", "Desc",
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(3),
                EventLocation.of("Sevilla", "Fibes"), "", EventSource.MANUAL_ADMIN
        ));

        cosplay = cosplayRepositoryAdapter.save(
                Cosplay.createNew("Zoro", "3 espadas", "Roronoa Zoro", "One Piece")
        );
    }

    @Test
    @DisplayName("Flujo completo de Participación: Crear, unirse miembro y asignar personaje vía REST")
    void shouldExecuteFullParticipationRestWorkflow() throws Exception {
        // 1. Crear participación
        CreateParticipationRequest createRequest = new CreateParticipationRequest(
                event.getId().value(),
                cosplay.getId(),
                ParticipationType.GROUP,
                "user-leader-rest",
                "Alonso Leader",
                "Grupo Mugiwara"
        );

        String responseContent = mockMvc.perform(post("/api/v1/participations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.type", is("GROUP")))
                .andExpect(jsonPath("$.groupName", is("Grupo Mugiwara")))
                .andExpect(jsonPath("$.participants", hasSize(1)))
                .andReturn().getResponse().getContentAsString();

        String participationId = objectMapper.readTree(responseContent).get("id").asText();

        // 2. Unirse miembro
        JoinParticipationRequest joinRequest = new JoinParticipationRequest(
                "user-member-2",
                "Beatriz",
                ParticipantRole.MEMBER
        );

        mockMvc.perform(post("/api/v1/participations/" + participationId + "/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(joinRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.participants", hasSize(2)));

        // 3. Asignar personaje
        AssignCharacterRequest assignRequest = new AssignCharacterRequest(
                "user-leader-rest",
                "Roronoa Zoro"
        );

        mockMvc.perform(put("/api/v1/participations/" + participationId + "/characters")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(assignRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.participants[0].assignedCharacter", is("Roronoa Zoro")));
    }
}
