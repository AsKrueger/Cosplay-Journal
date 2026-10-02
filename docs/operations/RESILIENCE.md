# Resiliencia, Timeouts y Control de Fallos — Cosplay Journal

## 1. Visión General

**Cosplay Journal** implementa una política explícita de resiliencia para sus integraciones externas (ej. ListadoManga). La estrategia garantiza que caídas temporales, respuestas lentas o errores de red de proveedores externos no comprometan la estabilidad del backend ni degraden la salud global del sistema.

---

## 2. Política de Timeouts, Reintentos y Backoff

### Timeouts
Toda conexión HTTP hacia la fuente externa utiliza límites de tiempo independientes:
- **Connect Timeout:** `5000ms` por defecto (`integrations.listadomanga.connect-timeout`).
- **Read Timeout:** `10000ms` por defecto (`integrations.listadomanga.read-timeout`).

### Reintentos con Backoff Exponencial
- **Máximo de Reintentos:** 3 intentos por defecto (`integrations.listadomanga.retry.max-attempts`).
- **Estrategia de Backoff:** Exponencial `backoffMs * (2^(intento-1))` (ej: 1000ms ➔ 2000ms).
- **Errores Reintentables:** Errores de red temporales, timeouts de conexión/lectura, respuestas HTTP 5xx.
- **Errores No Reintentables:** Errores HTTP 4xx persistentes o errores de estructura HTML.

---

## 3. Evaluación Técnica de Circuit Breaker vs. Timeout + Retry

Se evaluó la introducción de un Circuit Breaker (ej. Resilience4j) y se concluyó:
1. **Frecuencia de Invocación:** La sincronización de ListadoManga ejecuta como una tarea en segundo plano batch cada 6 horas (o mediante activación manual por administradores).
2. **Impacto en Peticiones de Usuarios:** Las llamadas externas no bloquean peticiones HTTP de usuarios en tiempo real.
3. **Decisión:** Un mecanismo ligero de **Timeouts + Reintentos Exponenciales + Excepción de Infraestructura Aislada ([`ListadoMangaUnavailableException`](file:///C:/Users/lovei/Documents/XD/Cosplay-Journal/backend/src/main/java/com/cosplayjournal/infrastructure/adapter/out/external/listadomanga/exception/ListadoMangaUnavailableException.java))** resulta idóneo sin necesidad de añadir el sobrecoste de proxies y dependencias pesadas de Resilience4j.

---

## 4. Resiliencia del Invocador Programado (Scheduler)

Cuando una sincronización falla debido a indisponibilidad externa:
- La excepción es capturada por [`ScheduledEventSynchronizationAdapter`](file:///C:/Users/lovei/Documents/XD/Cosplay-Journal/backend/src/main/java/com/cosplayjournal/infrastructure/adapter/in/scheduler/ScheduledEventSynchronizationAdapter.java).
- Se incrementa la métrica `cosplay_journal_event_sync_failed_total`.
- El fallo queda registrado en logs estructurados con nivel `ERROR`.
- El scheduler **mantiene su estado operativo** para las siguientes ejecuciones programadas.
- El endpoint de salud `/actuator/health` **permanece UP**.
