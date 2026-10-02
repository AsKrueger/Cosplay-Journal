package com.cosplayjournal.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ArchitectureIsolationTest {

    @Test
    @DisplayName("El paquete com.cosplayjournal.domain no debe importar Spring, JPA, Hibernate, Kafka, Security, JJWT, Jsoup, OpenAPI ni Swagger")
    void domainPackageMustNotImportFrameworks() throws IOException {
        Path domainDir = resolveDir("domain");
        assertTrue(Files.exists(domainDir), "El directorio del dominio debe existir");

        List<String> forbiddenImports = List.of(
                "org.springframework",
                "jakarta.persistence",
                "javax.persistence",
                "org.hibernate",
                "org.postgresql",
                "com.fasterxml.jackson",
                "org.jsoup",
                "io.jsonwebtoken",
                "io.micrometer",
                "io.swagger.v3",
                "org.springdoc"
        );

        assertNoForbiddenImports(domainDir, forbiddenImports, "dominio");
    }

    @Test
    @DisplayName("El paquete com.cosplayjournal.application no debe importar Kafka, JPA, Spring Data, SecurityContextHolder, JJWT, Jsoup, OpenAPI ni Swagger")
    void applicationPackageMustNotImportKafka() throws IOException {
        Path applicationDir = resolveDir("application");
        assertTrue(Files.exists(applicationDir), "El directorio de aplicación debe existir");

        List<String> forbiddenImports = List.of(
                "org.springframework.kafka",
                "org.apache.kafka",
                "jakarta.persistence",
                "javax.persistence",
                "org.hibernate",
                "org.jsoup",
                "io.jsonwebtoken",
                "org.springframework.data",
                "org.springframework.security.core.context.SecurityContextHolder",
                "org.springframework.scheduling",
                "io.micrometer",
                "io.swagger.v3",
                "org.springdoc"
        );

        assertNoForbiddenImports(applicationDir, forbiddenImports, "aplicación");
    }

    private Path resolveDir(String subPackage) {
        Path path = Paths.get("src/main/java/com/cosplayjournal/" + subPackage);
        if (!Files.exists(path)) {
            path = Paths.get("backend/src/main/java/com/cosplayjournal/" + subPackage);
        }
        return path;
    }

    private void assertNoForbiddenImports(Path dir, List<String> forbiddenImports, String layerName) throws IOException {
        try (Stream<Path> paths = Files.walk(dir)) {
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
                                                    "Violación de Arquitectura Hexagonal en la capa de " + layerName + " (" + path.getFileName() +
                                                            "): Importa " + forbidden + " en la línea: " + line
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
