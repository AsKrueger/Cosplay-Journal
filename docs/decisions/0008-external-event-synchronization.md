# ADR 0008: Sincronización y Actualización de Eventos Externos

- **Fecha:** 2025-02-18
- **Estado:** Aprobado
- **Autores:** Equipo de Arquitectura de Cosplay Journal

---

## 1. Contexto

La Issue #9 permitió la ingesta de eventos externos desde ListadoManga ignorando duplicados (`skipped`). Sin embargo, cuando la fuente externa actualizaba fechas, lugares o descripciones de una convención previa, el catálogo interno de **Cosplay Journal** no reflejaba estos cambios.

---

## 2. Alternativas Consideradas

### Opción A: Sobrescribir siempre el evento en base de datos sin comparar (Rechazada)
- **Desventajas:** Genera escrituras SQL innecesarias en la base de datos y publica eventos Kafka falsos (`EventUpdatedEvent`) cuando los datos de la fuente no han cambiado.

### Opción B: Comparación en Dominio + `updateExternalDetails()` + `EventUpdatedEvent` (Seleccionada)
- **Ventajas:**
  - El método de dominio `event.updateExternalDetails(...)` valida las reglas del negocio y retorna `boolean` indicando si algún campo cambió realmente.
  - Si no existen cambios, el resultado es `skipped` (cero escrituras SQL y cero eventos Kafka).
  - Si existen cambios, el resultado es `updated`, se guarda la entidad en PostgreSQL y se publica `EventUpdatedEvent` a Kafka.

---

## 3. Decisión Aprobada

Se aprueba la **Opción B**:
1. La clave externa `(source, externalId)` sirve como clave de correlación.
2. Si el evento existe, se invocará `updateExternalDetails(...)`. Si algún atributo sincronizable varía (`name`, `startDate`, `endDate`, `city`, `venue`, `website`), la entidad se guardará y se emitirá `EventUpdatedEvent`.
3. `ImportEventsResult` diferenciará `created`, `updated`, `skipped` y `failed`.

---

## 4. Consecuencias

### Positivas
- Sincronización limpia, idempotente y eficiente.
- Publicación de eventos de dominio Kafka únicamente ante cambios de negocio reales.
- Dominio y aplicación 100% aislados de detalles técnicos de scraping.
