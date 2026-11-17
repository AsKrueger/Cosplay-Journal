package com.cosplayjournal.infrastructure.adapter.out.storage;

import com.cosplayjournal.application.port.out.PhotoStoragePort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Guarda las fotos en una carpeta del servidor. Pensado para desarrollo: en producción se
 * sustituye por un adaptador de almacenamiento de objetos (R2 o S3).
 */
@Component
public class LocalDiskPhotoStorageAdapter implements PhotoStoragePort {

    private final Path root;
    private final String publicBaseUrl;

    public LocalDiskPhotoStorageAdapter(
            @Value("${photos.storage.dir:uploads}") String dir,
            @Value("${photos.public-base-url:http://localhost:8080/uploads}") String publicBaseUrl
    ) {
        this.root = Path.of(dir).toAbsolutePath().normalize();
        this.publicBaseUrl = publicBaseUrl.endsWith("/") ? publicBaseUrl.substring(0, publicBaseUrl.length() - 1) : publicBaseUrl;
    }

    public Path root() {
        return root;
    }

    @Override
    public String store(String key, String contentType, byte[] content) {
        Path target = root.resolve(key).normalize();
        if (!target.startsWith(root)) {
            throw new IllegalArgumentException("Ruta de almacenamiento no válida");
        }
        try {
            Files.createDirectories(target.getParent());
            Files.write(target, content);
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo guardar la foto", e);
        }
        return publicBaseUrl + "/" + key;
    }
}
