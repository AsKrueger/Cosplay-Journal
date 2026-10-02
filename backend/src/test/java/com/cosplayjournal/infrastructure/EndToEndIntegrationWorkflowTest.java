package com.cosplayjournal.infrastructure;

import com.cosplayjournal.infrastructure.adapter.in.rest.dto.LoginRequest;
import com.cosplayjournal.infrastructure.adapter.in.rest.dto.RegisterUserRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
class EndToEndIntegrationWorkflowTest extends AbstractTestcontainersIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Flujo End-To-End completo ejecutado sobre contenedores reales de PostgreSQL 16 y Kafka")
    void shouldExecuteFullEndToEndWorkflowOnRealContainers() throws Exception {
        // 1. Registro de usuario sobre PostgreSQL 16 real
        RegisterUserRequest registerRequest = new RegisterUserRequest(
                "e2e_user",
                "e2e@example.com",
                "e2epassword123"
        );

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.username", is("e2e_user")));

        // 2. Login
        LoginRequest loginRequest = new LoginRequest("e2e@example.com", "e2epassword123");

        String loginResponse = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", notNullValue()))
                .andReturn().getResponse().getContentAsString();

        String token = objectMapper.readTree(loginResponse).get("accessToken").asText();

        // 3. Acceder a endpoint protegido con Bearer JWT
        mockMvc.perform(get("/api/v1/cosplays")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }
}
