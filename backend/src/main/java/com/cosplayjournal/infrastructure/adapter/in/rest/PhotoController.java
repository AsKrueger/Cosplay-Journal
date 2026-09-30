package com.cosplayjournal.infrastructure.adapter.in.rest;

import com.cosplayjournal.application.port.in.AddPhotoUseCase;
import com.cosplayjournal.application.port.in.GetParticipationPhotosUseCase;
import com.cosplayjournal.application.port.in.GetPhotoUseCase;
import com.cosplayjournal.domain.model.participation.ParticipationId;
import com.cosplayjournal.domain.model.photo.Photo;
import com.cosplayjournal.domain.model.photo.PhotoId;
import com.cosplayjournal.infrastructure.adapter.in.rest.dto.AddPhotoRequest;
import com.cosplayjournal.infrastructure.adapter.in.rest.dto.PhotoResponse;
import com.cosplayjournal.infrastructure.adapter.in.rest.mapper.PhotoRestMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class PhotoController {

    private final AddPhotoUseCase addPhotoUseCase;
    private final GetPhotoUseCase getPhotoUseCase;
    private final GetParticipationPhotosUseCase getParticipationPhotosUseCase;

    public PhotoController(
            AddPhotoUseCase addPhotoUseCase,
            GetPhotoUseCase getPhotoUseCase,
            GetParticipationPhotosUseCase getParticipationPhotosUseCase
    ) {
        this.addPhotoUseCase = addPhotoUseCase;
        this.getPhotoUseCase = getPhotoUseCase;
        this.getParticipationPhotosUseCase = getParticipationPhotosUseCase;
    }

    @PostMapping("/photos")
    public ResponseEntity<PhotoResponse> addPhoto(@Valid @RequestBody AddPhotoRequest request) {
        Photo photo = addPhotoUseCase.addPhoto(PhotoRestMapper.toCommand(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(PhotoRestMapper.toResponse(photo));
    }

    @GetMapping("/photos/{id}")
    public ResponseEntity<PhotoResponse> getPhotoById(@PathVariable String id) {
        Photo photo = getPhotoUseCase.getPhotoById(PhotoId.of(id));
        return ResponseEntity.ok(PhotoRestMapper.toResponse(photo));
    }

    @GetMapping("/participations/{participationId}/photos")
    public ResponseEntity<List<PhotoResponse>> getPhotosByParticipationId(@PathVariable String participationId) {
        List<PhotoResponse> photos = getParticipationPhotosUseCase.getPhotosByParticipationId(ParticipationId.of(participationId)).stream()
                .map(PhotoRestMapper::toResponse)
                .toList();
        return ResponseEntity.ok(photos);
    }
}
