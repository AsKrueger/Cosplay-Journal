# Seguridad, Autenticación y Autorización (Spring Security + JWT) — Cosplay Journal

## 1. Arquitectura de Seguridad Hexagonal

La seguridad en **Cosplay Journal** se implementa como un **adaptador de infraestructura** (`com.cosplayjournal.infrastructure.adapter.in.rest.security` y `com.cosplayjournal.infrastructure.adapter.out.security`).

El módulo de **Dominio** (`com.cosplayjournal.domain`) y la **Capa de Aplicación** (`com.cosplayjournal.application`) permanecen **100% aislados de Spring Security, JWT y BCrypt**:
- La aplicación trabaja con el puerto abstracto [`UserRepositoryPort`](file:///C:/Users/lovei/Documents/XD/Cosplay-Journal/backend/src/main/java/com/cosplayjournal/application/port/out/UserRepositoryPort.java), [`PasswordHasherPort`](file:///C:/Users/lovei/Documents/XD/Cosplay-Journal/backend/src/main/java/com/cosplayjournal/application/port/out/PasswordHasherPort.java) y [`TokenProviderPort`](file:///C:/Users/lovei/Documents/XD/Cosplay-Journal/backend/src/main/java/com/cosplayjournal/application/port/out/TokenProviderPort.java).
- La infraestructura proporciona los adaptadores concretos:
  - [`BCryptPasswordHasherAdapter`](file:///C:/Users/lovei/Documents/XD/Cosplay-Journal/backend/src/main/java/com/cosplayjournal/infrastructure/adapter/out/security/BCryptPasswordHasherAdapter.java) (usando BCrypt con sal interna).
  - [`JwtTokenProviderAdapter`](file:///C:/Users/lovei/Documents/XD/Cosplay-Journal/backend/src/main/java/com/cosplayjournal/infrastructure/adapter/out/security/JwtTokenProviderAdapter.java) (usando JJWT 0.12.6).
  - [`JwtAuthenticationFilter`](file:///C:/Users/lovei/Documents/XD/Cosplay-Journal/backend/src/main/java/com/cosplayjournal/infrastructure/adapter/in/rest/security/JwtAuthenticationFilter.java) (filtro HTTP para extraer e inyectar credenciales Bearer).

---

## 2. Diagrama del Flujo de Autenticación HTTP

```text
HTTP Request (Header: Authorization: Bearer <jwt_token>)
                        │
                        ▼
             JwtAuthenticationFilter
            (Inspecciona y valida JWT)
                        │
                        ▼
            SecurityContextHolder
       (Setea UsernamePasswordAuthenticationToken)
                        │
                        ▼
             SpringSecurityConfig
       (Revisa autorización del endpoint)
                        │
                        ▼
            Input Port / Controller
                        │
                        ▼
            Application Service
```

---

## 3. Endpoints Públicos vs. Protegidos

### Endpoints Públicos (sin token):
- `POST /api/v1/auth/register` (Registro de nuevo usuario)
- `POST /api/v1/auth/login` (Login y obtención de JWT)
- `GET /api/v1/health` (Healthcheck público)
- `GET /actuator/health` (Salud de la aplicación y base de datos)
- `/swagger-ui/**`, `/v3/api-docs/**` (Documentación OpenAPI)

### Endpoints Protegidos (requieren `Authorization: Bearer <token>`):
- `POST /api/v1/cosplays`
- `GET /api/v1/cosplays/**`
- `POST /api/v1/events`
- `GET /api/v1/events/**`
- `POST /api/v1/participations/**`
- `POST /api/v1/photos/**`

---

## 4. Estructura del JWT (JSON Web Token)

El token emitido es stateless y firmado digitalmente mediante algoritmo HMAC-SHA256:

### Claims estándar incluidos:
```json
{
  "sub": "4c9d921b-8526-444a-a00d-5a1e8e2b861e",
  "username": "alonso_cosplayer",
  "role": "USER",
  "iat": 1759250000,
  "exp": 1759253600
}
```

---

## 5. Respuestas Estándar de Error (RFC 7807)

- **`401 Unauthorized`:** Petición enviada a un recurso protegido sin header `Authorization` o con un token JWT manipulado/expirado.
- **`403 Forbidden`:** Usuario autenticado pero sin rol suficiente para la operación.
- **`409 Conflict`:** Registro denegado por duplicidad de `username` o `email`.

---

## 6. Variables de Entorno y Configuración

| Propiedad / Variable de Entorno | Valor por Defecto Local | Descripción |
|---|---|---|
| `JWT_SECRET` | `cosplayJournalSuperSecretKeyForDevelopmentPhase2026!` | Clave secreta HMAC para firma de tokens (mínimo 256 bits). |
| `JWT_EXPIRATION` | `3600000` (1 hora) | Tiempo de validez del JWT en milisegundos. |
