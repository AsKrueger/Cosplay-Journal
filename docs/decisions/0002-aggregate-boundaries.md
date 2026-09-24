# ADR 0002: Definición de Límites de Agregados y Separación de Photo como Agregado Independiente

- **Fecha:** 2025-02-18
- **Estado:** Aprobado
- **Autores:** Equipo de Arquitectura de Cosplay Journal

---

## 1. Contexto y Problema

Durante el diseño del dominio para el backend de Cosplay Journal (Issue #2 e Issue #3), se hizo necesario definir formalmente los **Aggregate Roots** (Raíces de Agregado) y sus límites de consistencia.

En particular, se debían resolver dos cuestiones clave:
1. **Evitar el Gran Agregado Monolítico:** Impedir que `Event`, `Participation`, `Cosplay` y `Photo` estuviesen acoplados mediante referencias a objetos Java completos en memoria.
2. **Decisión sobre `Photo`:** Determinar si `Photo` debe ser una entidad interna dentro de `Participation` o un **Aggregate Root independiente**.

---

## 2. Opciones Consideradas para `Photo`

### Opción A: `Photo` como entidad interna dentro del Agregado `Participation`
- **Ventajas:** Relación directa dentro de la misma raíz.
- **Desventajas:**
  - Cargar una `Participation` para modificar participantes cargaría forzosamente la lista completa de fotos en memoria.
  - Al añadir o eliminar fotos se mutaría el agregado `Participation`, aumentando la contención de bloqueos transaccionales.
  - Dificulta la paginación de álbumes de fotos y el control de acceso independiente a archivos multimedia.

### Opción B: `Photo` como Aggregate Root independiente (Seleccionada)
- **Ventajas:**
  - `Photo` posee su propio identificador inmutable (`PhotoId`) y referencia a `Participation` mediante `ParticipationId`.
  - La carga, paginación, subida y eliminación de fotografías se gestionan de forma atómica sin cargar ni bloquear el agregado `Participation`.
  - El ciclo de vida de almacenamiento de archivos (S3 / CDN / disco local) escala de forma independiente.
  - Mantiene la consistencia transaccional pequeña y enfocada.

---

## 3. Decisión Aprobada

1. **Agregados Definidos:**
   - **`Event` Aggregate Root:** (`EventId`, `EventDateRange`, `EventLocation`). Responsable del ciclo de vida e información del evento.
   - **`Cosplay` Aggregate Root:** (`Long id`, `CosplayStatus`). Responsable del estado e hitos del proyecto de cosplay.
   - **`Participation` Aggregate Root:** (`ParticipationId`, `EventId`, `Long cosplayId`, `List<Participant>`). Responsable de la coherencia de miembros, roles, asignación de personajes y límites por tipo (`INDIVIDUAL`, `DUO`, `GROUP`).
   - **`Photo` Aggregate Root:** (`PhotoId`, `ParticipationId`, `storageReference`, `caption`, `uploadedByUserId`, `uploadedAt`). Responsable de la metadata y gestión de imágenes asociadas a una participación.

2. **Referencias Cruzadas por Identificador:**
   - Ningún agregado contendrá referencias directas a objetos completos de otro agregado.
   - Las relaciones cruzarán límites mediante identificadores inmutables (`EventId`, `Long cosplayId`, `ParticipationId`).

3. **Límites Transaccionales en Persistencia:**
   - Cada caso de uso de aplicación o transacción de base de datos modificará exactamente **1 Aggregate Root**.
   - Las reglas que involucren múltiples agregados (ej: verificar que el evento no esté cancelado al crear una participación) se validarán mediante Servicios de Dominio o Servicios de Aplicación consultando los repositorios correspondientes por ID.

---

## 4. Consecuencias

### Positivas
- Desacoplamiento total entre agregados, facilitando el mapeo a JPA/PostgreSQL e integración asíncrona con Kafka.
- Rendimiento optimizado: no se cargan colecciones innecesarias de fotos al modificar participaciones.
- Pruebas unitarias de dominio extremadamente limpias e independientes.

### Desafíos
- La consistencia entre agregados es eventualmente consistente o se valida mediante servicios de aplicación orchestradores.
