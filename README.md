# Cosplay Journal

La app para cosplayers y frikis de España: todos los salones del manga, cómic y videojuegos en un calendario que se actualiza solo, tu galería ordenada por evento y cosplay, y chats con tus amigos para organizar grupales.

- [Visión del producto](docs/VISION.md)
- [Análisis de competencia](docs/COMPETENCIA.md)
- [Arquitectura](docs/ARQUITECTURA.md)
- [Decisiones](docs/decisiones/)
- [Hoja de ruta](ROADMAP.md)
- [Puesta en marcha y publicación](docs/PUESTA_EN_MARCHA.md)

## Descargar

La app se publica gratis en [Releases](https://github.com/AsKrueger/Cosplay-Journal/releases/latest): descarga el APK en el móvil y ábrelo. La propia app avisa cuando hay una versión nueva.

## Tecnología

| Parte | Tecnología |
|---|---|
| App | Android, Kotlin, Jetpack Compose, Room, Coil, WorkManager |
| Backend | Supabase (Auth, Postgres + RLS, Realtime, Storage), plan gratuito |
| Eventos | Scraper en Python ejecutado a diario por GitHub Actions |
| Publicación | APK firmado en GitHub Releases al subir una etiqueta `v*` |

La versión anterior (backend Spring Boot) está en la etiqueta `v1-final`.

## Licencia

Ver [LICENSE](LICENSE).
