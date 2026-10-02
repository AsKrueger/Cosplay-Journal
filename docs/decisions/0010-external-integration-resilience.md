# ADR 0010: Resiliencia, Timeouts y Control de Fallos en Integraciones Externas

- **Fecha:** 2025-02-18
- **Estado:** Aprobado
- **Autores:** Equipo de Arquitectura de Cosplay Journal

---

## 1. Contexto

La integración con ListadoManga (Issues #9, #11 y #12) depende de un servicio HTTP externo. Era necesario establecer una estrategia de resiliencia frente a problemas de red, timeouts o indisponibilidad temporal del servidor remoto, evitando que dichos fallos afecten al backend.

---

## 2. Alternativas Consideradas

### Opción A: Añadir Resilience4j con Circuit Breaker (Rechazada)
- **Desventajas:** La sincronización externa no atiende peticiones síncronas de usuarios en tiempo real, sino ejecuciones batch en segundo plano cada 6 horas. Un Circuit Breaker añade complejidad técnica sin beneficios tangibles para este patrón de uso.

### Opción B: Timeouts + Reintentos Programáticos con Backoff Exponencial + `ListadoMangaUnavailableException` (Seleccionada)
- **Ventajas:**
  - Encapsula la lógica de reintentos con backoff exponencial directamente en `ListadoMangaEventSourceAdapter`.
  - Diferencia errores temporales reintentables (timeouts, 5xx) de errores permanentes (4xx).
  - Eleva `ListadoMangaUnavailableException` (excepción de infraestructura) si persisten los fallos.
  - Mantiene el dominio y los casos de uso 100% aislados.

---

## 3. Decisión Aprobada

Se aprueba la **Opción B**:
1. Configurar timeouts explícitos de conexión y lectura.
2. Reintentar hasta 3 veces con backoff exponencial `backoffMs * 2^(intento-1)`.
3. Encapsular fallos no recuperables en `ListadoMangaUnavailableException`.

---

## 4. Consecuencias

### Positivas
- Resiliencia transparente y aislada en la infraestructura.
- Cero acoplamiento de librerías externas en la capa de aplicación o dominio.
- Suite de tests 100% determinista y offline.
