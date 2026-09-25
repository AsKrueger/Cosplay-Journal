# Esquema de Base de Datos y Persistencia — Cosplay Journal

## 1. Visión General

La persistencia del backend de **Cosplay Journal** utiliza **PostgreSQL 16** gestionada mediante migraciones de versión con **Flyway** e integrada vía **Spring Data JPA / Hibernate** dentro del adaptador de infraestructura (`infrastructure.adapter.out.persistence`).

La capa de dominio permanece **100% aislada de anotaciones JPA (`@Entity`, `@Table`, etc.)**, convirtiéndose hacia/desde modelos JPA mediante Mappers dedicados (`CosplayPersistenceMapper`, `EventPersistenceMapper`, `ParticipationPersistenceMapper`, `PhotoPersistenceMapper`).

---

## 2. Diagrama Entidad-Relación (ER)

```text
┌───────────────────────────┐         ┌───────────────────────────┐
│           event           │         │          cosplay          │
├───────────────────────────┤         ├───────────────────────────┤
│ id (PK, VARCHAR)          │         │ id (PK, BIGSERIAL)        │
│ name                      │         │ name                      │
│ description               │         │ description               │
│ start_date / end_date     │         │ character_name            │
│ city / venue              │         │ origin_series             │
│ source / status           │         │ status / created_at       │
└─────────────┬─────────────┘         └─────────────┬─────────────┘
              │ 1                                   │ 1
              │                                     │
              │ N                                   │ N
              └───────────────────┬─────────────────┘
                                  │
                                  ▼
                     ┌───────────────────────────┐
                     │       participation       │
                     ├───────────────────────────┤
                     │ id (PK, VARCHAR)          │
                     │ event_id (FK -> event)    │
                     │ cosplay_id (FK -> cosplay)│
                     │ type / status             │
                     │ group_name                │
                     └────────────┬──────────────┘
                                  │
                   ┌──────────────┴──────────────┐
                   │ 1                           │ 1
                   │ N                           │ N
                   ▼                             ▼
    ┌───────────────────────────┐   ┌───────────────────────────┐
    │        participant        │   │           photo           │
    ├───────────────────────────┤   ├───────────────────────────┤
    │ user_id (PK)              │   │ id (PK, VARCHAR)          │
    │ participation_id (PK, FK) │   │ participation_id (FK)     │
    │ name / role               │   │ storage_reference         │
    │ assigned_character        │   │ caption / uploaded_at     │
    └───────────────────────────┘   └───────────────────────────┘
```

---

## 3. Tablas y Estructura SQL

### A. Tabla `cosplay`
| Columna | Tipo SQL | Restricciones | Descripción |
|---|---|---|---|
| `id` | `BIGSERIAL` | `PRIMARY KEY` | Identificador secuencial del proyecto |
| `name` | `VARCHAR(100)` | `NOT NULL` | Nombre del proyecto de cosplay |
| `description` | `VARCHAR(500)` | `NULL` | Descripción detallada |
| `character_name` | `VARCHAR(100)` | `NULL` | Personaje representado |
| `origin_series` | `VARCHAR(100)` | `NULL` | Serie / Manga de origen |
| `status` | `VARCHAR(30)` | `NOT NULL` | Estado (`IDEA`, `IN_PLANNING`, `IN_PROGRESS`, `COMPLETED`, `ABANDONED`, `ARCHIVED`) |
| `created_at` | `TIMESTAMPTZ` | `NOT NULL` | Timestamp de creación |
| `updated_at` | `TIMESTAMPTZ` | `NOT NULL` | Timestamp de última modificación |

### B. Tabla `event`
| Columna | Tipo SQL | Restricciones | Descripción |
|---|---|---|---|
| `id` | `VARCHAR(100)` | `PRIMARY KEY` | ID del evento |
| `name` | `VARCHAR(150)` | `NOT NULL` | Nombre del evento |
| `start_date` | `DATE` | `NOT NULL` | Fecha de inicio |
| `end_date` | `DATE` | `NOT NULL` | Fecha de fin |
| `city` | `VARCHAR(100)` | `NOT NULL` | Ciudad del evento |
| `venue` | `VARCHAR(150)` | `NULL` | Recinto del evento |
| `source` | `VARCHAR(50)` | `NOT NULL` | Origen de la información (`LISTADOMANGA`, `MANUAL_ADMIN`, `COMMUNITY`) |
| `status` | `VARCHAR(30)` | `NOT NULL` | Estado (`SCHEDULED`, `POSTPONED`, `CANCELLED`, `COMPLETED`) |

**Restricción de Verificación:** `CHECK (start_date <= end_date)`.

### C. Tabla `participation`
| Columna | Tipo SQL | Restricciones | Descripción |
|---|---|---|---|
| `id` | `VARCHAR(100)` | `PRIMARY KEY` | ID de la participación |
| `event_id` | `VARCHAR(100)` | `FOREIGN KEY -> event(id)` | Referencia al evento |
| `cosplay_id` | `BIGINT` | `FOREIGN KEY -> cosplay(id)` | Referencia al cosplay |
| `type` | `VARCHAR(30)` | `NOT NULL` | Tipo (`INDIVIDUAL`, `DUO`, `GROUP`) |
| `status` | `VARCHAR(30)` | `NOT NULL` | Estado (`PLANNED`, `CONFIRMED`, `CANCELLED`, `FINISHED`) |

### D. Tabla `participant`
- **Clave Primaria Compuesta:** (`user_id`, `participation_id`).
- Clave foránea `participation_id` a la tabla `participation` con cascada de borrado.

### E. Tabla `photo`
- Clave primaria `id` (VARCHAR).
- Clave foránea `participation_id` a la tabla `participation`.
- `storage_reference` (VARCHAR(500) NOT NULL).

---

## 4. Ejecución Local con Docker Compose

Para levantar la base de datos PostgreSQL 16 en entorno local:

```bash
cd backend
docker-compose up -d
```

Las variables por defecto son:
- **Host:** `localhost`
- **Puerto:** `5432`
- **Base de datos:** `cosplay_db`
- **Usuario:** `cosplay_user`
- **Contraseña:** `cosplay_pass`
