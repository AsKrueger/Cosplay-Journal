package com.cosplayjournal.application.service;

import com.cosplayjournal.application.dto.GalleryPhotoView;
import com.cosplayjournal.application.port.in.ManageGalleryUseCase;
import com.cosplayjournal.application.port.out.GalleryPhotoRepositoryPort;
import com.cosplayjournal.application.port.out.PhotoStoragePort;
import com.cosplayjournal.domain.exception.InvalidGalleryDataException;
import com.cosplayjournal.domain.model.gallery.GalleryPhoto;
import com.cosplayjournal.domain.model.gallery.GalleryTag;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Galería personal: subida de fotos etiquetadas y consulta por etiqueta o evento. */
public class GalleryApplicationService implements ManageGalleryUseCase {

    public static final long MAX_BYTES = 5L * 1024 * 1024;

    private static final Map<String, String> EXTENSIONS = Map.of(
            "image/jpeg", "jpg",
            "image/png", "png",
            "image/webp", "webp"
    );

    private final GalleryPhotoRepositoryPort photoRepository;
    private final PhotoStoragePort storage;

    public GalleryApplicationService(GalleryPhotoRepositoryPort photoRepository, PhotoStoragePort storage) {
        this.photoRepository = photoRepository;
        this.storage = storage;
    }

    @Override
    public GalleryPhotoView upload(String ownerId, UploadGalleryPhotoCommand command) {
        byte[] content = command.content();
        if (content == null || content.length == 0) {
            throw new InvalidGalleryDataException("Sube una imagen");
        }
        if (content.length > MAX_BYTES) {
            throw new InvalidGalleryDataException("La imagen supera el tamaño máximo de 5 MB");
        }
        String extension = command.contentType() == null ? null : EXTENSIONS.get(command.contentType());
        if (extension == null || !matchesSignature(command.contentType(), content)) {
            throw new InvalidGalleryDataException("Formato no admitido: se aceptan JPEG, PNG o WebP");
        }

        GalleryTag tag = parseTag(command.tag());
        String key = "gallery/" + ownerId + "/" + UUID.randomUUID() + "." + extension;
        String url = storage.store(key, command.contentType(), content);

        GalleryPhoto saved = photoRepository.save(GalleryPhoto.create(ownerId, tag, command.eventId(), url));
        return GalleryPhotoView.from(saved);
    }

    @Override
    public List<GalleryPhotoView> list(String ownerId, String tag, String eventId) {
        GalleryTag filter = tag == null || tag.isBlank() ? null : parseTag(tag);
        return photoRepository.findByOwner(ownerId, filter, eventId).stream()
                .map(GalleryPhotoView::from)
                .toList();
    }

    private static GalleryTag parseTag(String tag) {
        if (tag == null) {
            throw new InvalidGalleryDataException("Indica si la foto es de evento, cosplay o grupal");
        }
        try {
            return GalleryTag.valueOf(tag.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidGalleryDataException("Etiqueta no válida: " + tag);
        }
    }

    /** Comprueba que los primeros bytes son de verdad del tipo declarado, no solo su extensión. */
    static boolean matchesSignature(String contentType, byte[] c) {
        return switch (contentType) {
            case "image/jpeg" -> c.length >= 3 && (c[0] & 0xFF) == 0xFF && (c[1] & 0xFF) == 0xD8 && (c[2] & 0xFF) == 0xFF;
            case "image/png" -> c.length >= 4 && c[0] == (byte) 0x89 && c[1] == 'P' && c[2] == 'N' && c[3] == 'G';
            case "image/webp" -> c.length >= 12 && startsWith(c, "RIFF") && c[8] == 'W' && c[9] == 'E' && c[10] == 'B' && c[11] == 'P';
            default -> false;
        };
    }

    private static boolean startsWith(byte[] c, String text) {
        for (int i = 0; i < text.length(); i++) {
            if (c[i] != text.charAt(i)) return false;
        }
        return true;
    }
}
