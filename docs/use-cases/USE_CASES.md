# Casos de Uso y Capa de Aplicación — Cosplay Journal

La **capa de aplicación** (`com.cosplayjournal.application`) coordina las operaciones del sistema desacoplando completamente los adaptadores REST y los adaptadores de persistencia relacional PostgreSQL del modelo de dominio.

---

## 1. Arquitectura de Flujo de Caso de Uso

```text
                     INBOUND ADAPTER (REST Controller)
                                    │
                                    ▼
                         INPUT PORT (Interface)
                                    │
                                    ▼
                 APPLICATION SERVICE (Orquestador)
                                    │
                  ┌─────────────────┴─────────────────┐
                  ▼                                   ▼
          DOMAIN AGGREGATE                    OUTPUT PORT (Interface)
                  │                                   │
                  ▼                                   ▼
            DOMAIN EVENT                   PERSISTENCE ADAPTER (JPA)
```

---

## 2. Casos de Uso Implementados por Bounded Context

### A. Contexto de Cosplay (`com.cosplayjournal.application.service.CosplayApplicationService`)

| Puertos de Entrada (`in`) | Comandos / Parámetros | Propósito | Evento de Dominio Generado |
|---|---|---|---|
| `CreateCosplayUseCase` | `CreateCosplayCommand` | Crea un nuevo proyecto de cosplay en estado `IDEA`. | `CosplayCreatedEvent` |
| `GetCosplayUseCase` | `Long id` | Recupera un proyecto de cosplay por ID o lista todos. | - |
| `ChangeCosplayStatusUseCase` | `ChangeCosplayStatusCommand` | Transiciona el estado del cosplay validando invariantes. | `CosplayStatusChangedEvent` |

**Endpoints REST Asociados:**
- `POST /api/v1/cosplays`
- `GET /api/v1/cosplays/{id}`
- `GET /api/v1/cosplays`
- `PUT /api/v1/cosplays/{id}/status`

---

### B. Contexto de Eventos (`com.cosplayjournal.application.service.EventApplicationService`)

| Puertos de Entrada (`in`) | Comandos / Parámetros | Propósito | Evento de Dominio Generado |
|---|---|---|---|
| `CreateEventUseCase` | `CreateEventCommand` | Crea un nuevo evento de cosplay. | `EventCreatedEvent` |
| `GetEventUseCase` | `EventId id` | Consulta detalle de un evento por ID o lista todos. | - |

**Endpoints REST Asociados:**
- `POST /api/v1/events`
- `GET /api/v1/events/{id}`
- `GET /api/v1/events`

---

### C. Contexto de Participaciones (`com.cosplayjournal.application.service.ParticipationApplicationService`)

| Puertos de Entrada (`in`) | Comandos / Parámetros | Propósito | Evento de Dominio Generado |
|---|---|---|---|
| `CreateParticipationUseCase` | `CreateParticipationCommand` | Crea participación (`INDIVIDUAL`, `DUO`, `GROUP`) vinculada a un evento y cosplay. | `ParticipationCreatedEvent`, `ParticipantJoinedEvent` |
| `GetParticipationUseCase` | `ParticipationId id` | Consulta detalle de participación o lista todas. | - |
| `JoinParticipationUseCase` | `JoinParticipationCommand` | Unirse a una participación grupal existente. | `ParticipantJoinedEvent` |
| `LeaveParticipationUseCase` | `LeaveParticipationCommand` | Salir de una participación grupal. | - |
| `AssignCharacterUseCase` | `AssignCharacterCommand` | Asigna o modifica el personaje de un participante. | - |

**Endpoints REST Asociados:**
- `POST /api/v1/participations`
- `GET /api/v1/participations/{id}`
- `GET /api/v1/participations`
- `POST /api/v1/participations/{id}/members`
- `DELETE /api/v1/participations/{id}/members/{userId}`
- `PUT /api/v1/participations/{id}/characters`

---

### D. Contexto de Fotografías (`com.cosplayjournal.application.service.PhotoApplicationService`)

| Puertos de Entrada (`in`) | Comandos / Parámetros | Propósito | Evento de Dominio Generado |
|---|---|---|---|
| `AddPhotoUseCase` | `AddPhotoCommand` | Asocia una fotografía a una participación existente. | `PhotoUploadedEvent` |
| `GetPhotoUseCase` | `PhotoId id` | Consulta detalle de una foto por ID. | - |
| `GetParticipationPhotosUseCase` | `ParticipationId id` | Consulta la galería de fotos de una participación. | - |

**Endpoints REST Asociados:**
- `POST /api/v1/photos`
- `GET /api/v1/photos/{id}`
- `GET /api/v1/participations/{participationId}/photos`

---

## 3. Publicación de Eventos de Dominio

La capa de aplicación utiliza la abstracción `DomainEventPublisherPort` para desacoplar los eventos de dominio de la infraestructura de mensajería:
- En la fase actual, `LoggingDomainEventPublisherAdapter` registra los eventos de dominio en logs estructurados.
- En fases posteriores (Issue #6), el adaptador de mensajería `KafkaDomainEventPublisherAdapter` publicará los eventos a tópicos de Apache Kafka sin requerir cambios en los servicios de aplicación ni en el dominio.
