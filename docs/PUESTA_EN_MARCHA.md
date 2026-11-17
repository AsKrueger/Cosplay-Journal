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
5. **Emails (importante).** El servidor de correo que trae Supabase solo envía a los miembros de tu equipo y unos 2 correos por hora, así que con usuarios reales no llegarían ni la confirmación ni el código de «He olvidado mi contraseña». Configura un SMTP gratuito, por ejemplo [Brevo](https://www.brevo.com) (300 correos al día gratis):
   1. En Brevo: crea la cuenta, verifica tu dirección de remitente y en **SMTP & API** genera una clave SMTP.
   2. En Supabase → **Authentication → Emails → SMTP Settings**: activa *Custom SMTP* con host `smtp-relay.brevo.com`, puerto `587`, usuario = tu login de Brevo, contraseña = la clave SMTP, y como remitente tu dirección verificada y el nombre `Cosplay Journal`.
   3. En **Authentication → Rate Limits** sube *emails por hora* a 30.
   4. En **Authentication → Emails → Templates → Reset Password** pon el asunto «Tu código de Cosplay Journal» y pega `supabase/templates/recuperar.html`. La app pide el código de 6 cifras; no hacen falta enlaces ni web.
   5. Mientras no tengas SMTP, desactiva **Confirm email** en *Providers → Email* para que la gente pueda registrarse.

## 2. Secretos de GitHub (scraper diario)

En **Settings → Secrets and variables → Actions** del repositorio crea `SUPABASE_URL` y `SUPABASE_SERVICE_KEY`. Después lanza a mano el workflow **Eventos** (pestaña Actions → Eventos → Run workflow) para cargar el calendario la primera vez.

## Comentarios de los usuarios

Los comentarios enviados desde la app se guardan en la tabla privada `feedback`. Se leen en Supabase → **Table Editor → feedback**; marca `processed` cuando los hayas atendido. No se publican en GitHub porque el repositorio es público.

## Cómo se mantiene gratis y sin ordenador encendido

Todo funciona en la nube gratuita; tu PC y Docker no hacen falta para que la app esté en marcha.

| Pieza | Dónde corre | Plan gratuito |
|---|---|---|
| Base de datos, cuentas, chat, fotos | Supabase | 500 MB de base, 1 GB de archivos, 5 GB de tráfico/mes |
| Importar salones y limpiar archivos | GitHub Actions (cron diario) | Ilimitado en repositorios públicos |

Protecciones para no salir del plan gratuito:

- **Supabase pausa los proyectos gratuitos tras 7 días sin uso.** El cron diario consulta la base cada mañana, así que nunca llega a pausarse.
- **GitHub apaga los crons de repositorios sin actividad en 60 días.** El propio cron se reactiva cada día (último paso de `eventos.yml`).
- **Si ListadoManga cambia su web** y el importador encuentra menos de 30 salones, falla sin tocar el calendario y GitHub te avisa por email.
- **Almacenamiento:** las fotos se comprimen a WebP (máx. 300 KB), el escaparate admite 12 por persona y el cron borra cada día las fotos del chat con más de 90 días y los archivos que ya no usa nadie.
- **Spam:** como máximo 20 mensajes por minuto, 30 solicitudes de amistad al día, 5 comentarios al día, 10 grupos al día y 20 denuncias al día por usuario.

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
