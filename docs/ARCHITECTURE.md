# Dirección Técnica y Arquitectura — Cosplay Journal

## 1. Visión Arquitectónica

El backend de **Cosplay Journal** se desarrolla sobre el patrón **Arquitectura Hexagonal (Puertos y Adaptadores)** en **Java 21** y **Spring Boot 3**.

La meta fundamental es desacoplar completamente las reglas de negocio y modelos del dominio de cualquier infraestructura, framework web, ORM, base de datos o sistema de mensajería.

---

## 2. Diagrama de Capas y Regla de Dependencia

```text
                                INBOUND ADAPTERS
                 (REST Controllers, Kafka Listeners, CLI)
                                       │
                                       ▼
                                  INPUT PORTS
                             (Use Cases Interfaces)
                                       │
                                       ▼
                            APPLICATION / USE CASES
                            (Application Services)
                                       │
                                       ▼
                                 DOMAIN MODEL
                           (Entities, Value Objects)
                                       │
                                       ▼
                                 OUTPUT PORTS
                         (Repository & Gateway Interfaces)
                                       │
                ┌──────────────────────┼──────────────────────┐
                ▼                      ▼                      ▼
        PERSISTENCE ADAPTER    MESSAGING ADAPTER       EXTERNAL ADAPTER
          (JPA / Flyway /       (Kafka Producer)       (Web Scrapers /
            PostgreSQL)                                 Storage APIs)
```

### Regla Fundamental de Dependencia

**El módulo de Dominio NUNCA debe depender de:**
- Spring / Spring Boot
- JPA / Hibernate / Room
- PostgreSQL / SQLite
- Apache Kafka
- HTTP / REST Frameworks
- Docker / Kubernetes / Cloud Providers

El Dominio debe ser **Java puro**, ejecutable y totalmente testeable en milisegundos mediante JUnit 5 sin necesidad de levantar contenedores ni contexto de Spring.

---

## 3. Estructura de Módulos / Paquetes

```text
com.cosplayjournal
├── domain
│   ├── model            # Entidades puras y Objetos de Valor (Event, Cosplay, Participation, Photo)
│   ├── event            # Eventos de dominio puros (CosplayCreatedEvent, PhotoUploadedEvent, etc.)
│   ├── service          # Servicios de Dominio (ParticipationValidationDomainService)
│   └── exception        # Excepciones de negocio (EventNotFoundException, etc.)
├── application
│   ├── port
│   │   ├── in           # Puertos de Entrada (Interfaces de casos de uso)
│   │   └── out          # Puertos de Salida (EventRepositoryPort, PhotoStoragePort, etc.)
│   └── service          # Servicios de Aplicación (CosplayApplicationService)
└── infrastructure
    ├── adapter
    │   ├── in
    │   │   └── rest     # Controladores REST, DTOs de petición/respuesta, mappers web
    │   │   └── kafka    # Consumidores de eventos Kafka
    │   └── out
    │       ├── persistence # Entidades JPA, Spring Data Repositories, Mappers Dominio-JPA
    │       ├── messaging   # Kafka Producers y Mappers de Eventos
    │       └── external    # Adaptador de Web Scraping para eventos
    └── config           # Configuración de Beans de Spring, Seguridad, Kafka, Swagger
```

---

## 4. Stack Tecnológico

| Componente | Tecnología Seleccionada | Justificación |
|---|---|---|
| **Lenguaje Backend** | Java 21 LTS | Records, Pattern Matching, Virtual Threads, ecosistema maduro. |
| **Framework Base** | Spring Boot 3.x | Inyección de dependencias en infraestructura, ecosistema robusto. |
| **Base de Datos Relacional** | PostgreSQL 16 | Relaciones complejas, soporte JSONB, transacciones ACID. |
| **Migraciones de BD** | Flyway | Control de versiones evolutivo y reproducible del esquema SQL. |
| **Persistencia ORM** | JPA / Hibernate | Mapeo objeto-relacional exclusivo dentro del adaptador de persistencia. |
| **Mensajería Asíncrona** | Apache Kafka | Desacoplamiento de eventos de dominio e integración eventual. |
| **Documentación API** | OpenAPI 3.0 / Springdoc | Especificación interactiva y contratos de API REST claros. |
| **Seguridad** | Spring Security + JWT | Autenticación stateless y control de acceso RBAC / Ownership. |
| **Testing** | JUnit 5, Mockito, Testcontainers | Pruebas aisladas en dominio e integración real con PostgreSQL/Kafka. |
| **Contenedores y Orquestación** | Docker, Docker Compose, K8s | Entornos reproducibles y preparación para despliegue en nube. |
| **Observabilidad** | Actuator, Micrometer, Prometheus, Grafana | Métricas de rendimiento, health checks y trazabilidad. |

---

## 5. Agregados, Referencias Cruzadas y Límites Transaccionales

