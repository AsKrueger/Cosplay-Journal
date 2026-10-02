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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Photos", description = "Galería de fotos asociadas a participaciones")
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
    @Operation(summary = "Añadir foto a participación", description = "Asocia una foto a una participación existente asignando uploadedByUserId del usuario autenticado")
    @ApiResponse(responseCode = "201", description = "Foto añadida exitosamente")
    @ApiResponse(responseCode = "400", description = "Petición inválida")
    @ApiResponse(responseCode = "403", description = "No autorizado para añadir fotos a esta participación")
    public ResponseEntity<PhotoResponse> addPhoto(@Valid @RequestBody AddPhotoRequest request) {
        Photo photo = addPhotoUseCase.addPhoto(PhotoRestMapper.toCommand(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(PhotoRestMapper.toResponse(photo));
    }

    @GetMapping("/photos/{id}")
    @Operation(summary = "Obtener foto por ID", description = "Devuelve la información de la fotografía")
    @ApiResponse(responseCode = "200", description = "Foto encontrada")
    @ApiResponse(responseCode = "404", description = "Foto no encontrada")
    public ResponseEntity<PhotoResponse> getPhotoById(@PathVariable String id) {
        Photo photo = getPhotoUseCase.getPhotoById(PhotoId.of(id));
        return ResponseEntity.ok(PhotoRestMapper.toResponse(photo));
    }

    @GetMapping("/participations/{participationId}/photos")
    @Operation(summary = "Obtener fotos de una participación", description = "Devuelve la lista de fotografías compartidas en una participación")
    @ApiResponse(responseCode = "200", description = "Listado de fotos de la participación")
    public ResponseEntity<List<PhotoResponse>> getPhotosByParticipationId(@PathVariable String participationId) {
        List<PhotoResponse> photos = getParticipationPhotosUseCase
                .getPhotosByParticipationId(ParticipationId.of(participationId)).stream()
                .map(PhotoRestMapper::toResponse)
                .toList();
        return ResponseEntity.ok(photos);
    }
}
