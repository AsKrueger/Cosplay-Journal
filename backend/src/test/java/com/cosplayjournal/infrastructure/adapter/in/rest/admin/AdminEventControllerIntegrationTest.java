package com.cosplayjournal.infrastructure.adapter.in.rest.admin;

import com.cosplayjournal.application.dto.ExternalEventData;
import com.cosplayjournal.application.port.out.ExternalEventSourcePort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdminEventControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ExternalEventSourcePort externalEventSourcePort;

    @Test
    @DisplayName("POST /api/v1/admin/events/import sin token JWT debe retornar 401 Unauthorized")
    void shouldReturn401WhenUnauthenticated() throws Exception {
        mockMvc.perform(post("/api/v1/admin/events/import"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "normal_user", roles = {"USER"})
    @DisplayName("POST /api/v1/admin/events/import con rol USER debe retornar 403 Forbidden")
    void shouldReturn403WhenUserIsNotAdmin() throws Exception {
        mockMvc.perform(post("/api/v1/admin/events/import"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin_user", roles = {"ADMIN"})
    @DisplayName("POST /api/v1/admin/events/import con rol ADMIN debe ejecutar la importación y retornar 200 OK")
    void shouldImportEventsSuccessfullyWhenAdmin() throws Exception {
        ExternalEventData event1 = new ExternalEventData(
                "lm-admin-1", "Salón Manga BCN", "Desc",
                LocalDate.now().plusDays(5), LocalDate.now().plusDays(7),
                "Barcelona", "Fira", "Barcelona", "https://salonmangabcn.com"
        );

        when(externalEventSourcePort.fetchEvents()).thenReturn(List.of(event1));

        mockMvc.perform(post("/api/v1/admin/events/import")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalFound", is(1)))
                .andExpect(jsonPath("$.created", is(1)))
                .andExpect(jsonPath("$.skipped", is(0)));
    }
}
