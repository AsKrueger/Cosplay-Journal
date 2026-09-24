package com.cosplayjournal.domain;

import com.cosplayjournal.domain.exception.InvalidParticipationDataException;
import com.cosplayjournal.domain.model.participation.ParticipationId;
import com.cosplayjournal.domain.model.photo.Photo;
import com.cosplayjournal.domain.model.photo.PhotoId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PhotoTest {

    @Test
    @DisplayName("Debe crear una fotografía con datos válidos")
    void shouldCreatePhotoWithValidData() {
        PhotoId id = PhotoId.of("photo-1");
        ParticipationId participationId = ParticipationId.of("part-1");

        Photo photo = Photo.create(
                id,
                participationId,
                "s3://bucket/photos/part-1/photo1.jpg",
                "Foto de grupo en el escenario",
                "user-1"
        );

        assertNotNull(photo);
        assertEquals("photo-1", photo.getId().value());
        assertEquals("part-1", photo.getParticipationId().value());
        assertEquals("s3://bucket/photos/part-1/photo1.jpg", photo.getStorageReference());
        assertEquals("Foto de grupo en el escenario", photo.getCaption());
        assertEquals("user-1", photo.getUploadedByUserId());
        assertNotNull(photo.getUploadedAt());
    }

    @Test
    @DisplayName("Debe lanzar excepción si la referencia de almacenamiento es vacía o nula")
    void shouldThrowExceptionWhenStorageReferenceIsInvalid() {
        PhotoId id = PhotoId.generate();
        ParticipationId participationId = ParticipationId.generate();

        assertThrows(InvalidParticipationDataException.class, () ->
                Photo.create(id, participationId, "", "Caption", "user-1")
        );

        assertThrows(InvalidParticipationDataException.class, () ->
                Photo.create(id, participationId, null, "Caption", "user-1")
        );
    }
}
