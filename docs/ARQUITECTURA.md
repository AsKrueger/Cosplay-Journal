# Arquitectura v2

```text
 listadomanga.es ──(cada día)──▶ GitHub Actions: scraper/ (Python)
                                         │ upsert con service key
                                         ▼
 ┌──────────────────────── Supabase (plan gratuito) ────────────────────────┐
 │ Auth (email + contraseña)                                                 │
 │ Postgres + Row Level Security: profiles, events, attendance, favorites,   │
 │   friendships, blocks, reports, conversations, messages, showcase, ads,   │
 │   feedback                                                                │
 │ Realtime: mensajes nuevos de chat                                         │
 │ Storage: avatars/, showcase/, chat/ (imágenes comprimidas a ≤ 300 KB)     │
 └───────────────────────────────────────────────────────────────────────────┘
                                         ▲
                                         │ supabase-kt
                         App Android (Kotlin + Jetpack Compose)
                         Room (caché de eventos, galería local, etiquetas)
                         Coil (carteles con caché en disco)
                         WorkManager (recordatorios)
```

## Por qué así

| Decisión | Motivo |
|---|---|
| Supabase en lugar del backend Spring Boot de la v1 | Auth, base de datos, tiempo real y archivos en un único plan gratuito sin servidor que mantener. La v1 necesitaba Render + Neon + Kafka y aun así se dormía o no tenía copias |
| Seguridad con RLS | Las reglas "solo amigos ven tu perfil" y "solo miembros leen el chat" se cumplen aunque alguien use la API directamente |
| Scraper en GitHub Actions | Cron gratuito; además, la escritura diaria mantiene activo el proyecto de Supabase (el plan gratuito se pausa tras 7 días sin actividad) |
| Galería local (Room + MediaStore) | Las fotos no consumen los 1 GB de Storage; solo se sube lo que se comparte |
| Ubicación por provincia | Los 2 primeros dígitos del código postal dan la provincia; con su centroide calculamos "más cercanos" sin APIs de pago |

## Límites del plan gratuito y cómo los respetamos

| Recurso | Límite | Medida |
|---|---|---|
| Base de datos | 500 MB | Texto y metadatos solamente; eventos con más de 2 años se borran |
| Storage | 1 GB | Imágenes en WebP ≤ 1280 px y ≤ 300 KB; máximo 12 fotos de escaparate por usuario |
| Egreso | 5 GB/mes | Coil cachea en disco; las miniaturas se piden con transformación de tamaño |
| Pausa por inactividad | 7 días | El scraper diario escribe en la base |

## Módulos del repositorio

| Carpeta | Contenido |
|---|---|
| `app/` | App Android |
| `supabase/migrations/` | Esquema SQL, políticas RLS y funciones |
| `supabase/tests/` | Tests de las políticas en un Postgres local con Docker |
| `scraper/` | Importador de eventos (Python) y sus tests |
| `.github/workflows/` | CI de app, base de datos y scraper; cron del importador |

## Modelo de datos

- `profiles(id, nickname, avatar_path, friend_code)`: uno por usuario de Auth.
- `events(id, slug, name, start_date, end_date, venue, city, province_code, price, image_url, website, updated_at)`.
- `attendance(user_id, event_id, status)`: `GOING`, `NOT_GOING`, `WENT`, `MISSED`.
- `favorites(user_id, event_id)`.
- `friendships(requester_id, addressee_id, status)`: `PENDING` o `ACCEPTED`. La función `are_friends(a, b)` la usan las políticas.
- `blocks(blocker_id, blocked_id)` y `reports(reporter_id, reported_id, reason)`.
- `conversations(id, kind, title, event_id)`, `conversation_members`, `messages(id, conversation_id, sender_id, body, image_path, created_at)`.
- `showcase_photos(id, user_id, image_path, caption, event_id, position)`.
- `ads(id, image_url, target_url, starts_at, ends_at, active)`: post-MVP.
- `feedback(id, user_id, message, app_version, created_at)`.
