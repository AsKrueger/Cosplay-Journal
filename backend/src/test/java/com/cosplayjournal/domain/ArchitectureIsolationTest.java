package com.cosplayjournal.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ArchitectureIsolationTest {

    @Test
    @DisplayName("El paquete com.cosplayjournal.domain no debe importar Spring, JPA, Hibernate ni PostgreSQL")
    void domainPackageMustNotImportFrameworks() throws IOException {
        Path domainDir = Paths.get("src/main/java/com/cosplayjournal/domain");
        if (!Files.exists(domainDir)) {
            domainDir = Paths.get("backend/src/main/java/com/cosplayjournal/domain");
        }

        assertTrue(Files.exists(domainDir), "El directorio del dominio debe existir");

        List<String> forbiddenImports = List.of(
                "org.springframework",
                "jakarta.persistence",
                "javax.persistence",
                "org.hibernate",
                "org.postgresql",
                "com.fasterxml.jackson"
        );

        try (Stream<Path> paths = Files.walk(domainDir)) {
            paths.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".java"))
                    .forEach(path -> {
                        try {
                            List<String> lines = Files.readAllLines(path);
                            for (String line : lines) {
                                String trimmed = line.trim();
                                if (trimmed.startsWith("import ")) {
                                    for (String forbidden : forbiddenImports) {
                                        if (trimmed.contains(forbidden)) {
                                            throw new AssertionError(
                                                    "Violación de Arquitectura Hexagonal en " + path.getFileName() +
                                                            ": El dominio importa " + forbidden + " en la línea: " + line
                                            );
                                        }
                                    }
                                }
                            }
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
        }
    }
}
