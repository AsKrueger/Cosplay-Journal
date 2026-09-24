# Alcance del MVP — Cosplay Journal

## 1. Flujo Completo del MVP

El MVP de **Cosplay Journal** debe resolver de principio a fin el siguiente flujo de usuario:

```text
Descubrir evento
      ↓
Filtrar por ubicación y/o fecha
      ↓
Consultar detalle del evento
      ↓
Crear idea de cosplay (Cosplan)
      ↓
Añadir notas y detalles
      ↓
Asociar cosplay al evento
      ↓
Crear participación (Individual / Dúo / Grupal)
      ↓
Gestionar participantes
      ↓
Asignar personajes a participantes
      ↓
Añadir fotografías a la participación
      ↓
Compartir fotografías con los participantes
```

---

## 2. Alcance Funcional Detallado

### A. Módulo de Eventos
- **Consultar catálogo de eventos:** Lista paginada y ordenada de eventos de cosplay en España.
- **Detalle de evento:** Nombre, descripción, fecha de inicio, fecha de fin, ciudad/ubicación, recinto, enlace/fuente oficial.
- **Búsqueda y filtrado:**
  - Filtrado por ubicación (ciudad/comunidad).
  - Filtrado por rango de fechas (próximos, este mes, rango personalizado).
  - Combinación de filtros de fecha y ubicación.

### B. Módulo de Cosplay
- **Creación y gestión de proyectos:**
  - Crear idea de cosplay / proyecto.
  - Modificar detalles (nombre del personaje, obra/serie origen, estado: Idea, En proceso, Completado).
  - Añadir notas y observaciones asociadas.
- **Asociación a Eventos:**
  - Planificar qué cosplay se llevará a qué día/evento.

### C. Módulo de Participaciones
- **Creación de Participación:**
  - Vincular la participación a un evento concreto.
  - Seleccionar tipo: **Individual**, **Dúo** o **Grupal**.
- **Gestión de Miembros y Personajes:**
  - Añadir participantes (usuarios de la plataforma).
  - Asignar personaje/cosplay a cada participante.
  - Estado de la participación (Planificada, Confirmada, Finalizada).

### D. Módulo de Fotografías
- **Galería por Participación:**
  - Asociar fotos de la participación/sesión.
  - Consultar fotografías subidas.
- **Control de Acceso y Compartición:**
  - Permitir a los participantes de la agrupación ver y gestionar las fotos.
  - Almacenamiento desacoplado mediante puerto de salida (`PhotoStoragePort`).

---

## 3. Funcionalidades Fuera del Alcance (Out of Scope para el MVP)

Las siguientes funcionalidades quedan diferidas para fases posteriores:

- Chat y mensajería en tiempo real.
- Comentarios, me gusta (likes) y feed social interactivo.
- Sistema de seguidores y perfiles sociales públicos avanzados.
- Notificaciones push (móviles/web).
- Motor de recomendaciones personalizadas por IA.
- Marketplace o sección de compra/venta de cosplays y props.
- Integración directa para publicar en redes sociales (Instagram, TikTok).
- Editor avanzado de fotografías integrado.
- Gamificación, logros y medallas.
- Arquitectura de microservicios distribuida desde el día uno.
- Múltiples proveedores cloud simultáneos.

---

## 4. Criterios de Aceptación del MVP

1. Un usuario puede buscar y encontrar un evento en España filtrando por ciudad o rango de fechas.
2. Un usuario puede registrar una idea de cosplay con sus notas correspondientes.
3. Un usuario puede crear una participación de tipo Grupal o Dúo vinculada a un evento, invitar miembros y asignar personajes a cada uno.
4. Los miembros de la participación grupal pueden acceder a las fotos subidas a dicha participación.
5. Todas las reglas de negocio están totalmente aisladas en el Dominio backend y cubiertas por tests unitarios.
