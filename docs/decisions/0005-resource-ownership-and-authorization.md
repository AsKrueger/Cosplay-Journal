# ADR 0005: Propiedad de Recursos (Ownership) y Autorización en la Capa de Aplicación

- **Fecha:** 2025-02-18
- **Estado:** Aprobado
- **Autores:** Equipo de Arquitectura de Cosplay Journal

---

## 1. Contexto

Tras implementar la autenticación mediante JWT (Issue #7), la API era capaz de verificar la identidad de los usuarios. Sin embargo, no existía un mecanismo para validar la propiedad de los recursos ni impedir que un usuario autenticado modificase o eliminase proyectos de cosplay, participaciones o fotos pertenecientes a otros usuarios.

---

## 2. Alternativas Consideradas

### Opción A: Autorización basada exclusivamente en anotaciones `@PreAuthorize` (Rechazada)
- **Desventajas:** Dificulta la expresión de reglas complejas que requieren consultar la base de datos para verificar el propietario del recurso (`resource.ownerId == currentUser.id`).

### Opción B: Inyectar `SecurityContextHolder` en los servicios de aplicación (Rechazada)
- **Desventajas:** Viola la Arquitectura Hexagonal al acoplar la capa de aplicación con Spring Security.

### Opción C: Abstracción `CurrentUserPort` + Validación de Ownership en Servicios de Aplicación (Seleccionada)
- **Ventajas:**
  - `CurrentUserPort` abstrae la obtención del usuario autenticado sin acoplar la aplicación a Spring Security.
  - La validación de propiedad se realiza en la capa de aplicación/dominio, devolviendo `ForbiddenAccessException` (`403 Forbidden`) ante violaciones.
  - El cliente HTTP no puede manipular el propietario del recurso.

---

## 3. Decisión Aprobada

Se aprueba la **Opción C**:
1. Los agregados `Cosplay`, `Participation` y `Photo` mantendrán el identificador de su creador/propietario (`ownerId`, `creatorId`, `uploadedByUserId`).
2. Los servicios de aplicación utilizarán `CurrentUserPort` para obtener la identidad autenticada.
3. Se aplicará validación de propiedad en las modificaciones y eliminaciones: si el usuario no es el propietario ni `ADMIN`, se lanzará `ForbiddenAccessException` retornando `403 Forbidden`.

---

## 4. Consecuencias

### Positivas
- Seguridad robusta a nivel de recurso.
- Protección total contra la manipulación de payloads JSON.
- Aislamiento arquitectónico mantenido.
