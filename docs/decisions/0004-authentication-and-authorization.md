# ADR 0004: Adopción de Spring Security y Autenticación Stateless con JWT

- **Fecha:** 2025-02-18
- **Estado:** Aprobado
- **Autores:** Equipo de Arquitectura de Cosplay Journal

---

## 1. Contexto

La plataforma Cosplay Journal requiere autenticar usuarios e identificar quién ejecuta cada caso de uso para habilitar el control de acceso, participaciones grupales y autorías de proyectos y fotos.

Se debían cumplir dos restricciones principales:
1. **API Stateless:** La comunicación entre la App móvil Android y el backend debe ser sin estado.
2. **Aislamiento Hexagonal:** El módulo de dominio y la capa de aplicación no deben depender de Spring Security ni de librerías de JWT.

---

## 2. Alternativas Consideradas

### Opción A: Sesiones HTTP tradicionales con Cookies (Rechazada)
- **Desventajas:** Dificulta el consumo desde aplicaciones móviles nativas Android y requiere almacenamiento de sesión en servidor (almacenamiento distribuido/Sticky Sessions).

### Opción B: OAuth2 / OIDC con Servidor de Identidad Externo (Rechazada para MVP)
- **Desventajas:** Complejidad innecesaria en la fase inicial del proyecto portfolio.

### Opción C: Spring Security + JWT Stateless con Puertos de Aplicación (Seleccionada)
- **Ventajas:**
  - Token firmado digitalmente enviado vía header HTTP `Authorization: Bearer <token>`.
  - La aplicación trabaja contra las abstracciones `UserRepositoryPort`, `PasswordHasherPort` y `TokenProviderPort`.
  - La infraestructura encapsula BCrypt, Spring Security y la librería JJWT.
  - Testabilidad limpia del dominio y servicios de aplicación sin levantar contexto de seguridad.

---

## 3. Decisión Aprobada

Se aprueba la **Opción C**:
1. El backend utilizará **Spring Security** para interceptar peticiones HTTP en la infraestructura mediante `JwtAuthenticationFilter`.
2. Las contraseñas se almacenarán como hashes mediante **BCrypt**.
3. El token JWT contendrá la identidad del usuario (`sub`) y su rol.
4. El dominio y los servicios de aplicación recibirán únicamente el identificador de usuario (`UserId`) o modelo `User` sin conocer el mecanismo de autenticación.

---

## 4. Consecuencias

### Positivas
- Desacoplamiento total entre seguridad e infraestructura y reglas de negocio del dominio.
- Arquitectura ligera y totalmente compatible con el cliente Android nativo.
- Tests unitarios de aplicación rápidos sin dependencias de seguridad.

### Desafíos
- Los JWT no se pueden revocar de forma síncrona salvo mediante listas negras o expiraciones cortas.
