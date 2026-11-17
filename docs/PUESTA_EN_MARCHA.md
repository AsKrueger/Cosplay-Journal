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
