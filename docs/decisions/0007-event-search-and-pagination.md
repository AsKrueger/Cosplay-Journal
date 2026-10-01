# ADR 0007: Consulta, Filtrado y Paginación del Catálogo de Eventos

- **Fecha:** 2025-02-18
- **Estado:** Aprobado
- **Autores:** Equipo de Arquitectura de Cosplay Journal

---

## 1. Contexto

Tras integrar la importación externa de convenciones desde ListadoManga (Issue #9), el backend disponía de un catálogo de eventos en PostgreSQL. Sin embargo, no existía un mecanismo para consultar el catálogo mediante filtros (ciudad, rango de fechas, fuente, estado, término de búsqueda) ni paginación en la API REST.

---

## 2. Alternativas Consideradas

### Opción A: Cargar todos los eventos en memoria y filtrar en la aplicación (Rechazada)
- **Desventajas:** Ineficiente. A medida que el catálogo crece, cargar toda la tabla degrada el rendimiento del servidor y el consumo de memoria.

### Opción B: Exponer objetos `Pageable` y `Specification` de Spring Data en la Capa de Aplicación (Rechazada)
- **Desventajas:** Viola la Arquitectura Hexagonal acoplando los casos de uso a librerías de Spring Data JPA.

### Opción C: DTO `EventSearchCriteria` + `PageResult<T>` + `Specification` encapsulado en Adaptador JPA (Seleccionada)
- **Ventajas:**
  - `EventSearchCriteria` y `PageResult<T>` son DTOs agnósticos en la capa de aplicación.
  - La consulta SQL filtrada y la paginación con ordenación estable (`startDate ASC, id ASC`) se ejecutan directamente en la base de datos PostgreSQL/H2 mediante `JpaSpecificationExecutor`.
  - La API REST valida parámetros de paginación (`0 <= page`, `1 <= size <= 100`) y rango de fechas (`from <= to`).

---

## 3. Decisión Aprobada

Se aprueba la **Opción C**:
1. El contrato `GET /api/v1/events` aceptará parámetros opcionales `city`, `from`, `to`, `source`, `status`, `query`, `page` y `size`.
2. El endpoint retornará una estructura `PageResult<EventResponse>`.
3. El filtrado y paginación se ejecutarán en la base de datos a través de `JpaEventRepositoryAdapter`.

---

## 4. Consecuencias

### Positivas
- Consulta de catálogo altamente eficiente y escalable.
- Integración transparente entre eventos manuales e importados desde ListadoManga.
- Dominio y aplicación 100% aislados de clases de Spring Data.
