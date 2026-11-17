# Puesta en marcha

Pasos para dejar la app funcionando con coste cero.

## 1. Proyecto de Supabase

1. Crea una cuenta gratuita en [supabase.com](https://supabase.com) y un proyecto nuevo (región `eu-west`, Irlanda o Fráncfort).
2. En **Project Settings → API** copia:
   - `Project URL` → `SUPABASE_URL`
   - `anon public` → `SUPABASE_ANON_KEY` (va dentro de la app; es pública y la protegen las políticas RLS)
   - `service_role` → `SUPABASE_SERVICE_KEY` (**secreta**: solo para el scraper en GitHub)
3. Aplica el esquema: en **SQL Editor** ejecuta en orden los ficheros de `supabase/migrations/`, o con la CLI: `npx supabase link` + `npx supabase db push`.
4. En **Authentication → Providers → Email** deja activado email + contraseña.

## 2. Secretos de GitHub (scraper diario)

En **Settings → Secrets and variables → Actions** del repositorio crea `SUPABASE_URL` y `SUPABASE_SERVICE_KEY`. Después lanza a mano el workflow **Eventos** (pestaña Actions → Eventos → Run workflow) para cargar el calendario la primera vez.

## Comentarios de los usuarios

Los comentarios enviados desde la app se guardan en la tabla privada `feedback`. Se leen en Supabase → **Table Editor → feedback**; marca `processed` cuando los hayas atendido. No se publican en GitHub porque el repositorio es público.

## 3. App Android

Añade a `local.properties` (no se sube al repositorio):

```properties
SUPABASE_URL=https://xxxx.supabase.co
SUPABASE_ANON_KEY=eyJ...
FEEDBACK_EMAIL=tu-correo@ejemplo.com
```

## 4. Probar el esquema en local

Con Docker arrancado:

```bash
docker run -d --name cj-pg -e POSTGRES_PASSWORD=test -v "$PWD/supabase:/supabase" postgres:15-alpine
docker exec -e PGUSER=postgres cj-pg bash /supabase/tests/run.sh
```

## 5. Publicidad (opcional)

La barra superior de anuncios solo aparece si hay una fila activa en la tabla `ads`. Para publicar un anuncio, en Supabase → **Table Editor → ads → Insert row**:

| Campo | Ejemplo |
|---|---|
| `title` | Tienda de pelucas |
| `image_url` | URL pública de una imagen horizontal (proporción 6:1, p. ej. 1200×200) |
| `target_url` | Web a la que lleva al pulsar |
| `starts_at` / `ends_at` | Fechas de campaña (`ends_at` vacío = sin fin) |
| `active` | `true` |

La app consulta los anuncios al abrirse y cada 30 minutos y, si hay varios, los rota cada 20 segundos. Todos llevan la etiqueta «Publicidad». Para retirarlo, pon `active = false`.

## 6. Probar todo en local con el emulador (sin cuenta de Supabase)

Requiere Docker Desktop y el emulador de Android Studio.

```bash
npx supabase@2.40.7 start                    # levanta Supabase y aplica supabase/migrations
npx supabase@2.40.7 status -o env            # muestra ANON_KEY y SERVICE_ROLE_KEY
```

1. En `local.properties`: `SUPABASE_URL=http://10.0.2.2:54321` (el ordenador visto desde el emulador) y `SUPABASE_ANON_KEY=<ANON_KEY>`. La versión debug permite HTTP solo hacia esa dirección (`app/src/debug`).
2. Cargar el calendario: `cd scraper && SUPABASE_URL=http://127.0.0.1:54321 SUPABASE_SERVICE_KEY=<SERVICE_ROLE_KEY> python -m cosplay_scraper.main`
3. `./gradlew :app:installDebug`. En local no hace falta confirmar el email al registrarse.

Si `supabase start` se queda en «container is not ready: unhealthy» en el primer arranque en Windows, ejecuta `npx supabase@2.40.7 start --ignore-health-check`: la base termina de iniciarse sola.
