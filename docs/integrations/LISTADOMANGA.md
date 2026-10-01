# Integración de Eventos Externos: ListadoManga — Cosplay Journal

## 1. Visión General

**ListadoManga** (`https://www.listadomanga.es/salones.php`) proporciona el listado de referencia de convenciones, salones de manga y eventos de cosplay en España.

Esta integración permite importar automáticamente o mediante ejecución administrativa eventos reales hacia la base de datos de **Cosplay Journal** sin acoplar el dominio ni los casos de uso a la estructura HTML del sitio web.

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
           (Verifica idempotencia por ID)
                         │
                         ▼
                 Event Aggregate
                         │
                         ▼
             PostgreSQL + Kafka Event
```

---

## 3. Clave Externa e Idempotencia

Cada evento importado se almacena con el valor `source = 'LISTADOMANGA'` y un identificador externo estable `external_id`:
- Si el hipervínculo del evento en ListadoManga contiene `salones.php?id=101`, el `external_id` asignado es `lm-101`.
- Si no dispone de parámetro de query, se genera un slug determinista basado en el nombre normalizado.
- **Invariante de Persistencia:** Restricción de unicidad relacional `UNIQUE (source, external_id)` en la tabla `event` (Migración Flyway `V4__add_external_event_identity.sql`).

Reejecutar la importación no genera duplicados: los eventos previamente registrados se omiten (`skipped`).

---

## 4. Endpoint Administrativo

- **Ruta:** `POST /api/v1/admin/events/import`
- **Permisos:** Requiere rol `ROLE_ADMIN` (`@PreAuthorize("hasRole('ADMIN')")`).
- **Respuesta:**
```json
{
  "totalFound": 12,
  "created": 10,
  "updated": 0,
  "skipped": 2,
  "failed": 0
}
```

---

## 5. Configuración

| Propiedad / Variable de Entorno | Valor por Defecto | Descripción |
|---|---|---|
| `integrations.listadomanga.url` | `https://www.listadomanga.es/salones.php` | URL de la fuente de salones. |
| `integrations.listadomanga.connect-timeout` | `5000` | Timeout de conexión HTTP en ms. |
