# Observabilidad, Métricas y Sincronización Operativa — Cosplay Journal

## 1. Visión General

**Cosplay Journal** incorpora capacidades de observabilidad mediante **Spring Boot Actuator**, **Micrometer** y logs estructurados para monitorizar la salud del backend y el rendimiento de las tareas programadas de sincronización de eventos.

---

## 2. Métricas de Sincronización Automática (Micrometer)

La tarea programada [`ScheduledEventSynchronizationAdapter`](file:///C:/Users/lovei/Documents/XD/Cosplay-Journal/backend/src/main/java/com/cosplayjournal/infrastructure/adapter/in/scheduler/ScheduledEventSynchronizationAdapter.java) registra las siguientes métricas de contador:

| Nombre de la Métrica | Tipo | Descripción |
|---|---|---|
| `cosplay_journal_event_sync_created_total` | Counter | Número total de eventos creados desde fuentes externas. |
| `cosplay_journal_event_sync_updated_total` | Counter | Número total de eventos actualizados tras cambios en origen. |
| `cosplay_journal_event_sync_skipped_total` | Counter | Número de eventos omitidos por no presentar cambios. |
| `cosplay_journal_event_sync_failed_total` | Counter | Número de errores durante la sincronización. |

---

## 3. Endpoints de Monitoreo y Salud (Spring Boot Actuator)

- **Salud del Sistema y Base de Datos:** `GET /actuator/health` (Incluye estado de PostgreSQL/H2 y espacio en disco).
- **Información del Sistema:** `GET /actuator/info`
- **Métricas:** `GET /actuator/metrics` (Soporta filtrado por nombre de métrica, ej: `/actuator/metrics/cosplay_journal_event_sync_created_total`).

---

## 4. Estrategia de Logging y Resiliencia Externa

- **Resiliencia frente a fallos externos:** Si la fuente externa ListadoManga está temporalmente inaccesible o retorna errores HTTP 5xx, el backend captura la excepción, registra un log estructurado `ERROR` e incrementa la métrica `cosplay_journal_event_sync_failed_total`. El estado de salud de la API (`/actuator/health`) **permanece UP**, previniendo falsos positivos de caída del servidor.
- **Seguridad en Logs:** Los logs nunca registran contraseñas, secretos, tokens JWT ni datos personales sensibles de los usuarios.
