# Roadmap Siguiente — Cosplay Journal

Plan de trabajo a partir del estado del 28/10/2026. Complementa a [`ROADMAP.md`](../ROADMAP.md), que recoge las fases históricas, y usa las issues de GitHub como fuente vigente de tareas.

Cada fase depende de la anterior salvo que se indique lo contrario. Las decisiones pendientes están marcadas con **[Decisión]**.

---

## Estado de partida

**Hecho:**
- Limpieza del repositorio: artefactos de build, estado de IDE y `local.properties` fuera del índice (PR #65, fusionada en `develop`).
- Perfil `prod` sin secretos por defecto.
- Guía de despliegue de coste cero con cuotas verificadas.
- CI ampliado a `develop`, `chore/**` y `docs/**` (pendiente de PR).

**Pendiente de integrar:**
- Rama `docs/arquitectura-coste-cero`: guía, ADR 0015, README y CI. Sin commit.

---

## Fase 0 — Cerrar el trabajo en curso

Tareas inmediatas, sin desarrollo nuevo.

1. Commit y PR de la rama `docs/arquitectura-coste-cero`, con la guía, la ADR 0015 y los cambios de CI y documentación. Referencia: #60.
2. Commit y PR de las issues #67, #68 y #69 (CI en `develop`, documentación de Gradle y `docker-compose`).
3. Confirmar en GitHub que una PR hacia `develop` dispara `Cosplay Journal Backend CI`.
4. Ejecutar `mvn verify` con Docker activo para ejecutar los 3 tests de Testcontainers que hoy se omiten.
5. Ejecutar `docker compose config` en `backend/` con Docker activo.
6. Cerrar #60 cuando la PR esté fusionada, con documentación y asignación.

**[Decisión] Kafka en producción:** aprobar o rechazar la ADR 0015. Condiciona la Fase 3.

---

## Fase 1 — Cliente Android con la API REST

Es la siguiente funcionalidad de producto. La app todavía usa Room como almacenamiento principal.

1. Añadir Retrofit y los modelos de la API, según `docs/api/API.md`.
2. Sustituir los repositorios basados en Room por llamadas a la API, manteniendo las pantallas actuales.
3. Gestionar el token JWT en almacenamiento seguro.
4. Eliminar Room cuando las pantallas funcionen contra la API.
5. Pruebas de los repositorios con un servidor simulado.

**[Decisión] Numeración:** el README llama "Issue #17" a esta fase, pero en GitHub el #17 es la auditoría de despliegue, ya cerrada. Conviene asignarle un número propio antes de crear su issue.

---

## Fase 2 — Almacenamiento real de fotografías

Hoy `PhotoStoragePort` tiene un adaptador de sistema de archivos local. Un contenedor PaaS pierde esos ficheros en cada redeploy.

1. **[Decisión]** Elegir proveedor: Cloudinary (plan gratuito con créditos mensuales, ver guía) o almacenamiento compatible con S3.
2. Implementar un adaptador nuevo para `PhotoStoragePort`, sin cambiar el dominio.
3. Migrar los registros de fotos existentes, si los hay.
4. Eliminar el adaptador local del perfil `prod`.

---

## Fase 3 — Mensajería de eventos en producción

Depende de la decisión de la ADR 0015.

1. Si se aprueba **Upstash**: configurar `KafkaDomainEventPublisherAdapter` para el perfil `prod`, definir la política ante superación de cuota (10.000 mensajes/día) y probar el consumo real.
2. Si se aprueba **in-process**: implementar `SpringApplicationEventPublisherAdapter`, mantener los tests de Kafka con Testcontainers y documentar la pérdida de garantías.
3. En ambos casos, actualizar `docs/messaging/KAFKA.md`.

---

## Fase 4 — Despliegue continuo

1. Añadir al CI el job `docker-build` que construye `backend/Dockerfile`.
2. Verificar que `/api/v1/health` responde en el contenedor, ya que el `HEALTHCHECK` depende de ese endpoint.
3. Desplegar en un PaaS de coste cero según la guía. Requiere verificar antes la RAM de Render (sin confirmar) y el egreso de Supabase.
4. Configurar `JWT_SECRET` y `POSTGRES_PASSWORD` como secretos del proveedor. Sin ellos el perfil `prod` no arranca, que es el comportamiento buscado.
5. Programar copias externas de PostgreSQL (`pg_dump`), porque el plan gratuito de Neon no las incluye.
6. **[Decisión]** Elegir si el job de despliegue corre en cada push a `main` o solo bajo demanda.

---

## Fase 5 — Calidad y mantenimiento

Mejoras que no bloquean el producto, por orden de impacto.

1. Añadir el job `lint` que `docs/ARCHITECTURE.md` describe como pendiente (análisis estático y aislamiento hexagonal).
2. Revisar las dependencias: actualizar Spring Boot y las librerías de seguridad, y valorar Dependabot.
3. Revisar la seguridad de la API: CORS, límites de tamaño de subida y mensajes de error sin detalles internos. La issue #17 ya lista estos puntos.
4. Unificar la documentación de fases y de issues en una sola fuente, para evitar la numeración duplicada.
5. Completar `backend/README.md` con los comandos de desarrollo actuales (Maven, Docker Compose, perfiles).
6. Valorar la política de ramas (`develop`, `main`, `feature/**`, `chore/**`, `docs/**`) y documentarla en `docs/ci/CI_CD.md`.

---

## Decisiones pendientes

| Decisión | Afecta a | Opciones |
|---|---|---|
| Kafka en producción (ADR 0015) | Fase 3 | Upstash o in-process |
| Almacenamiento de fotos | Fase 2 | Cloudinary o S3 compatible |
| Número de la issue de Android | Fase 1 | Asignar número propio |
| Disparador del despliegue | Fase 4 | Cada push a `main` o bajo demanda |

---

## Riesgos conocidos

- **Pérdida de datos en PaaS:** el sistema de archivos es efímero y el plan gratuito de Neon no incluye copias diarias. Mitigación: Fase 2 y el `pg_dump` de la Fase 4.
- **Suspensión del backend:** el scheduler de sincronización no se ejecuta con el contenedor suspendido. Mitigación: sincronización bajo demanda con `POST /api/v1/admin/events/import`, según la guía.
- **Cuotas gratuitas:** superar 10.000 mensajes diarios de Upstash o 0,5 GB en Neon causa fallos o suspensiones. Hay que monitorizarlo.
- **Tests sin Docker:** los tests de Testcontainers no se han ejecutado en el equipo de verificación. Mitigación: Fase 0, paso 4.
