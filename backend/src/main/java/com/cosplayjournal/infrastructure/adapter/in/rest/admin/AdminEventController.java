package com.cosplayjournal.infrastructure.adapter.in.rest.admin;

import com.cosplayjournal.application.dto.ImportEventsResult;
import com.cosplayjournal.application.port.in.ImportExternalEventsUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/events")
public class AdminEventController {

    private final ImportExternalEventsUseCase importExternalEventsUseCase;

    public AdminEventController(ImportExternalEventsUseCase importExternalEventsUseCase) {
        this.importExternalEventsUseCase = importExternalEventsUseCase;
    }

    @PostMapping("/import")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ImportEventsResult> importEvents() {
        ImportEventsResult result = importExternalEventsUseCase.importEvents();
        return ResponseEntity.ok(result);
    }
}
