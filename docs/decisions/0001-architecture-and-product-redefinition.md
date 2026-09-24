# ADR 0001: Redefinición del Producto y Adopción de Arquitectura Hexagonal en Java

- **Fecha:** 2025-02-18
- **Estado:** Aprobado
- **Autores:** Equipo de Arquitectura & Desarrollo de Cosplay Journal

---

## 1. Contexto

**Cosplay Journal** comenzó como un proyecto cliente Android local en Kotlin que almacenaba datos en SQLite (Room).
Tras analizar el potencial del producto y la necesidad de los cosplayers de coordinarse en participaciones grupales, compartir fotografías y consultar eventos actualizados de forma unificada en España, se ha determinado la necesidad de transformar la aplicación en una plataforma colaborativa cliente-servidor con backend centralizado.

---

## 2. Decisión

Se aprueba la redefinición completa de la arquitectura y alcance de Cosplay Journal bajo las siguientes pautas:

1. **Visión del Producto:** Plataforma integral para la preparación y organización de participaciones en eventos de cosplay.
2. **Backend Java 21 / Spring Boot 3:** Construcción de un nuevo backend basado en **Arquitectura Hexagonal (Ports & Adapters)**.
3. **Persistencia Relacional:** Uso de **PostgreSQL** con **Flyway** para migraciones SQL.
4. **Dominio Aislado:** El módulo de dominio no tendrá dependencias de Spring Boot, JPA, Hibernate, PostgreSQL, Kafka ni HTTP.
5. **API REST:** Exposición de endpoints bajo `/api/v1/...` documentados con OpenAPI 3.0.
6. **Evolución Asíncrona con Kafka:** Incorporación de Apache Kafka para eventos de dominio (`ParticipationCreated`, `PhotoAdded`) en fases donde esté técnicamente justificado.
7. **Evolución Infraesctructura:** Despliegue progresivo vía Docker, Docker Compose y finalmente Kubernetes.
8. **App Móvil Android:** Rediseñada como cliente / adaptador de entrada móvil que consume la API REST del backend.

---

## 3. Consecuencias

### Positivas
- Permite colaboración en tiempo real, participaciones en dúo/grupo y galerías compartidas.
- Dominio 100% testeable en milisegundos sin emuladores ni infraestructura externa.
- Mantenibilidad y desacoplamiento garantizados por Arquitectura Hexagonal.

### Riesgos y Mitigaciones
- **Riesgo:** Incremento inicial del trabajo al construir la base del backend.
- **Mitigación:** Desarrollo incremental dividido en fases claras especificadas en `ROADMAP.md` e `NEXT_ISSUES.md`.
