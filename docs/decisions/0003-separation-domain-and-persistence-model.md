# ADR 0003: Separación entre Modelo de Dominio y Modelo de Persistencia (JPA)

- **Fecha:** 2025-02-18
- **Estado:** Aprobado
- **Autores:** Equipo de Arquitectura de Cosplay Journal

---

## 1. Contexto

Al incorporar la persistencia en PostgreSQL mediante Spring Data JPA e Hibernate (Issue #4), se presentó la decisión de diseño entre:
- **Anotar directamente el modelo de dominio** con anotaciones de JPA (`@Entity`, `@Table`, `@Column`, `@OneToMany`).
- **Mantener entidades JPA separadas** en la capa de infraestructura y utilizar mappers de conversión bidireccional (`Domain <-> JPA Entity`).

---

## 2. Alternativas Consideradas

### Opción A: Anotar el modelo de dominio con JPA (Rechazada)
- **Ventajas:** Menor cantidad de código (no se requieren mappers de infraestructura ni clases reduplicadas).
- **Desventajas:**
  - Viola la **Regla Fundamental de la Arquitectura Hexagonal**: el dominio pasa a depender directamente de `jakarta.persistence`, Hibernate y proxies de ORM.
  - Exige constructores sin argumentos por defecto en las entidades de dominio, rompiendo el encapsulamiento y las invariantes del dominio.
  - Imposibilita utilizar `records` inmutables de Java 21 o Value Objects enriquecidos en las columnas de forma natural.
  - Acopla las reglas de negocio a las limitaciones físicas de la base de datos relacional.

### Opción B: Entidades JPA separadas en Infraestructura + Mappers (Seleccionada)
- **Ventajas:**
  - El Dominio permanece **100% Java puro**, ejecutable y testeable en milisegundos sin contextos de Spring ni base de datos.
  - Encapsulamiento estricto: las invariantes de negocio no pueden ser eludidas por proxies de Hibernate o reflexiones ORM.
  - Libertad para modificar el esquema físico de PostgreSQL o la librería de ORM en el futuro sin alterar la lógica de negocio.
  - Respeto absoluto a los límites de los **Aggregate Roots** y sus referencias cruzadas por Value Objects ID (`EventId`, `Long cosplayId`, `ParticipationId`).

---

## 3. Decisión Aprobada

Se aprueba la **Opción B**:
1. El paquete `com.cosplayjournal.domain` continuará siendo **100% agnóstico de frameworks**.
2. La infraestructura mantendrá entidades JPA dedicadas en `com.cosplayjournal.infrastructure.adapter.out.persistence.entity`.
3. Mappers dedicados (`CosplayPersistenceMapper`, `EventPersistenceMapper`, etc.) traducirán entre la capa de aplicación/dominio y los repositorios de Spring Data JPA.
4. Las pruebas unitarias de dominio no requerirán contenedores ni levantar contexto de Spring Boot.

---

## 4. Consecuencias

### Positivas
- Testabilidad inmediata del dominio (<10ms por suite).
- Mantenibilidad y cumplimiento estricto de la Arquitectura Hexagonal.
- Protección total de la lógica de negocio.

### Desafíos
- Código adicional de mapeo entre capas de persistencia y dominio.
