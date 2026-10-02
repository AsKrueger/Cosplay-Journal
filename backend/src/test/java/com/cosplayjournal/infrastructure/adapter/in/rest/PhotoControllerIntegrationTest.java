package com.cosplayjournal.infrastructure.adapter.in.rest;

import com.cosplayjournal.domain.model.Cosplay;
import com.cosplayjournal.domain.model.event.Event;
import com.cosplayjournal.domain.model.event.EventId;
import com.cosplayjournal.domain.model.event.EventLocation;
import com.cosplayjournal.domain.model.event.EventSource;
import com.cosplayjournal.domain.model.participation.Participant;
import com.cosplayjournal.domain.model.participation.Participation;
import com.cosplayjournal.domain.model.participation.ParticipationId;
import com.cosplayjournal.domain.model.participation.ParticipationType;
import com.cosplayjournal.domain.model.user.User;
import com.cosplayjournal.domain.model.user.UserId;
import com.cosplayjournal.infrastructure.adapter.in.rest.dto.AddPhotoRequest;
import com.cosplayjournal.infrastructure.adapter.out.persistence.adapter.JpaCosplayRepositoryAdapter;
import com.cosplayjournal.infrastructure.adapter.out.persistence.adapter.JpaEventRepositoryAdapter;
import com.cosplayjournal.infrastructure.adapter.out.persistence.adapter.JpaParticipationRepositoryAdapter;
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

import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockUser(username = "testuser", roles = {"USER"})
class PhotoControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JpaEventRepositoryAdapter eventRepositoryAdapter;

    @Autowired
    private JpaCosplayRepositoryAdapter cosplayRepositoryAdapter;

    @Autowired
    private JpaParticipationRepositoryAdapter participationRepositoryAdapter;

    @Autowired
    private JpaUserRepositoryAdapter userRepositoryAdapter;

    private Participation participation;

    @BeforeEach
    void setUp() {
        if (userRepositoryAdapter.findById(UserId.of("testuser")).isEmpty()) {
            userRepositoryAdapter.save(User.create(UserId.of("testuser"), "testuser", "testuser@example.com", "$2a$10$hash"));
        }

        Event event = eventRepositoryAdapter.save(Event.create(
                EventId.of("evt-photo-rest"), "Expocomic", "Desc",
                LocalDate.now(), LocalDate.now(), EventLocation.of("Madrid", "IFEMA"), "", EventSource.MANUAL_ADMIN
        ));

        Cosplay cosplay = cosplayRepositoryAdapter.save(Cosplay.createNew("Batman", "Desc", "Batman", "DC", UserId.of("testuser")));

        participation = participationRepositoryAdapter.save(Participation.create(
                ParticipationId.of("part-photo-rest"),
                UserId.of("testuser"),
                event.getId(),
                cosplay.getId(),
                ParticipationType.INDIVIDUAL,
                Participant.createLeader("testuser", "Carlos")
        ));
    }

    @Test
    @DisplayName("Flujo de Photo REST: Añadir foto y consultar por participationId")
    void shouldAddAndGetPhotosViaRest() throws Exception {
        AddPhotoRequest addRequest = new AddPhotoRequest(
                participation.getId().value(),
                "s3://bucket/photos/img-rest.jpg",
                "Foto en el photocall principal",
                "testuser"
        );

        String responseContent = mockMvc.perform(post("/api/v1/photos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.storageReference", is("s3://bucket/photos/img-rest.jpg")))
                .andExpect(jsonPath("$.caption", is("Foto en el photocall principal")))
                .andReturn().getResponse().getContentAsString();

        String photoId = objectMapper.readTree(responseContent).get("id").asText();

        // Obtener por ID
        mockMvc.perform(get("/api/v1/photos/" + photoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(photoId)));

        // Obtener por participationId
        mockMvc.perform(get("/api/v1/participations/" + participation.getId().value() + "/photos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }
}