### Definición de Raíces de Agregado (Aggregate Roots):
- **`Event` Aggregate Root:** Administra las fechas, ubicación y ciclo de vida del evento.
- **`Cosplay` Aggregate Root:** Administra los estados e hitos del proyecto de cosplay.
- **`Participation` Aggregate Root:** Administra la lista de participantes, roles y asignación de personajes.
- **`Photo` Aggregate Root:** Administra las imágenes asociadas a una participación de forma independiente.

### Regla de Referencia por ID:
Ningún agregado mantiene referencias directas a instancias completas de otro agregado. Se comunican exclusivamente mediante Value Objects ID:
- `Participation` conoce `EventId` y `Long cosplayId`.
- `Photo` conoce `ParticipationId`.

### Límites Transaccionales:
Cada operación de repositorio en la capa de persistencia muta exactamente **un Aggregate Root** por transacción en PostgreSQL. La validación cruzada entre agregados se ejecuta a través de Servicios de Aplicación o Servicios de Dominio.

---

## 6. Estrategia de Incorporación de Apache Kafka

Kafka formará parte de la arquitectura del proyecto **únicamente cuando exista una necesidad real de procesamiento asíncrono o desacoplamiento**.

### Eventos de Dominio Previstos:
- `CosplayProjectCreated`
- `CosplayStatusChanged`
- `ParticipationCreated`
- `ParticipantJoined`
- `PhotoUploaded`
- `EventCreated`

### Marco de Decisión antes de usar Kafka:
Para cada caso de uso con Kafka, se justificará:
1. **Problema a resolver:** ¿Requiere procesamiento fuera de la transacción HTTP síncrona?
2. **Alternativa síncrona:** ¿Se puede ejecutar directamente en el servicio de aplicación?
3. **Manejo de errores:** Definición de Dead Letter Topics (DLT) y política de reintentos exponencial.
4. **Idempotencia:** Uso de IDs únicos de evento (`eventId`) y tablas de deduplicación en consumidores.

---

## 7. Estrategia de Evolución con Kubernetes

Kubernetes se introducirá progresivamente sin bloquear el desarrollo inicial:

```text
Fase 1: Desarrollo Local (Spring Boot + PostgreSQL embebido/local)
   ↓
Fase 2: Entorno Contenerizado (Docker + Docker Compose)
   ↓
Fase 3: Integración de Eventos (Docker Compose con Kafka + Zookeeper/Kraft)
   ↓
Fase 4: Observabilidad y Métricas (Prometheus + Grafana en Docker Compose)
   ↓
Fase 5: Despliegue en Kubernetes (Manifests / Helm: Deployments, Services, ConfigMaps, Secrets, Probes)
```

---

## 8. Estrategia de Testing

Se aplicará la pirámide de pruebas:

1. **Domain Tests (JUnit 5):**
   - Cobertura 100% de la lógica de negocio, invariantes y límites de agregados.
   - Sin Spring, sin BD, sin contexto externo.
2. **Application Tests (JUnit 5 + Mockito / Fakes):**
   - Validación de orquestación en los servicios de aplicación.
3. **Integration Tests (Spring Boot Test + Testcontainers):**
   - Pruebas reales contra PostgreSQL y Kafka en contenedores aislados.
   - Validación de consultas JPA, repositorios y Flyway.
4. **API / Contract Tests (MockMvc / RestAssured):**
   - Validación de endpoints HTTP `/api/v1/...`, códigos de respuesta, DTOs y validaciones de entrada (`@Valid`).

---

## 9. Estrategia de Persistencia y Separación de Modelos

El dominio no utilizará anotaciones `@Entity` ni `@Table`. Se mantendrá una separación estricta mediante Mappers:

```text
Domain Model  <── Mapper ──>  JPA Entity  <── Spring Data ──>  PostgreSQL Table
 (Java Record                  (Annotated
  / Class)                      Data Class)
```

Flyway gestionará los scripts de migración SQL en `src/main/resources/db/migration/V1__*.sql`.

---

## 10. Estrategia de API REST

- **Versionado:** URL base `/api/v1/`.
- **Formato:** JSON RFC 8259.
- **Validación:** Declarativa mediante `@NotNull`, `@NotBlank`, `@Size` en DTOs de entrada.
- **Manejo Global de Errores:** `@ControllerAdvice` retornando respuestas estandarizadas bajo **RFC 7807 (Problem Details)**.
- **Paginación y Filtrado:** Parámetros estándar `page`, `size`, `sort`.

---

## 11. Seguridad y Control de Acceso

- **Spring Security** gestionará autenticación stateless mediante **JWT (JSON Web Tokens)**.
- **Ownership de Recursos:** Un usuario solo puede modificar sus propios Cosplays o Participaciones salvo que sea administrador o creador del grupo.

---

## 12. CI/CD y DevOps

- **Pipeline GitHub Actions:**
  - `build`: Compilación Gradle con Java 21.
  - `test`: Ejecución de tests unitarios e integración con Testcontainers.
  - `lint/quality`: Análisis de código y cumplimiento de arquitectura.
  - `docker-build`: Creación de imagen Docker del backend.
