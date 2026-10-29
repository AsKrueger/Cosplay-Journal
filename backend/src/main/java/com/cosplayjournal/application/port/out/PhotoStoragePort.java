package com.cosplayjournal.application.port.out;

/** Guarda el contenido de una imagen y devuelve la URL pública con la que se muestra. */
public interface PhotoStoragePort {

    String store(String key, String contentType, byte[] content);
}
