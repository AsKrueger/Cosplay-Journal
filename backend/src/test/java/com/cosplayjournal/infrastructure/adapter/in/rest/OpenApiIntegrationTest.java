package com.cosplayjournal.infrastructure.adapter.in.rest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OpenApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("GET /v3/api-docs debe generar el documento OpenAPI 3.0 con títulos, esquemas de seguridad y rutas")
    void shouldGenerateOpenApiDocumentation() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.openapi", startsWith("3.0")))
                .andExpect(jsonPath("$.info.title", is("Cosplay Journal REST API")))
                .andExpect(jsonPath("$.info.version", is("v1.0.0")))
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth", notNullValue()))
                .andExpect(jsonPath("$.paths['/api/v1/auth/login']", notNullValue()))
                .andExpect(jsonPath("$.paths['/api/v1/cosplays']", notNullValue()))
                .andExpect(jsonPath("$.paths['/api/v1/events']", notNullValue()))
                .andExpect(jsonPath("$.paths['/api/v1/participations']", notNullValue()))
                .andExpect(jsonPath("$.paths['/api/v1/photos']", notNullValue()))
                .andExpect(jsonPath("$.paths['/api/v1/admin/events/import']", notNullValue()));
    }
}
