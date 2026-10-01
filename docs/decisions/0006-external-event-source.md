# ADR 0006: Integración de Fuente Externa de Eventos (ListadoManga + Jsoup)

- **Fecha:** 2025-02-18
- **Estado:** Aprobado
- **Autores:** Equipo de Arquitectura de Cosplay Journal

---

## 1. Contexto

Para ofrecer valor inmediato a los cosplayers, el backend de **Cosplay Journal** debe poblar su catálogo con convenciones y salones de manga reales celebrados en España. La fuente pública de referencia identificada es ListadoManga.

Se requería integrar esta fuente sin comprometer la Arquitectura Hexagonal ni acoplar el dominio a detalles de scraping HTML.

---

## 2. Alternativas Consideradas

### Opción A: Ingesta directa en el dominio mediante clases de scraping (Rechazada)
- **Desventajas:** Viola el aislamiento del dominio introduciendo dependencias de Jsoup, HTTP y parsing de cadenas HTML dentro de la capa del núcleo.

### Opción B: Adaptador de Salida + DTO Externo + Idempotencia por Clave Externa (Seleccionada)
- **Ventajas:**
  - `ListadoMangaEventSourceAdapter` encapsula la dependencia de Jsoup en `infrastructure.adapter.out.external.listadomanga`.
  - La aplicación trabaja con la interfaz abstracta `ExternalEventSourcePort` y DTOs agnósticos `ExternalEventData`.
  - Garantiza idempotencia mediante la restricción de unicidad relacional `UNIQUE (source, external_id)`.
  - Permite realizar pruebas unitarias y de integración offline usando fixtures HTML sin depender de conexión a Internet.

---

## 3. Decisión Aprobada

Se aprueba la **Opción B**:
1. Implementar `ExternalEventSourcePort` en la capa de aplicación.
2. Implementar `ListadoMangaEventSourceAdapter` usando Jsoup en infraestructura.
3. Exponer la importación mediante el endpoint administrativo `POST /api/v1/admin/events/import` protegido por `ROLE_ADMIN`.

---

## 4. Consecuencias

### Positivas
- Integración limpia con datos del mundo real.
- Pruebas deterministas y aisladas mediante fixtures HTML localizadas en `src/test/resources/fixtures/`.
- Dominio y aplicación 100% libres de dependencias de Jsoup o HTML.

### Desafíos
- Si ListadoManga modifica drásticamente su estructura HTML, solo será necesario actualizar el selector CSS dentro del adaptador de infraestructura sin tocar el dominio.
