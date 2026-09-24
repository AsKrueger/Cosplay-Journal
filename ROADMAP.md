# Hoja de Ruta (Roadmap) — Cosplay Journal

La evolución de **Cosplay Journal** se divide en fases consecutivas e incrementales, garantizando la calidad del software y la utilidad real del producto en cada paso.

---

## 📍 Fase 0: Auditoría, Dirección Técnica y Redefinición (Completada — Issue #0)
- Auditoría integral del proyecto Android previo.
- Definición de Visión de Producto, Alcance MVP y Funcionalidades fuera del MVP.
- Establecimiento de la Arquitectura Hexagonal y Stack Backend (Java 21, Spring Boot 3, PostgreSQL, Kafka, K8s).
- Documentación de arquitectura, modelo de dominio, matriz de decisiones y hoja de ruta.

---

## 🏗️ Fase 1: Arquitectura Base del Backend Hexagonal (Issue #1)
- Creación de la estructura del proyecto Java 21 / Spring Boot 3 con módulos Hexagonales.
- Configuración de Gradle, JUnit 5, Mockito y Jacoco.
- Definición de entidades de dominio puras: `Event`, `CosplayProject`, `Participation`.
- Definición de Puertos de Entrada (`Inbound Ports`) y Puertos de Salida (`Outbound Ports`).
- Tests unitarios de dominio sin dependencias externas (100% cobertura en dominio).

---

## 📅 Fase 2: Módulo de Eventos e Importador Scraper (Issue #2)
- Implementación de la persistencia relacional PostgreSQL + Flyway para Eventos.
- Desarrollo del adaptador de salida `EventScraperAdapter` (migrando Jsoup al backend) con semilla de respaldo `events.json`.
- Implementación de la API REST de Eventos:
  - `GET /api/v1/events` (Búsqueda, filtrado por ciudad/fecha, paginación).
  - `GET /api/v1/events/{id}` (Detalle del evento).
- Pruebas de integración con **Testcontainers** (PostgreSQL).
- Documentación OpenAPI 3.0 / Swagger UI.

---

## 🧵 Fase 3: Módulo de Cosplay y Notas (Issue #3)
- Dominio y Casos de Uso para la gestión de proyectos de cosplay (`CosplayProject`).
- Mapeo de persistencia JPA y migración Flyway para cosplays y notas.
- API REST `/api/v1/cosplays`:
  - `POST /api/v1/cosplays` (Crear proyecto/idea).
  - `GET /api/v1/cosplays` (Listar cosplays del usuario).
  - `PUT /api/v1/cosplays/{id}` (Actualizar estado/detalles).
  - `POST /api/v1/cosplays/{id}/notes` (Añadir notas).
- Tests de API con MockMvc.

---

## 👥 Fase 4: Módulo de Participaciones y Grupos (Issue #4)
- Dominio para `Participation` (Tipos: `INDIVIDUAL`, `DUO`, `GROUP`).
- Lógica de asignación de miembros (`Participant`), roles (`LEADER`, `MEMBER`) y personajes (`CharacterAssignment`).
- API REST `/api/v1/participations`:
  - `POST /api/v1/participations` (Crear participación en evento).
  - `POST /api/v1/participations/{id}/members` (Añadir participante).
  - `PUT /api/v1/participations/{id}/characters` (Asignar personajes).
- Invariantes de negocio validados en el dominio (ej: regla de 1 líder por grupo).

---

## 📸 Fase 5: Módulo de Galerías de Fotos y Control de Acceso (Issue #5)
- Puerto de salida de almacenamiento de fotografías (`PhotoStoragePort`) y adaptador local/filesystem.
- Dominio de control de acceso a fotos por miembros de la participación.
- API REST `/api/v1/participations/{id}/photos`:
  - `POST /api/v1/participations/{id}/photos` (Subir foto).
  - `GET /api/v1/participations/{id}/photos` (Consultar fotos de la participación).
- Validación de permisos del usuario autenticado sobre la participación.

---

## ⚡ Fase 6: Eventos Asíncronos con Apache Kafka (Issue #6)
- Integración de Spring Kafka y configuración de Brokers/Topics.
- Publicación de eventos de dominio (`ParticipationCreated`, `PhotoAddedToParticipation`).
- Implementación de consumidores, Dead Letter Topics (DLT) e idempotencia.
- Tests de integración de Kafka con **Testcontainers Kafka**.

---

## 📱 Fase 7: Integración del Cliente Móvil Android (Issue #7)
- Refactorización de la app Android para consumir la API REST del backend (`Retrofit` / `Ktor`).
- Eliminación de Room como almacenamiento principal y sustitución por API REST.
- Pantallas de UI Compose para el flujo completo del MVP (Descubrir -> Crear -> Coordinar Grupo -> Ver Fotos).

---

## 🚀 Fase 8: Contenerización, CI/CD y Preparación Kubernetes (Issue #8)
- Creación de `Dockerfile` multi-stage para el backend Java.
- Configuración de `docker-compose.yml` completo (Backend + PostgreSQL + Kafka).
- Pipeline CI/CD en GitHub Actions (Build, Test, Testcontainers, Lint, Docker Push).
- Manifiestos de Kubernetes (Deployments, Services, ConfigMaps, Secrets, Probes).
- Configuración de Actuator + Prometheus + Grafana para observabilidad.
