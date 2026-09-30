package com.cosplayjournal.infrastructure.adapter.in.rest.auth;

import com.cosplayjournal.infrastructure.adapter.in.rest.dto.LoginRequest;
import com.cosplayjournal.infrastructure.adapter.in.rest.dto.RegisterUserRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Flujo completo de Autenticación: Registro, Login y Acceso a Recurso Protegido")
    void shouldExecuteFullAuthenticationWorkflow() throws Exception {
        // 1. Intentar acceder a endpoint protegido sin token -> 401 Unauthorized
        mockMvc.perform(get("/api/v1/cosplays"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is(401)));

        // 2. Registro de nuevo usuario -> 201 Created
        RegisterUserRequest registerRequest = new RegisterUserRequest(
                "cosplayer_sec",
                "sec_user@example.com",
                "password123"
        );

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.username", is("cosplayer_sec")))
                .andExpect(jsonPath("$.email", is("sec_user@example.com")));

        // 3. Login con credenciales -> 200 OK + JWT
        LoginRequest loginRequest = new LoginRequest("sec_user@example.com", "password123");

        String loginResponse = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", notNullValue()))
                .andExpect(jsonPath("$.tokenType", is("Bearer")))
                .andReturn().getResponse().getContentAsString();

        String token = objectMapper.readTree(loginResponse).get("accessToken").asText();

        // 4. Acceder a endpoint protegido con Header Authorization Bearer -> 200 OK
        mockMvc.perform(get("/api/v1/cosplays")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/v1/auth/register debe retornar 409 Conflict al intentar registrar un usuario duplicado")
    void shouldReturnConflictOnDuplicateUser() throws Exception {
        RegisterUserRequest request = new RegisterUserRequest("dup_user", "dup@example.com", "pass123");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)));
    }
}
