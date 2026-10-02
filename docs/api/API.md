# Contrato de API REST y Especificación OpenAPI — Cosplay Journal

## 1. Visión General

La API REST de **Cosplay Journal** expone los servicios necesarios para que el cliente móvil Android y otras aplicaciones puedan gestionar usuarios, cosplans, eventos, participaciones y fotos.

Toda la especificación técnica OpenAPI 3.0 está disponible en formato JSON/YAML y mediante la interfaz interactiva **Swagger UI**:
- **Swagger UI:** `http://localhost:8080/swagger-ui/index.html` (o `http://localhost:8080/swagger-ui.html`)
- **Especificación OpenAPI JSON:** `http://localhost:8080/v3/api-docs`

---

## 2. Autenticación y Autorización HTTP

La API emplea autenticación stateless con **Bearer JWT**:
- **Header:** `Authorization: Bearer <token_jwt>`
- **Obtención del token:** `POST /api/v1/auth/login`

### Matriz de Acceso por Endpoint

| Endpoint | Método | Acceso | Permisos |
|---|---|---|---|
| `/api/v1/auth/register` | `POST` | Público | Ninguno |
| `/api/v1/auth/login` | `POST` | Público | Ninguno |
| `/api/v1/health` | `GET` | Público | Ninguno |
| `/api/v1/events` | `GET` | Público | Ninguno |
| `/api/v1/cosplays` | `POST` | Autenticado | `ROLE_USER` |
| `/api/v1/cosplays/{id}/status` | `PUT` | Autenticado | Propietario o `ROLE_ADMIN` |
| `/api/v1/participations` | `POST` | Autenticado | `ROLE_USER` |
| `/api/v1/photos` | `POST` | Autenticado | Integrante de la Participación o `ROLE_ADMIN` |
| `/api/v1/admin/events/import` | `POST` | Autenticado | Requiere `ROLE_ADMIN` |

---

## 3. Estructura Estándar de Errores (RFC 7807)

Todos los errores retornan un objeto JSON consistente:
```json
{
  "status": 403,
  "error": "Forbidden",
  "message": "No dispone de permisos para realizar esta operación",
  "path": "/api/v1/cosplays/10/status",
  "timestamp": "2026-10-02T14:00:00Z"
}
```

Códigos HTTP soportados:
- `400 Bad Request`: Formato de JSON o fechas inválido, o restricción de dominio incumplida.
- `401 Unauthorized`: Token no proporcionado, malformado o expirado.
- `403 Forbidden`: Usuario no autorizado sobre el recurso (`ownerId` / `creatorId`).
- `404 Not Found`: Recurso no encontrado en el sistema.
- `409 Conflict`: Intento de registrar un `username` o `email` duplicado.
- `500 Internal Server Error`: Fallo imprevisto en el servidor.
