# Testing de Integración con Testcontainers — Cosplay Journal

## 1. Visión General

**Cosplay Journal** emplea **Testcontainers** para ejecutar pruebas de integración sobre instancias reales, efímeras y aisladas de **PostgreSQL 16 (Alpine)** y **Apache Kafka (KRaft)** en entorno de pruebas.

---

## 2. Arquitectura de Testing de Integración

```text
               Test Execution Suite
                         │
        ┌────────────────┴────────────────┐
        ▼                                 ▼
PostgreSQL 16 Container            Kafka Container
  (Flyway V1..V5)                  (Topics Reales)
        │                                 │
        ▼                                 ▼
JPA / Repository Adapters        Kafka Producer / Consumer
        │                                 │
        └────────────────┬────────────────┘
                         ▼
             End-To-End Integration Test
```

---

## 3. Clases de Prueba y Perfil `testcontainers`

- **Clase Base Abstracta:** [`AbstractTestcontainersIntegrationTest`](file:///C:/Users/lovei/Documents/XD/Cosplay-Journal/backend/src/test/java/com/cosplayjournal/infrastructure/AbstractTestcontainersIntegrationTest.java) levanta los contenedores de Docker mediante `@Testcontainers` y configura dinámicamente las propiedades de Spring `spring.datasource.url` y `spring.kafka.bootstrap-servers`.
- **Pruebas de Integración:**
  - [`PostgresFlywayRepositoryIntegrationTest`](file:///C:/Users/lovei/Documents/XD/Cosplay-Journal/backend/src/test/java/com/cosplayjournal/infrastructure/PostgresFlywayRepositoryIntegrationTest.java): Válida las 5 migraciones Flyway relacionales e interacciones JPA/PostgreSQL real.
  - [`KafkaMessagingIntegrationTest`](file:///C:/Users/lovei/Documents/XD/Cosplay-Journal/backend/src/test/java/com/cosplayjournal/infrastructure/KafkaMessagingIntegrationTest.java): Válida la publicación y consumo de mensajes de eventos de dominio sobre Kafka real.
  - [`EndToEndIntegrationWorkflowTest`](file:///C:/Users/lovei/Documents/XD/Cosplay-Journal/backend/src/test/java/com/cosplayjournal/infrastructure/EndToEndIntegrationWorkflowTest.java): Prueba el flujo completo de autenticación JWT y consulta de datos.

---

## 4. Rol de H2 vs. Testcontainers

| Entorno / Perfil | Tecnología | Objetivo | Tiempo de Ejecución |
|---|---|---|---|
| `profile=test` | H2 in-memory | Tests unitarios y de lógica de negocio ultrarrápidos para desarrollo diario. | < 15 segundos |
| `profile=testcontainers` | PostgreSQL 16 + Kafka (Docker) | Tests de integración con la infraestructura de producción real. | ~ 25 segundos |

---

## 5. Requisitos para la Ejecución

- **Docker:** Tener Docker Desktop o Docker Daemon en ejecución en la máquina.
- **Comando:** `mvn test` en el directorio `backend/`.
