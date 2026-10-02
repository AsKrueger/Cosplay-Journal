# ADR 0012: Estrategia de Testing de Integración con Testcontainers (PostgreSQL & Kafka)

- **Fecha:** 2025-02-18
- **Estado:** Aprobado
- **Autores:** Equipo de Arquitectura de Cosplay Journal

---

## 1. Contexto

La suite de pruebas del backend utilizaba H2 en memoria para validar la persistencia JPA y deshabilitaba Kafka en modo test. Aunque H2 permite ejecuciones instantáneas, no garantiza el comportamiento real de dialectos SQL de PostgreSQL 16 (índices, constraints relacionales, migraciones Flyway) ni la serialización/deserialización de mensajes en un broker Kafka real.

---

## 2. Alternativas Consideradas

### Opción A: Depender de bases de datos y Kafka locales preinstalados (Rechazada)
- **Desventajas:** Frágil. Falla si la máquina del desarrollador o el servidor de CI no tiene corriendo PostgreSQL en el puerto 5432 o Kafka en el 9092.

### Opción B: Testcontainers en perfil `testcontainers` manteniendo H2 para unit tests (Seleccionada)
- **Ventajas:**
  - H2 permanece activo para pruebas unitarias rápidas e instantáneas.
  - Testcontainers levanta automáticamente PostgreSQL 16 Alpine y Confluent Kafka durante los tests de integración en el perfil `testcontainers`.
  - Prueba de forma 100% fiel las migraciones Flyway V1..V5 y la mensajería asíncrona Kafka.

---

## 3. Decisión Aprobada

Se aprueba la **Opción B**:
1. Crear `AbstractTestcontainersIntegrationTest` con `@Testcontainers`.
2. Validar las migraciones Flyway e interacciones JPA contra PostgreSQL 16 real.
3. Validar publicación y consumo de eventos de dominio contra Kafka real.

---

## 4. Consecuencias

### Positivas
- Fiabilidad extrema de la suite de pruebas.
- Cero discrepancias entre entornos de test y producción.
- Aislamiento total en la capa de infraestructura.
