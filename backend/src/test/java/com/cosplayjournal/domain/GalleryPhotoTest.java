package com.cosplayjournal.domain;

import com.cosplayjournal.domain.exception.InvalidGalleryDataException;
import com.cosplayjournal.domain.model.gallery.GalleryPhoto;
import com.cosplayjournal.domain.model.gallery.GalleryTag;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class GalleryPhotoTest {

    @Test
    void una_foto_de_cosplay_no_necesita_evento() {
        GalleryPhoto photo = GalleryPhoto.create("user-1", GalleryTag.COSPLAY, null, "http://x/a.jpg");

        assertNull(photo.getEventId());
        assertNotNull(photo.getId());
        assertEquals("user-1", photo.getOwnerId());
    }

    @Test
    void una_foto_de_evento_exige_el_evento() {
        assertThrows(InvalidGalleryDataException.class,
                () -> GalleryPhoto.create("user-1", GalleryTag.EVENTO, " ", "http://x/a.jpg"));
    }

    @Test
    void sin_propietario_o_sin_etiqueta_no_se_crea() {
        assertThrows(InvalidGalleryDataException.class,
                () -> GalleryPhoto.create(" ", GalleryTag.COSPLAY, null, "http://x/a.jpg"));
        assertThrows(InvalidGalleryDataException.class,
                () -> GalleryPhoto.create("user-1", null, null, "http://x/a.jpg"));
    }

    @Test
    void la_url_es_obligatoria_y_tiene_limite_de_longitud() {
        assertThrows(InvalidGalleryDataException.class,
                () -> GalleryPhoto.create("user-1", GalleryTag.GRUPAL, null, ""));
        String longUrl = "http://x/" + "a".repeat(500);
        assertThrows(InvalidGalleryDataException.class,
                () -> GalleryPhoto.create("user-1", GalleryTag.GRUPAL, null, longUrl));
    }

    @Test
    void restaurar_conserva_los_datos_guardados() {
        Instant when = Instant.parse("2026-10-29T10:00:00Z");

        GalleryPhoto photo = GalleryPhoto.restore("p1", "user-1", GalleryTag.EVENTO, "evt-7", "http://x/a.jpg", when);

        assertEquals("p1", photo.getId());
        assertEquals(GalleryTag.EVENTO, photo.getTag());
        assertEquals("evt-7", photo.getEventId());
        assertEquals(when, photo.getUploadedAt());
    }
}
