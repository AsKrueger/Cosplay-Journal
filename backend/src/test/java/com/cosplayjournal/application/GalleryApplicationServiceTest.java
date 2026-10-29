package com.cosplayjournal.application;

import com.cosplayjournal.application.dto.GalleryPhotoView;
import com.cosplayjournal.application.port.in.ManageGalleryUseCase.UploadGalleryPhotoCommand;
import com.cosplayjournal.application.port.out.GalleryPhotoRepositoryPort;
import com.cosplayjournal.application.service.GalleryApplicationService;
import com.cosplayjournal.domain.exception.InvalidGalleryDataException;
import com.cosplayjournal.domain.model.gallery.GalleryPhoto;
import com.cosplayjournal.domain.model.gallery.GalleryTag;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GalleryApplicationServiceTest {

    private static final byte[] JPEG = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0, 0};
    private static final byte[] PNG = {(byte) 0x89, 'P', 'N', 'G', 13, 10, 26, 10};
    private static final byte[] WEBP = {'R', 'I', 'F', 'F', 0, 0, 0, 0, 'W', 'E', 'B', 'P'};

    @Mock
    private GalleryPhotoRepositoryPort photos;

    private final AtomicReference<String> storedKey = new AtomicReference<>();
    private GalleryApplicationService service;

    @BeforeEach
    void setUp() {
        service = new GalleryApplicationService(photos, (key, contentType, content) -> {
            storedKey.set(key);
            return "http://localhost:8080/uploads/" + key;
        });
    }

    @Test
    void subir_una_foto_jpeg_la_guarda_con_su_etiqueta_y_devuelve_la_url() {
        when(photos.save(any(GalleryPhoto.class))).thenAnswer(invocation -> invocation.getArgument(0));

        GalleryPhotoView view = service.upload("user-1", new UploadGalleryPhotoCommand("cosplay", null, "image/jpeg", JPEG));

        assertEquals("COSPLAY", view.tag());
        assertNull(view.eventId());
        assertTrue(view.url().startsWith("http://localhost:8080/uploads/gallery/user-1/"));
        assertTrue(view.url().endsWith(".jpg"));
        assertEquals(view.url(), "http://localhost:8080/uploads/" + storedKey.get());
    }

    @Test
    void una_foto_de_evento_guarda_el_evento_y_la_etiqueta_en_minusculas() {
        when(photos.save(any(GalleryPhoto.class))).thenAnswer(invocation -> invocation.getArgument(0));

        GalleryPhotoView view = service.upload("user-1", new UploadGalleryPhotoCommand("evento", "evt-7", "image/png", PNG));

        assertEquals("EVENTO", view.tag());
        assertEquals("evt-7", view.eventId());
        assertTrue(storedKey.get().endsWith(".png"));
    }

    @Test
    void acepta_webp_con_su_firma() {
        when(photos.save(any(GalleryPhoto.class))).thenAnswer(invocation -> invocation.getArgument(0));

        GalleryPhotoView view = service.upload("user-1", new UploadGalleryPhotoCommand("GRUPAL", null, "image/webp", WEBP));

        assertEquals("GRUPAL", view.tag());
        assertTrue(storedKey.get().endsWith(".webp"));
    }

    @Test
    void rechaza_un_archivo_cuya_firma_no_coincide_con_el_tipo_declarado() {
        InvalidGalleryDataException error = assertThrows(InvalidGalleryDataException.class,
                () -> service.upload("user-1", new UploadGalleryPhotoCommand("COSPLAY", null, "image/jpeg", PNG)));

        assertTrue(error.getMessage().contains("Formato no admitido"));
        verifyNoInteractions(photos);
    }

    @Test
    void rechaza_tipos_que_no_son_imagenes_admitidas() {
        assertThrows(InvalidGalleryDataException.class,
                () -> service.upload("user-1", new UploadGalleryPhotoCommand("COSPLAY", null, "image/gif", JPEG)));
        assertThrows(InvalidGalleryDataException.class,
                () -> service.upload("user-1", new UploadGalleryPhotoCommand("COSPLAY", null, null, JPEG)));
        verifyNoInteractions(photos);
    }

    @Test
    void rechaza_una_imagen_vacia_o_de_mas_de_5_mb() {
        assertThrows(InvalidGalleryDataException.class,
                () -> service.upload("user-1", new UploadGalleryPhotoCommand("COSPLAY", null, "image/jpeg", new byte[0])));

        byte[] tooBig = new byte[(int) GalleryApplicationService.MAX_BYTES + 1];
        System.arraycopy(JPEG, 0, tooBig, 0, JPEG.length);
        InvalidGalleryDataException error = assertThrows(InvalidGalleryDataException.class,
                () -> service.upload("user-1", new UploadGalleryPhotoCommand("COSPLAY", null, "image/jpeg", tooBig)));

        assertTrue(error.getMessage().contains("5 MB"));
        verifyNoInteractions(photos);
    }

    @Test
    void rechaza_una_etiqueta_desconocida() {
        assertThrows(InvalidGalleryDataException.class,
                () -> service.upload("user-1", new UploadGalleryPhotoCommand("PERSONAL", null, "image/jpeg", JPEG)));
        assertThrows(InvalidGalleryDataException.class,
                () -> service.upload("user-1", new UploadGalleryPhotoCommand(null, null, "image/jpeg", JPEG)));
        verifyNoInteractions(photos);
    }

    @Test
    void una_foto_de_evento_sin_evento_se_rechaza() {
        assertThrows(InvalidGalleryDataException.class,
                () -> service.upload("user-1", new UploadGalleryPhotoCommand("EVENTO", null, "image/jpeg", JPEG)));
        verifyNoInteractions(photos);
    }

    @Test
    void listar_sin_filtros_pasa_nulos_al_repositorio() {
        when(photos.findByOwner("user-1", null, null)).thenReturn(List.of(
                GalleryPhoto.restore("p1", "user-1", GalleryTag.COSPLAY, null, "http://x/p1.jpg", java.time.Instant.now())
        ));

        List<GalleryPhotoView> list = service.list("user-1", " ", null);

        assertEquals(1, list.size());
        assertEquals("p1", list.get(0).id());
        verify(photos).findByOwner("user-1", null, null);
    }

    @Test
    void listar_por_etiqueta_y_evento_los_pasa_al_repositorio() {
        when(photos.findByOwner(anyString(), any(), anyString())).thenReturn(List.of());

        service.list("user-1", "evento", "evt-7");

        ArgumentCaptor<GalleryTag> tag = ArgumentCaptor.forClass(GalleryTag.class);
        verify(photos).findByOwner(org.mockito.ArgumentMatchers.eq("user-1"), tag.capture(), org.mockito.ArgumentMatchers.eq("evt-7"));
        assertEquals(GalleryTag.EVENTO, tag.getValue());
    }
}
