# ADR 0009: Automatización de la Sincronización y Configuración Operativa

- **Fecha:** 2025-02-18
- **Estado:** Aprobado
- **Autores:** Equipo de Arquitectura de Cosplay Journal

---

## 1. Contexto

Hasta la Issue #11, la sincronización de eventos de ListadoManga requería la invocación manual del endpoint administrativo `POST /api/v1/admin/events/import`. Para un entorno de producción, se requería automatizar la ejecución periódica del proceso de sincronización.

---

## 2. Alternativas Consideradas

### Opción A: Invocar el endpoint HTTP desde una tarea cron / curl externa (Rechazada)
- **Desventajas:** Genera un acoplamiento innecesario a la capa de transporte HTTP y requiere gestionar credenciales JWT administrativas para una tarea interna.

### Opción B: Scheduler como Adaptador de Entrada (`@Scheduled`) + `ImportExternalEventsUseCase` + Micrometer (Seleccionada)
- **Ventajas:**
  - `ScheduledEventSynchronizationAdapter` funciona como un Input Adapter en la capa de infraestructura.
  - Reutiliza directamente el puerto de entrada `ImportExternalEventsUseCase` sin duplicar lógica de negocio ni realizar llamadas HTTP internas.
  - La propiedad `cosplay-journal.events.sync.enabled` permite habilitar/deshabilitar el scheduler según el perfil (ej. desactivado en tests).
  - Previene ejecuciones simultáneas mediante `AtomicBoolean` e incrementa métricas de Micrometer.

---

## 3. Decisión Aprobada

Se aprueba la **Opción B**:
1. Activar `@EnableScheduling` en la aplicación de Spring Boot.
2. Crear `ScheduledEventSynchronizationAdapter` que invoca `ImportExternalEventsUseCase` bajo la expresión cron configurada (`cosplay-journal.events.sync.cron`).
3. El endpoint administrativo manual `POST /api/v1/admin/events/import` se mantiene para ejecuciones a demanda.

---

## 4. Consecuencias

### Positivas
- Automatización limpia respetando la Arquitectura Hexagonal.
- Observabilidad mejorada con métricas en Actuator.
- Totalmente configurable por variables de entorno y deshabilitado de forma transparente durante los tests unitarios e integración.
