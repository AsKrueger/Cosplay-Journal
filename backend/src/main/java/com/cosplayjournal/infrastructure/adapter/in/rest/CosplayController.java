package com.cosplayjournal.infrastructure.adapter.in.rest;

import com.cosplayjournal.application.port.in.CreateCosplayUseCase;
import com.cosplayjournal.application.port.in.GetCosplayUseCase;
import com.cosplayjournal.domain.model.Cosplay;
import com.cosplayjournal.infrastructure.adapter.in.rest.dto.CosplayResponse;
import com.cosplayjournal.infrastructure.adapter.in.rest.dto.CreateCosplayRequest;
import com.cosplayjournal.infrastructure.adapter.in.rest.mapper.CosplayRestMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/cosplays")
public class CosplayController {

    private final CreateCosplayUseCase createCosplayUseCase;
    private final GetCosplayUseCase getCosplayUseCase;

    public CosplayController(CreateCosplayUseCase createCosplayUseCase, GetCosplayUseCase getCosplayUseCase) {
        this.createCosplayUseCase = createCosplayUseCase;
        this.getCosplayUseCase = getCosplayUseCase;
    }

    @PostMapping
    public ResponseEntity<CosplayResponse> createCosplay(@Valid @RequestBody CreateCosplayRequest request) {
        Cosplay created = createCosplayUseCase.createCosplay(CosplayRestMapper.toCommand(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(CosplayRestMapper.toResponse(created));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CosplayResponse> getCosplayById(@PathVariable Long id) {
        Cosplay cosplay = getCosplayUseCase.getCosplayById(id);
        return ResponseEntity.ok(CosplayRestMapper.toResponse(cosplay));
    }

    @GetMapping
    public ResponseEntity<List<CosplayResponse>> getAllCosplays() {
        List<CosplayResponse> cosplays = getCosplayUseCase.getAllCosplays().stream()
                .map(CosplayRestMapper::toResponse)
                .toList();
        return ResponseEntity.ok(cosplays);
    }
}
