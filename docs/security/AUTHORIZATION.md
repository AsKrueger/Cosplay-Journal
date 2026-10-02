# Autorización de Recursos y Propiedad (Ownership) — Cosplay Journal

## 1. Visión General

La autorización en **Cosplay Journal** amplía la autenticación (`SECURITY.md`) asegurando que los usuarios autenticados únicamente puedan modificar o administrar los recursos que les pertenecen o para los cuales tienen permisos explícitos.

---

## 2. Diferenciación de Responsabilidades

```text
Autenticación ➔ ¿Quién eres?              (Resuelto por JwtAuthenticationFilter)
Autorización  ➔ ¿Qué puedes hacer?        (Resuelto por SpringSecurityConfig / Roles)
Ownership     ➔ ¿Sobre qué recurso?       (Resuelto por la Capa de Aplicación / Dominio)
```

---

## 3. Modelo de Propiedad por Agregado

### A. Cosplay (`ownerId`)
- Todo `Cosplay` se crea asignando como `ownerId` la identidad autenticada (`UserId`) obtenida a través de [`CurrentUserPort`](file:///C:/Users/lovei/Documents/XD/Cosplay-Journal/backend/src/main/java/com/cosplayjournal/application/port/out/CurrentUserPort.java).
- **Regla:** Solo el propietario (`ownerId == authenticatedUserId`) o un usuario con rol `ADMIN` puede modificar el estado o detalles de un proyecto de cosplay.
- Intentos de modificación por otros usuarios retornan **`403 Forbidden`**.

### B. Participation (`creatorId` & `participants`)
- Toda `Participation` registra un `creatorId` (quien creó el grupo) y una lista de `participants`.
- **Reglas:**
  - Solo el usuario autenticado puede unirse a sí mismo o el `creatorId` / `ADMIN` puede administrar miembros.
  - Asignar personajes o retirar miembros ajenos exige ser `creatorId`, el propio participante o `ADMIN`.

### C. Photo (`uploadedByUserId`)
- Toda `Photo` registra `uploadedByUserId`.
- **Regla:** Para añadir fotografías a una participación, el usuario debe formar parte de la lista de participantes de dicha participación, ser su creador o ser `ADMIN`.

---

## 4. Abstracción Hexagonal: `CurrentUserPort`

Para evitar que la capa de aplicación y los servicios de negocio dependan de `SecurityContextHolder` o clases de Spring Security:
- **Puerto de Salida:** [`CurrentUserPort`](file:///C:/Users/lovei/Documents/XD/Cosplay-Journal/backend/src/main/java/com/cosplayjournal/application/port/out/CurrentUserPort.java) en `application.port.out`.
- **Adaptador:** [`SpringSecurityCurrentUserAdapter`](file:///C:/Users/lovei/Documents/XD/Cosplay-Journal/backend/src/main/java/com/cosplayjournal/infrastructure/adapter/out/security/SpringSecurityCurrentUserAdapter.java) en `infrastructure.adapter.out.security`.

---

## 5. Protección contra Manipulación del Client Request

La identidad del propietario (`ownerId`, `creatorId`, `uploadedByUserId`) se extrae **exclusivamente de la sesión autenticada (JWT)**.
Si un cliente envía campos `ownerId` o `creatorId` en el payload JSON de la petición HTTP, dichos campos son completamente ignorados, previniendo la elevación no autorizada de privilegios.
