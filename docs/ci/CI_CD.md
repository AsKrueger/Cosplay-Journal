# Integración Continua (CI) y Calidad Automatizada — Cosplay Journal

## 1. Visión General

**Cosplay Journal** implementa un pipeline de Integración Continua (CI) automatizado y reproducible mediante **GitHub Actions** ([`.github/workflows/backend-ci.yml`](file:///C:/Users/lovei/Documents/XD/Cosplay-Journal/.github/workflows/backend-ci.yml)).

Cada cambio enviado al repositorio o a través de Pull Requests ejecuta de forma automática el ciclo de validación completo sin depender del entorno local del desarrollador.

---

## 2. Flujo del Pipeline CI

```text
               Git Event (Push / Pull Request)
                             │
                             ▼
                GitHub Actions (ubuntu-latest)
                             │
            ┌────────────────┴────────────────┐
            ▼                                 ▼
    Java 21 LTS (Temurin)              Docker Daemon
   Maven Wrapper (`mvnw`)           PostgreSQL 16 + Kafka
            │                                 │
            └────────────────┬────────────────┘
                             ▼
                `./mvnw clean verify -B`
                             │
       ┌─────────────────────┼─────────────────────┐
       ▼                     ▼                     ▼
Unit & Domain Tests   Architecture Tests   Testcontainers Integration
 (H2 in-memory)      (Hexagonal Boundary)   (PostgreSQL 16 & Kafka)
       │                     │                     │
       └─────────────────────┼─────────────────────┘
                             ▼
                  Artifact Generation (.jar)
                             │
       ┌─────────────────────┴─────────────────────┐
       ▼                                           ▼
Surefire Test Reports Artifact            Backend JAR Artifact
```

---

## 3. Disparadores y Configuración del Workflow

- **Eventos:** `push` a ramas `main`, `master`, `feature/**` y `pull_request` dirigidos a `main` o `master`.
- **Entorno:** `ubuntu-latest` de GitHub Actions (incluye motor de Docker habilitado).
- **Versión de Java:** **Java 21 LTS** (Distribución `temurin`).
- **Caché:** Caché de dependencias Maven activado vía `actions/setup-java`.
- **Ejecución:** `./mvnw clean verify -B` desde el directorio `backend/`.

---

## 4. Validaciones Automatizadas

1. **Compilación y Construcción:** Garantiza que el código Java 21 compila sin errores.
2. **Pruebas Unitarias de Dominio y Aplicación:** Valida las reglas de negocio en memoria.
3. **Aislamiento Arquitectónico ([`ArchitectureIsolationTest`](file:///C:/Users/lovei/Documents/XD/Cosplay-Journal/backend/src/test/java/com/cosplayjournal/domain/ArchitectureIsolationTest.java)):** Comprueba que `domain` y `application` no importan Spring, JPA, Kafka, Security, JJWT, Jsoup, Micrometer, OpenAPI ni Testcontainers.
4. **Testing de Integración con Testcontainers:** Ejecuta las migraciones Flyway V1..V5 contra **PostgreSQL 16 Alpine** y la publicación/consumo de eventos contra **Confluent Kafka 7.6.1**.
5. **Generación de Artefactos:** Si todas las pruebas pasan en verde, se empaqueta el ejecutable JAR de Spring Boot y se guardan los informes Surefire como artefactos del workflow (`actions/upload-artifact@v4`).

---

## 5. Gestión de Errores y Seguridad

- **Manejo de Fallos:** Cualquier error de compilación, fallo de prueba o violación arquitectónica falla inmediatamente el job de CI.
- **Cero Credenciales:** No se hardcodean tokens, credenciales ni claves privadas en el repositorio. Los contenedores de test utilizan credenciales efímeras específicas de entorno de pruebas.
