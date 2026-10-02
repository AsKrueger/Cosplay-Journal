# ADR 0011: Adopción de OpenAPI 3 y Swagger UI para el Contrato REST

- **Fecha:** 2025-02-18
- **Estado:** Aprobado
- **Autores:** Equipo de Arquitectura de Cosplay Journal

---

## 1. Contexto

Para que la API REST del backend de **Cosplay Journal** pueda ser consumida de forma clara por la aplicación móvil nativa Android (o clientes de terceros) sin necesidad de inspeccionar el código fuente Java, se requería una especificación técnica viva y estandarizada del contrato HTTP.

---

## 2. Alternativas Consideradas

### Opción A: Documentación manual estática en Markdown únicamente (Rechazada)
- **Desventajas:** Tiende a desincronizarse rápidamente con el código a medida que evolucionan los DTOs y endpoints.

### Opción B: Integración de `springdoc-openapi` en Infraestructura (Seleccionada)
- **Ventajas:**
  - Genera automáticamente la especificación OpenAPI 3.0 basada en las anotaciones `@Operation`, `@ApiResponse` y `@Tag` de los controladores REST.
  - Expone la interfaz interactiva **Swagger UI** en `/swagger-ui/index.html`.
  - Documenta el esquema de seguridad `bearerAuth` (JWT) y las respuestas de error RFC 7807.
  - La dependencia y las anotaciones residen **exclusivamente en la capa de infraestructura**, manteniendo `domain` y `application` 100% aislados.

---

## 3. Decisión Aprobada

Se aprueba la **Opción B**:
1. Añadir `springdoc-openapi-starter-webmvc-ui` a la capa de infraestructura del backend.
2. Configurar el esquema global `bearerAuth` en `OpenApiConfig`.
3. Documentar todos los controladores REST con anotaciones OpenAPI.
4. Crear una prueba de integración `OpenApiIntegrationTest` que valida la generación del contrato `/v3/api-docs`.

---

## 4. Consecuencias

### Positivas
- Contrato HTTP transparente, interactivo y actualizado en tiempo real.
- Preparación directa para la integración con el cliente nativo Android.
- Dominio y aplicación libres de anotaciones o dependencias de Swagger/OpenAPI.
