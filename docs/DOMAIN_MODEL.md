# Modelo de Dominio — Cosplay Journal

El módulo de dominio de **Cosplay Journal** está implementado en Java 21 puro dentro del paquete `com.cosplayjournal.domain` de la aplicación backend, totalmente aislado de frameworks como Spring, JPA, Hibernate o motores de bases de datos.

La orquestación de este dominio hacia la API REST y la infraestructura se realiza mediante la [**Capa de Aplicación**](file:///C:/Users/lovei/Documents/XD/Cosplay-Journal/docs/use-cases/USE_CASES.md).

---

## 1. Contextos Delimitados (Bounded Contexts) y Entidades

```text
┌───────────────────────────────────────────────────────────┐
│                       EVENT CONTEXT                       │
│  [Event] ─── (EventId, EventName, EventDateRange,         │
│               EventLocation, EventSource, EventStatus)    │
└─────────────────────────────┬─────────────────────────────┘
                              │
                              ▼
┌───────────────────────────────────────────────────────────┐
│                      COSPLAY CONTEXT                      │
│  [Cosplay] ─── (id, name, description, characterName,     │
│                 originSeries, CosplayStatus)              │
└─────────────────────────────┬─────────────────────────────┘
                              │
                              ▼
┌───────────────────────────────────────────────────────────┐
│                   PARTICIPATION CONTEXT                   │
│  [Participation] ─── (ParticipationId, EventId,           │
│                       CosplayId, ParticipationType,       │
│                       ParticipationStatus, Participants)  │
└─────────────────────────────┬─────────────────────────────┘
                              │
                              ▼
┌───────────────────────────────────────────────────────────┐
│                        PHOTO CONTEXT                      │
│  [Photo] ─── (PhotoId, ParticipationId, storageReference, │
│               caption, uploadedByUserId, uploadedAt)      │
└─────────────────────────────┬─────────────────────────────┘
```

---

## 2. Detalle de Implementación por Entidades y Value Objects

### A. Contexto de Eventos (`com.cosplayjournal.domain.model.event`)

#### `Event`
- **Identificador:** `EventId` (Value Object wrapper inmutable de String).
- **Atributos:**
  - `name`: `String` (2 - 150 caracteres, obligatorio).
  - `description`: `String`.
  - `dateRange`: `EventDateRange` (Value Object inmutable: `startDate`, `endDate`).
  - `location`: `EventLocation` (Value Object inmutable: `city`, `venue`, `province`, `country`, `address`, `latitude`, `longitude`).
  - `website`: `String`.
  - `source`: `EventSource` (Enum: `LISTADOMANGA`, `MANUAL_ADMIN`, `COMMUNITY`).
  - `status`: `EventStatus` (Enum: `SCHEDULED`, `POSTPONED`, `CANCELLED`, `COMPLETED`).

#### Invariantes y Comportamiento de `Event`:
- `startDate` debe ser anterior o igual a `endDate` (`startDate <= endDate`).
- `city` es obligatorio en `EventLocation`.
- Transición protegida: No se puede completar un evento si su estado actual es `CANCELLED`.

---

### B. Contexto de Cosplay (`com.cosplayjournal.domain.model`)

#### `Cosplay`
- **Atributos:**
  - `id`: `Long`.
  - `name`: `String` (2 - 100 caracteres, obligatorio).
  - `description`: `String`.
  - `characterName`: `String`.
  - `originSeries`: `String`.
  - `status`: `CosplayStatus` (Enum: `IDEA`, `IN_PLANNING`, `IN_PROGRESS`, `COMPLETED`, `ABANDONED`, `ARCHIVED`).
  - `createdAt`: `Instant`.
  - `updatedAt`: `Instant`.

#### Matriz de Transiciones de Estado Protegidas (`changeStatus`):
- `IDEA` ➔ `IN_PLANNING`, `IN_PROGRESS`, `ABANDONED`
- `IN_PLANNING` ➔ `IN_PROGRESS`, `ABANDONED`, `IDEA`
- `IN_PROGRESS` ➔ `COMPLETED`, `ABANDONED`, `IN_PLANNING`
- `COMPLETED` ➔ `ARCHIVED`, `IN_PROGRESS`
- `ABANDONED` ➔ `IDEA`, `IN_PLANNING`
- `ARCHIVED` ➔ `COMPLETED`, `IDEA`
- Cualesquiera otras transiciones directas (ej: `COMPLETED` ➔ `IDEA`) lanzan `InvalidStateTransitionException`.

---

### C. Contexto de Participaciones (`com.cosplayjournal.domain.model.participation`)

#### `Participation`
- **Identificador:** `ParticipationId` (Value Object wrapper inmutable de String).
- **Atributos:**
  - `eventId`: `EventId` (Obligatorio, no nulo).
  - `cosplayId`: `Long` (Obligatorio, no nulo).
  - `type`: `ParticipationType` (Enum: `INDIVIDUAL`, `DUO`, `GROUP`).
  - `status`: `ParticipationStatus` (Enum: `PLANNED`, `CONFIRMED`, `CANCELLED`, `FINISHED`).
  - `groupName`: `String`.
  - `participants`: `List<Participant>`.

#### `Participant`
- `userId`: `String` (Obligatorio, no nulo).
- `name`: `String` (Obligatorio, no nulo).
- `role`: `ParticipantRole` (Enum: `LEADER`, `MEMBER`, `HELPER`).
- `assignedCharacter`: `String` (Personaje asignado en el contexto de la participación).

#### Invariantes y Reglas de Negocio en `Participation`:
- Un mismo `userId` no puede duplicarse dentro de la misma participación.
- Reglas según el tipo:
  - `INDIVIDUAL`: Máximo 1 participante. Se exige exactamente 1 participante al pasar a `CONFIRMED` o `FINISHED`.
  - `DUO`: Máximo 2 participantes. Se exigen exactamente 2 participantes al pasar a `CONFIRMED` o `FINISHED`.
  - `GROUP`: Sin límite superior. Se exigen al menos 3 participantes al pasar a `CONFIRMED` o `FINISHED`.

---

### D. Contexto de Fotografías (`com.cosplayjournal.domain.model.photo`)

#### `Photo`
- **Identificador:** `PhotoId` (Value Object wrapper inmutable de String).
- **Atributos:**
  - `participationId`: `ParticipationId` (Obligatorio, no nulo).
  - `storageReference`: `String` (Obligatorio, no nulo ni vacío).
  - `caption`: `String`.
  - `uploadedByUserId`: `String`.
  - `uploadedAt`: `Instant`.

---

## 3. Excepciones de Dominio

- `CosplayNotFoundException`: Lanzada cuando no se localiza un proyecto de cosplay por ID.
- `EventNotFoundException`: Lanzada cuando no se localiza un evento por ID.
- `ParticipationNotFoundException`: Lanzada cuando no se localiza una participación por ID.
- `PhotoNotFoundException`: Lanzada cuando no se localiza una foto por ID.
- `InvalidCosplayDataException`: Lanzada ante violaciones de validación de datos en el contexto de Cosplay.
- `InvalidStateTransitionException`: Lanzada al intentar una transición no permitida de `CosplayStatus`.
- `InvalidEventDataException`: Lanzada ante fechas o ubicaciones inconsistentes en `Event`.
- `InvalidParticipationDataException`: Lanzada ante violaciones de límites de participantes o referencias nulas en `Participation` y `Photo`.
