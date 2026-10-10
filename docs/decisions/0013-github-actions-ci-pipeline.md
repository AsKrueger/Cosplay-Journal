# ADR 0013: Automatización de Integración Continua (CI) con GitHub Actions

- **Fecha:** 2025-02-18
- **Estado:** Aprobado
- **Autores:** Equipo de Arquitectura de Cosplay Journal

---

## 1. Contexto

Hasta la Issue #15, la ejecución de la suite de pruebas y la construcción del proyecto se realizaban manualmente en las máquinas de los desarrolladores. Para garantizar la calidad y reproducibilidad en un proyecto profesional, era necesario automatizar estas verificaciones mediante un pipeline de Integración Continua (CI).

---

## 2. Alternativas Consideradas

### Opción A: Jenkins / GitLab CI autogestionado (Rechazada)
- **Desventajas:** Requiere mantenimiento de infraestructura externa de servidores, agentes de compilación y gestión de credenciales.

### Opción B: Pipeline nativo de GitHub Actions con Maven Wrapper y Testcontainers (Seleccionada)
- **Ventajas:**
  - Integración nativa con el repositorio GitHub mediante `.github/workflows/backend-ci.yml`.
  - Los runners `ubuntu-latest` disponen de Docker preinstalado, permitiendo ejecutar Testcontainers (PostgreSQL 16 & Kafka) directamente en CI.
  - Emplea Java 21 LTS (`temurin`), caché de dependencias Maven y Maven Wrapper (`mvnw`).
  - Almacena informes Surefire y artefactos JAR construidos.

---

## 3. Decisión Aprobada

Se aprueba la **Opción B**:
1. Definir el workflow `.github/workflows/backend-ci.yml`.
2. Ejecutar `./mvnw clean verify -B` en cada `push` y `pull_request`.
3. Validar compilación, unit tests, pruebas de arquitectura y pruebas de integración con Testcontainers en CI.

---

## 4. Consecuencias

### Positivas
- Garantía de calidad automática en cada cambio de código.
- Validación continua de la Arquitectura Hexagonal y de la integración con PostgreSQL y Kafka.
- Artefactos compilados listos para futuros despliegues sin intervención manual.
