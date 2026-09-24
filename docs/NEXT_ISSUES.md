# Próximas Issues de Implementación — Cosplay Journal

A continuación se detallan las especificaciones técnicas de las próximas tareas listas para desarrollo tras la finalización de la **Issue #0**.

---

## 📌 Issue #1 — Creación del Proyecto Backend Java 21 con Arquitectura Hexagonal

### Objetivo
Establecer la estructura base del backend del proyecto en Java 21 y Spring Boot 3 con Arquitectura Hexagonal, configurando la jerarquía de paquetes, herramientas de construcción y tests del Dominio sin dependencias de framework.

### Requisitos Técnicos
1. Inicializar proyecto Spring Boot 3 con Java 21 en Gradle (Kotlin DSL o Groovy).
2. Definir la estructura de paquetes hexagonales (`domain`, `application`, `infrastructure`).
3. Crear el modelo de dominio puro de `Event`, `CosplayProject` y `Participation` en `domain.model`.
4. Definir las interfaces de los Puertos de Entrada (`Inbound Ports`) y Puertos de Salida (`Outbound Ports`).
5. Configurar JUnit 5, Mockito y JaCoCo.
6. Escribir tests unitarios de dominio para validar las invariantes de negocio (sin Spring).

### Definition of Done
- [ ] El backend Java 21 compila correctamente con `./gradlew build`.
- [ ] La estructura de paquetes cumple la regla de dependencia hacia el interior.
- [ ] Módulo de Dominio 100% aislado de Spring, JPA o librerías de infraestructura.
- [ ] Cobertura de tests unitarios en Dominio >= 90%.

---

## 📌 Issue #2 — Persistencia de Eventos, Importador Scraper y API REST de Eventos

### Objetivo
Desarrollar la funcionalidad completa de gestión y descubrimiento de eventos, incluyendo la persistencia en PostgreSQL mediante Flyway, el adaptador de scraping para `listadomanga.es` y la API REST `/api/v1/events`.

### Requisitos Técnicos
1. Configurar PostgreSQL y Flyway en `infrastructure.adapter.out.persistence`.
2. Crear la migración `V1__create_events_table.sql`.
3. Implementar `EventJpaEntity` y `EventRepositoryAdapter` que implemente `EventRepositoryPort`.
4. Desarrollar `EventScraperAdapter` utilizando Jsoup para extraer eventos de `listadomanga.es` con fallback al archivo local `events.json`.
5. Implementar el controlador REST `EventController` con los endpoints:
   - `GET /api/v1/events` (Parámetros: `city`, `startDate`, `endDate`, `page`, `size`).
   - `GET /api/v1/events/{id}` (Detalle del evento).
6. Configurar **Testcontainers PostgreSQL** para los tests de integración del repositorio y Flyway.
7. Añadir especificación OpenAPI 3.0 con Springdoc.

### Definition of Done
- [ ] Migración de Flyway ejecutada correctamente en PostgreSQL.
- [ ] `GET /api/v1/events` devuelve la lista paginada y permite filtrar por ubicación y rango de fechas.
- [ ] Web scraper funciona y recurre al fallback `events.json` ante errores.
- [ ] Tests de integración con Testcontainers verdes.
- [ ] Documentación Swagger UI disponible en `/swagger-ui.html`.

---

## 📌 Issue #3 — Módulo de Gestión de Proyectos de Cosplay y Notas

### Objetivo
Permitir a los usuarios crear, consultar, modificar y añadir notas a sus proyectos de cosplay (Cosplans) mediante una API REST protegida.

### Requisitos Técnicos
1. Crear migración Flyway `V2__create_cosplays_and_notes_tables.sql`.
2. Implementar los Casos de Uso:
   - `CreateCosplayUseCase`
   - `GetCosplaysByUserUseCase`
   - `UpdateCosplayStatusUseCase`
   - `AddNoteToCosplayUseCase`
3. Desarrollar `CosplayController` en `/api/v1/cosplays` con validación de DTOs (`@Valid`).
4. Mapear excepciones de dominio (`CosplayNotFoundException`, etc.) a HTTP RFC 7807 problem details mediante `@ControllerAdvice`.
5. Escribir tests de controladores con `MockMvc` y tests de integración con `Testcontainers`.

### Definition of Done
- [ ] Endpoints REST CRUD para Cosplays operativos y validados.
- [ ] Posibilidad de añadir y consultar notas por proyecto de cosplay.
- [ ] Respuestas de error estandarizadas en formato JSON Problem Details.
- [ ] Tests unitarios y de integración pasando correctamente.

---

## 📌 Issue #4 — Módulo de Participaciones y Coordinación de Grupos

### Objetivo
Implementar la creación de participaciones (Individual, Dúo, Grupal) asociadas a eventos, permitiendo la invitación de participantes y la asignación de personajes.

### Requisitos Técnicos
1. Crear migración Flyway `V3__create_participations_tables.sql`.
2. Implementar lógica de dominio para controlar los tipos `INDIVIDUAL`, `DUO` y `GROUP` y sus reglas de tamaño/roles.
3. Desarrollar endpoints REST `/api/v1/participations`:
   - `POST /api/v1/participations`
   - `POST /api/v1/participations/{id}/members`
   - `PUT /api/v1/participations/{id}/characters`
4. Validar que la regla de "mínimo 3 miembros para grupo" y "exactamente 1 líder" se cumpla en el dominio.

### Definition of Done
- [ ] Flujo completo de creación de participaciones individuales y grupales probado via REST.
- [ ] Asignación de personajes a cada miembro de la agrupación.
- [ ] Invariantes de negocio validadas mediante tests de dominio.

---

## 📌 Issue #5 — Galería de Fotos Compartidas y Control de Acceso

### Objetivo
Desarrollar la capacidad de asociar fotografías a participaciones grupales con políticas de control de acceso restringidas a los miembros del grupo.

### Requisitos Técnicos
1. Definir puerto de salida `PhotoStoragePort` y adaptador de sistema de archivos local (`LocalFileStorageAdapter`).
2. Crear migración Flyway `V4__create_photos_table.sql`.
3. Implementar casos de uso de subida y consulta de fotos verificando la pertenencia del usuario al grupo.
4. Endpoints REST `/api/v1/participations/{id}/photos` (Soporte multipart/form-data).

### Definition of Done
- [ ] Subida y recuperación de fotografías vinculadas a participaciones funcionando.
- [ ] Acceso denegado (HTTP 403) para usuarios ajenos a la participación.
