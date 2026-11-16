# Cosplay Journal

Plataforma integral para cosplayers orientada a la planificación de proyectos, descubrimiento de eventos y coordinación de participaciones individuales, en dúo y grupales.

---

## 🚀 Visión del Producto

**Cosplay Journal** centraliza el ciclo de vida completo de un cosplayer:

1. **Descubrimiento de Eventos:** Consulta, búsqueda filtrada y paginación del catálogo de eventos de cosplay en España por fecha, ciudad y fuente.
2. **Gestión de Proyectos (Cosplans):** Control de ideas, materiales, presupuestos, estados y notas.
3. **Coordinación de Participaciones:** Organización de participaciones **Individuales**, **Dúos** y **Grupales** vinculadas a convenciones.
4. **Gestión de Grupos y Personajes:** Asignación de personajes y roles entre los miembros de un cosgroup.
5. **Galería de Fotos Compartida:** Galería restringida para compartir las fotografías de la participación entre los integrantes del grupo.

---

## 🛠️ Arquitectura y Dirección Técnica

El proyecto evoluciona hacia una arquitectura cliente-servidor basada en **Arquitectura Hexagonal (Ports & Adapters)**:

- **Backend:** Java 21 / Spring Boot 3 ([backend/](backend))
- **Arquitectura:** Hexagonal (Dominio desacoplado de infraestructura)
- **Base de Datos:** PostgreSQL 16 + Flyway ([docs/database/DATABASE.md](docs/database/DATABASE.md))
- **Mensajería Asíncrona:** Apache Kafka ([docs/messaging/KAFKA.md](docs/messaging/KAFKA.md))
- **Seguridad y Autorización:** Spring Security + JWT + Resource Ownership ([docs/security/SECURITY.md](docs/security/SECURITY.md) / [docs/security/AUTHORIZATION.md](docs/security/AUTHORIZATION.md))
- **Integraciones Externas:** Scraper ListadoManga + Jsoup + Sincronización Idempotente ([docs/integrations/LISTADOMANGA.md](docs/integrations/LISTADOMANGA.md))
- **Resiliencia & Operaciones:** Timeouts, Reintentos con Backoff Exponencial y Scheduler ([docs/operations/RESILIENCE.md](docs/operations/RESILIENCE.md) / [docs/operations/OBSERVABILITY.md](docs/operations/OBSERVABILITY.md))
- **API REST & Contrato OpenAPI:** Especificación OpenAPI 3.0 / Swagger UI / `/api/v1/` ([docs/api/API.md](docs/api/API.md))
- **CI/CD & Calidad Automatizada:** GitHub Actions, Maven Wrapper (`mvnw`), Testcontainers (PostgreSQL 16 & Kafka) ([docs/ci/CI_CD.md](docs/ci/CI_CD.md) / [docs/testing/TESTCONTAINERS.md](docs/testing/TESTCONTAINERS.md))
- **Cliente Móvil:** Android (Jetpack Compose, Material 3, Coroutines)
- **DevOps & Infraestructura:** Docker, Docker Compose, Kubernetes, GitHub Actions
- **Testing:** JUnit 5, Mockito, Testcontainers

---

## 📚 Documentación Técnica y Decisiones

La documentación detallada del proyecto se encuentra en el directorio [`docs/`](docs):

