package com.cosplayjournal.infrastructure.adapter.in.rest;

import com.cosplayjournal.application.dto.GalleryPhotoView;
import com.cosplayjournal.application.port.in.ManageGalleryUseCase;
import com.cosplayjournal.application.port.in.ManageGalleryUseCase.UploadGalleryPhotoCommand;
import com.cosplayjournal.application.port.out.CurrentUserPort;
import com.cosplayjournal.domain.exception.InvalidGalleryDataException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/** Galería personal. Cada foto es del usuario y se etiqueta como evento, cosplay o grupal. */
@RestController
@RequestMapping("/api/v1/gallery/photos")
@Tag(name = "Gallery", description = "Fotos personales etiquetadas por evento, cosplay o grupal")
public class GalleryController {

    private final ManageGalleryUseCase manageGalleryUseCase;
    private final CurrentUserPort currentUserPort;

    public GalleryController(ManageGalleryUseCase manageGalleryUseCase, CurrentUserPort currentUserPort) {
        this.manageGalleryUseCase = manageGalleryUseCase;
        this.currentUserPort = currentUserPort;
    }

    @PostMapping(consumes = "multipart/form-data")
    @Operation(summary = "Subir una foto", description = "JPEG, PNG o WebP de hasta 5 MB. El evento es obligatorio si la etiqueta es EVENTO")
    public ResponseEntity<GalleryPhotoView> upload(
            @RequestPart("file") MultipartFile file,
            @RequestParam String tag,
            @RequestParam(required = false) String eventId
    ) {
        byte[] content;
        try {
            content = file.getBytes();
        } catch (IOException e) {
            throw new InvalidGalleryDataException("No se pudo leer la imagen");
        }
        GalleryPhotoView view = manageGalleryUseCase.upload(
                currentUserPort.getRequiredCurrentUserId().value(),
                new UploadGalleryPhotoCommand(tag, eventId, file.getContentType(), content)
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(view);
    }

    @GetMapping
    @Operation(summary = "Mis fotos", description = "Filtra opcionalmente por etiqueta y por evento")
    public ResponseEntity<List<GalleryPhotoView>> list(
            @RequestParam(required = false) String tag,
            @RequestParam(required = false) String eventId
    ) {
        return ResponseEntity.ok(manageGalleryUseCase.list(currentUserPort.getRequiredCurrentUserId().value(), tag, eventId));
    }
}
