# Visión del producto — Cosplay Journal v2

## Qué es

La app de referencia para cosplayers y frikis en España. Une en un solo sitio lo que hoy está repartido entre webs de salones, carpetas de fotos y grupos de WhatsApp:

1. **Calendario de eventos de España** que se actualiza solo, con cartel, fechas, precio y dirección.
2. **Tu agenda de asistencia**: voy, no voy, fui o no fui, y favoritos.
3. **Tu galería**, enlazada a una carpeta del móvil y ordenada por evento, cosplay y grupal.
4. **Amigos de verdad**: nadie te escribe ni ve tu perfil sin una solicitud de amistad aceptada.
5. **Chats y grupos** para organizar grupales.
6. **Escaparate**: eliges qué fotos de tu galería ven tus amigos en tu perfil.

## Para quién

Cosplayers y público de salones del manga, cómic y videojuegos en España, mayoritariamente jóvenes (14–30 años). Idiomas: español e inglés, configurable en Ajustes.

## Principios

- **Coste cero**: todo funciona dentro de planes gratuitos (ver [ARQUITECTURA.md](ARQUITECTURA.md)).
- **Privacidad primero**: el perfil, el escaparate y los chats solo son visibles entre amigos. La seguridad la impone la base de datos (Row Level Security), no la app.
- **Tus fotos son tuyas**: la galería vive en el móvil. Solo se suben las fotos que eliges para el escaparate o envías a un chat, y se comprimen antes.
- **Funciona sin conexión**: calendario y carteles en caché, con la opción de desactivarlo.
- **Nada inútil**: cada pantalla tiene un propósito claro; sin secciones vacías.

## Alcance

### MVP

| Área | Funciones |
|---|---|
| Calendario | Vista mes + lista, cartel, colores por estado, filtros (todos, favoritos, cercanos, voy, fui), búsqueda por nombre o ciudad, detalle con web y "añadir al calendario del móvil" |
| Asistencia | Voy / No voy / Fui / No fui, favoritos, recordatorio el día antes |
| Galería | Carpeta del móvil vinculada, etiquetas evento / cosplay / grupal, filtros |
| Amigos | Buscar por nickname, solicitudes, aceptar/rechazar, eliminar, **bloquear y denunciar** |
| Perfil | Nickname, avatar, escaparate de fotos, eventos a los que voy (solo amigos) |
| Chats | Privados y de grupo, texto y foto, en tiempo real |
| Ajustes | Idioma ES/EN, imágenes sin conexión on/off, borrar caché, borrar datos locales, enviar comentarios, cerrar sesión, **borrar cuenta** |

### Post-MVP

- **Barra publicitaria superior** que solo aparece cuando hay un anuncio activo, para cubrir el mantenimiento.
- Ver qué amigos van a cada evento ("3 amigos van").
- Notificaciones push de mensajes (Firebase Cloud Messaging, gratuito).

## Por qué nos elegirán

Ver [COMPETENCIA.md](COMPETENCIA.md). En resumen: somos los únicos con **todos los eventos de España actualizados automáticamente** y una red social **cerrada entre amigos**, gratis y en español.