- 📄 [**HOJA DE RUTA (ROADMAP)**](ROADMAP.md) — Planificación por fases del desarrollo.
- 🎯 [**Visión del Producto**](docs/PRODUCT_VISION.md) — Objetivos, propuesta de valor y usuarios objetivo.
- 📌 [**Alcance del MVP**](docs/MVP_SCOPE.md) — Flujo completo del MVP y criterios de aceptación.
- 🏛️ [**Dirección Técnica y Arquitectura**](docs/ARCHITECTURE.md) — Reglas de dependencia, Hexagonal, Kafka, K8s, testing y persistencia.
- 🧩 [**Modelo de Dominio**](docs/DOMAIN_MODEL.md) — Bounded Contexts, Entidades, Value Objects e invariantes de negocio.
- ⚡ [**Mensajería y Eventos Kafka**](docs/messaging/KAFKA.md) — Topics, DTOs de mensajería, claves y consumidores asíncronos.
- ⚙️ [**Casos de Uso e Integración API**](docs/use-cases/USE_CASES.md) — Servicios de aplicación, puertos de entrada/salida y endpoints REST.
- 📖 [**Especificación de API REST (OpenAPI 3)**](docs/api/API.md) — Swagger UI, contratos, DTOs, esquemas JWT y códigos HTTP.
- 💶 [**Arquitectura de Despliegue de Coste Cero**](docs/deployment/COST_OPTIMIZED_ARCHITECTURE.md) — Alternativas gratuitas con cuotas verificadas, riesgos de pérdida de datos y ruta de migración.
- 🚀 [**Integración Continua (CI/CD)**](docs/ci/CI_CD.md) — Pipeline de GitHub Actions, Maven Wrapper, Testcontainers e informes.
- 🧪 [**Testing con Testcontainers**](docs/testing/TESTCONTAINERS.md) — Integración sobre contenedores efímeros de PostgreSQL 16 y Kafka.
- 🔒 [**Seguridad y Autenticación**](docs/security/SECURITY.md) — Modelo de tokens JWT, BCrypt, roles y filtros.
- 🛡️ [**Propiedad de Recursos (Ownership)**](docs/security/AUTHORIZATION.md) — Abstracción `CurrentUserPort`, matriz de permisos y respuestas 403 Forbidden.
- 🌐 [**Integración Externa ListadoManga**](docs/integrations/LISTADOMANGA.md) — Scraper Jsoup, clave externa, sincronización e idempotencia.
- 🏥 [**Resiliencia y Timeouts**](docs/operations/RESILIENCE.md) — Reintentos exponenciales, timeouts HTTP y excepciones técnicas.
- 📊 [**Observabilidad y Métricas**](docs/operations/OBSERVABILITY.md) — Métricas Micrometer, Spring Boot Actuator y salud del sistema.
- 🗄️ [**Esquema de Base de Datos**](docs/database/DATABASE.md) — Tablas, índices, claves y Docker Compose local.
- 📋 [**Auditoría y Decisiones (ADR)**](docs/AUDIT_AND_DECISIONS.md) — Decisiones técnicas ([ADR 0001](docs/decisions/0001-architecture-and-product-redefinition.md) - [ADR 0013](docs/decisions/0013-github-actions-ci-pipeline.md) - [ADR 0015](docs/decisions/0015-cost-optimized-deployment-and-kafka-in-production.md)).
- 📋 [**Próximas Issues**](docs/NEXT_ISSUES.md) — Backlog detallado de tareas de implementación.

---

## 📋 Estado Actual

- ✅ **Issue #0 (Redefinición del producto, auditoría y dirección técnica):** Completada.
- ✅ **Issue #1 (Creación del backend Java 21 con Arquitectura Hexagonal):** Completada.
- ✅ **Issue #2 (Modelado e Implementación del Dominio Inicial):** Completada.
- ✅ **Issue #3 (Definición de agregados y límites de consistencia del dominio):** Completada.
- ✅ **Issue #4 (Persistencia PostgreSQL mediante JPA/Hibernate y Flyway):** Completada.
- ✅ **Issue #5 (Implementación de casos de uso y consolidación de la capa de aplicación):** Completada.
- ✅ **Issue #6 (Integración de Apache Kafka para eventos de dominio):** Completada.
- ✅ **Issue #7 (Seguridad y autenticación mediante Spring Security + JWT):** Completada.
- ✅ **Issue #8 (Ownership y autorización de recursos):** Completada.
- ✅ **Issue #9 (Importación Real de Eventos desde ListadoManga y Scraper Service):** Completada.
- ✅ **Issue #10 (Consulta, filtrado y paginación del catálogo de eventos):** Completada.
- ✅ **Issue #11 (Sincronización y actualización de eventos externos):** Completada.
- ✅ **Issue #12 (Automatización de la sincronización y configuración operativa):** Completada.
- ✅ **Issue #13 (Resiliencia, timeouts y control de fallos de la fuente externa):** Completada.
- ✅ **Issue #14 (Contrato de API REST y documentación OpenAPI):** Completada.
- ✅ **Issue #15 (Testing de integración con Testcontainers):** Completada.
- ✅ **Issue #16 (CI/CD y calidad automatizada con GitHub Actions):** Completada.
- 🚀 **Próxima:** **Issue #17 (Integración del Cliente Móvil Android con la API REST)**.

---
Desarrollado con ❤️ para la comunidad de cosplay.
