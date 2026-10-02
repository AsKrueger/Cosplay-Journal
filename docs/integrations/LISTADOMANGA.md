# Integración de Eventos Externos y Sincronización: ListadoManga — Cosplay Journal

## 1. Visión General

**ListadoManga** (`https://www.listadomanga.es/salones.php`) proporciona el listado de referencia de convenciones, salones de manga y eventos de cosplay en España.

Esta integración permite importar y sincronizar automáticamente eventos reales hacia la base de datos de **Cosplay Journal** sin acoplar el dominio ni los casos de uso a la estructura HTML del sitio web.

---

## 2. Arquitectura de Integración Hexagonal

```text
               ADMIN REST REQUEST
            POST /api/v1/admin/events/import
                         │
                         ▼
             ImportExternalEventsUseCase
                         │
                         ▼
              ExternalEventSourcePort
                         │
                         ▼
          ListadoMangaEventSourceAdapter
                  (Jsoup HTML)
                         │
                         ▼
                 ExternalEventData
                         │
                         ▼
              ImportExternalEventsService
                   (Sincronización)
                         │
          ┌──────────────┼──────────────┐
          ▼              ▼              ▼
       CREATE         UPDATE          SKIP
          │              │              │
          ▼              ▼              ▼
     EventCreated   EventUpdated     No-op
          │              │
          └──────┬───────┘
                 ▼
      PostgreSQL + Kafka Topic
```

---

## 3. Clave Externa, Idempotencia y Estrategia de Sincronización

Cada evento importado se almacena con el valor `source = 'LISTADOMANGA'` y un identificador externo estable `external_id`:
- Restricción de unicidad relacional `UNIQUE (source, external_id)` en la tabla `event`.
- **Reglas de Sincronización (CREATE / UPDATE / SKIP / FAIL):**
  - **`CREATE`:** Si no existe un evento con la combinación `(LISTADOMANGA, externalId)`, se crea y se emite `EventCreatedEvent`.
  - **`UPDATE`:** Si el evento ya existe, se invoca `event.updateExternalDetails(...)`. Si algún atributo sincronizable (`name`, `description`, `startDate`, `endDate`, `location`, `website`) difiere de la fuente externa, se actualiza en base de datos y se emite `EventUpdatedEvent`.
  - **`SKIP`:** Si el evento ya existe y todos los datos sincronizables son idénticos, no se realizan escrituras SQL ni se emiten mensajes Kafka.
  - **`FAIL`:** Si una fila HTML externa presenta errores irrecuperables, se contabiliza como fallida sin cancelar el resto de la importación.

---

## 4. Endpoint Administrativo

- **Ruta:** `POST /api/v1/admin/events/import`
- **Permisos:** Requiere rol `ROLE_ADMIN` (`@PreAuthorize("hasRole('ADMIN')")`).
- **Respuesta:**
```json
{
  "totalFound": 30,
  "created": 5,
  "updated": 8,
  "skipped": 16,
  "failed": 1
}
```

---

## 5. Configuración

| Propiedad / Variable de Entorno | Valor por Defecto | Descripción |
|---|---|---|
| `integrations.listadomanga.url` | `https://www.listadomanga.es/salones.php` | URL de la fuente de salones. |
| `integrations.listadomanga.connect-timeout` | `5000` | Timeout de conexión HTTP en ms. |
