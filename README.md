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

- **Backend:** Java 21 / Spring Boot 3 ([backend/](file:///C:/Users/lovei/Documents/XD/Cosplay-Journal/backend))
- **Arquitectura:** Hexagonal (Dominio desacoplado de infraestructura)
- **Base de Datos:** PostgreSQL 16 + Flyway ([docs/database/DATABASE.md](file:///C:/Users/lovei/Documents/XD/Cosplay-Journal/docs/database/DATABASE.md))
- **Mensajería Asíncrona:** Apache Kafka ([docs/messaging/KAFKA.md](file:///C:/Users/lovei/Documents/XD/Cosplay-Journal/docs/messaging/KAFKA.md))
- **Seguridad y Autorización:** Spring Security + JWT + Resource Ownership ([docs/security/SECURITY.md](file:///C:/Users/lovei/Documents/XD/Cosplay-Journal/docs/security/SECURITY.md) / [docs/security/AUTHORIZATION.md](file:///C:/Users/lovei/Documents/XD/Cosplay-Journal/docs/security/AUTHORIZATION.md))
- **Integraciones Externas:** Scraper ListadoManga + Jsoup + Sincronización Idempotente ([docs/integrations/LISTADOMANGA.md](file:///C:/Users/lovei/Documents/XD/Cosplay-Journal/docs/integrations/LISTADOMANGA.md))
- **API REST:** Especificación OpenAPI 3.0 / `/api/v1/`
- **Cliente Móvil:** Android (Jetpack Compose, Material 3, Coroutines)
- **DevOps & Infraestructura:** Docker, Docker Compose, Kubernetes, GitHub Actions
- **Testing:** JUnit 5, Mockito, Testcontainers

---

## 📚 Documentación Técnica y Decisiones

La documentación detallada del proyecto se encuentra en el directorio [`docs/`](file:///C:/Users/lovei/Documents/XD/Cosplay-Journal/docs):

- 📄 [**HOJA DE RUTA (ROADMAP)**](file:///C:/Users/lovei/Documents/XD/Cosplay-Journal/ROADMAP.md) — Planificación por fases del desarrollo.
- 🎯 [**Visión del Producto**](file:///C:/Users/lovei/Documents/XD/Cosplay-Journal/docs/PRODUCT_VISION.md) — Objetivos, propuesta de valor y usuarios objetivo.
- 📌 [**Alcance del MVP**](file:///C:/Users/lovei/Documents/XD/Cosplay-Journal/docs/MVP_SCOPE.md) — Flujo completo del MVP y criterios de aceptación.
- 🏛️ [**Dirección Técnica y Arquitectura**](file:///C:/Users/lovei/Documents/XD/Cosplay-Journal/docs/ARCHITECTURE.md) — Reglas de dependencia, Hexagonal, Kafka, K8s, testing y persistencia.
- 🧩 [**Modelo de Dominio**](file:///C:/Users/lovei/Documents/XD/Cosplay-Journal/docs/DOMAIN_MODEL.md) — Bounded Contexts, Entidades, Value Objects e invariantes de negocio.
- ⚡ [**Mensajería y Eventos Kafka**](file:///C:/Users/lovei/Documents/XD/Cosplay-Journal/docs/messaging/KAFKA.md) — Topics, DTOs de mensajería, claves y consumidores asíncronos.
- ⚙️ [**Casos de Uso e Integración API**](file:///C:/Users/lovei/Documents/XD/Cosplay-Journal/docs/use-cases/USE_CASES.md) — Servicios de aplicación, puertos de entrada/salida y endpoints REST.
- 🔒 [**Seguridad y Autenticación**](file:///C:/Users/lovei/Documents/XD/Cosplay-Journal/docs/security/SECURITY.md) — Modelo de tokens JWT, BCrypt, roles y filtros.
- 🛡️ [**Propiedad de Recursos (Ownership)**](file:///C:/Users/lovei/Documents/XD/Cosplay-Journal/docs/security/AUTHORIZATION.md) — Abstracción `CurrentUserPort`, matriz de permisos y respuestas 403 Forbidden.
- 🌐 [**Integración Externa ListadoManga**](file:///C:/Users/lovei/Documents/XD/Cosplay-Journal/docs/integrations/LISTADOMANGA.md) — Scraper Jsoup, clave externa, sincronización e idempotencia.
- 🗄️ [**Esquema de Base de Datos**](file:///C:/Users/lovei/Documents/XD/Cosplay-Journal/docs/database/DATABASE.md) — Tablas, índices, claves y Docker Compose local.
- 📊 [**Auditoría y Decisiones (ADR)**](file:///C:/Users/lovei/Documents/XD/Cosplay-Journal/docs/AUDIT_AND_DECISIONS.md) — Auditoría del estado actual y decisiones técnicas ([ADR 0001](file:///C:/Users/lovei/Documents/XD/Cosplay-Journal/docs/decisions/0001-architecture-and-product-redefinition.md) / [ADR 0002](file:///C:/Users/lovei/Documents/XD/Cosplay-Journal/docs/decisions/0002-aggregate-boundaries.md) / [ADR 0003](file:///C:/Users/lovei/Documents/XD/Cosplay-Journal/docs/decisions/0003-separation-domain-and-persistence-model.md) / [ADR 0004](file:///C:/Users/lovei/Documents/XD/Cosplay-Journal/docs/decisions/0004-authentication-and-authorization.md) / [ADR 0005](file:///C:/Users/lovei/Documents/XD/Cosplay-Journal/docs/decisions/0005-resource-ownership-and-authorization.md) / [ADR 0006](file:///C:/Users/lovei/Documents/XD/Cosplay-Journal/docs/decisions/0006-external-event-source.md) / [ADR 0007](file:///C:/Users/lovei/Documents/XD/Cosplay-Journal/docs/decisions/0007-event-search-and-pagination.md) / [ADR 0008](file:///C:/Users/lovei/Documents/XD/Cosplay-Journal/docs/decisions/0008-external-event-synchronization.md)).
- 📋 [**Próximas Issues**](file:///C:/Users/lovei/Documents/XD/Cosplay-Journal/docs/NEXT_ISSUES.md) — Backlog detallado de tareas de implementación.

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
- 🚀 **Próxima:** **Issue #12 (Integración del Cliente Móvil Android con la API REST)**.

---
Desarrollado con ❤️ para la comunidad de cosplay.
