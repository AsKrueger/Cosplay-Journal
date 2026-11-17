# 0001 — Reinicio v2 sobre Supabase

- **Fecha**: 2026-11-17
- **Estado**: aceptada

## Contexto

La v1 (etiqueta `v1-final`) tenía un backend Java 21 / Spring Boot con arquitectura hexagonal, PostgreSQL, Kafka y una app Android que lo consumía por REST. Funcionaba en local, pero:

- Publicarlo gratis exigía combinar varios proveedores (Render se duerme, Neon no tiene copias, Kafka sobra).
- Tiempo real para chats, almacenamiento de fotos y autenticación eran piezas a mantener por nuestra cuenta.
- El alcance se había recortado y quitado funciones del brief original (ajustes, favoritos, idioma).

## Decisión

Rehacer desde cero con:

- **Supabase** (plan gratuito) como backend: Auth, Postgres con RLS, Realtime y Storage.
- **App Android** nueva en Kotlin + Compose con `supabase-kt`.
- **Scraper en Python** ejecutado a diario por GitHub Actions.
- El código v1 se elimina de `develop`; queda accesible en la etiqueta `v1-final`.

## Consecuencias

- Coste real de 0 € y sin servidores propios.
- Las reglas de privacidad viven en SQL y se prueban con tests contra Postgres en CI.
- Dependemos de Supabase; el esquema es Postgres estándar y se puede migrar con `pg_dump`.
- Hay que crear el proyecto de Supabase y configurar secretos (ver [PUESTA_EN_MARCHA.md](../PUESTA_EN_MARCHA.md)).
