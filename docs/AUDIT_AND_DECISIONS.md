# Auditoría del Repositorio y Decisiones de Arquitectura — Cosplay Journal

## 1. Auditoría del Estado Actual del Repositorio

Con fecha de inicio de la Issue #0, se ha realizado una auditoría exhaustiva del código fuente y recursos del proyecto **Cosplay Journal**.

### A. Estructura y Tecnologías
- **Tipo de proyecto:** Aplicación nativa Android cliente en Kotlin.
- **Librerías principales utilizadas:**
  - Jetpack Compose + Material 3 (UI)
  - Navigation Compose (Navegación)
  - Room Persistence Library v6 (`AppDatabase.kt` en SQLite local)
  - Coil (Carga de imágenes)
  - Jsoup (Web scraping cliente desde `listadomanga.es`)
  - osmdroid (Mapas OpenStreetMap)
  - iText7 (Generación de PDF en el dispositivo)
  - DataStore Preferences (Preferencias del usuario)

### B. Arquitectura y Acoplamiento
- **Estructura por capas local:**
  - `data/dao/` (`CosplayDao.kt`, `LocationDao.kt`)
  - `data/entity/` (`Cosplay`, `Cosplan`, `Location`, `WigMakeup`, `HandmadePart`, etc.)
  - `data/repository/` (`CosplayRepository`, `EventRepository`, `LocationRepository`, `SettingsRepository`)
  - `ui/screens/` y `ui/viewmodel/`
- **Problemas identificados:**
  1. **Ausencia de backend:** La aplicación es 100% cliente y almacena todo en una base de datos local SQLite (Room). Imposibilita la colaboración grupal, la compartición de fotos o la sincronización multi-dispositivo.
  2. **Acoplamiento Directo:** Las entidades de Room `@Entity` actúan simultáneamente como modelos de datos y modelos de UI. No existe capa de Dominio pura ni abstracción de puertos/adaptadores.
  3. **Scraping en el Cliente:** `EventRepository.kt` hace llamadas HTTP directas con `Jsoup` a `listadomanga.es` dentro de la App móvil. Esto es inestable, propenso a bloqueos por CORS/UA, consume datos móviles innecesarios y falla sin conexión si la caché en assets (`events.json`) se desactualiza.

### C. Persistencia y Datos
- Room gestiona 13 entidades locales orientadas únicamente al seguimiento individual (materiales, pelucas, presupuesto, checklist).
- Carece de modelos para participaciones grupales, usuarios/autenticación, asignación de personajes entre miembros o galerías compartidas.

### D. Testing
- `app/src/test` y `app/src/androidTest` están totalmente vacíos. Cobertura de pruebas actual: **0%**.

### E. Documentación
- `README.md` básico descriptivo del cliente Android local.

---

## 2. Matriz de Decisiones: Conservar, Refactorizar y Eliminar

| Componente / Código | Acción | Justificación |
|---|---|---|
| **Estructura JSON de Eventos (`events.json`)** | **CONSERVAR** | Servirá como semilla (*seed*) inicial para el importador de eventos del backend. |
| **Conceptos de Dominio (Cosplay, Eventos, Materiales)** | **CONSERVAR** | Se trasladarán como entidades puras de Java al módulo de Dominio del backend. |
| **Diseños y Temas Compose (Material 3)** | **CONSERVAR** | Se mantendrán en el cliente Android, adaptándolos para consumir la API REST del backend. |
| **Repositorios Android y ViewModel actuales** | **REFACTORIZAR** | Se reescribirán en el cliente Android para sustituir las llamadas a Room por llamadas HTTP Retrofit/Ktor hacia la API REST `/api/v1/...`. |
| **Lógica de Web Scraping (`Jsoup`)** | **REFACTORIZAR** | Se extraerá de la app móvil y se reubicará en un adaptador de salida del backend Java (`EventScraperAdapter`). |
| **Persistencia SQLite local con Room** | **ELIMINAR / DEPRECAR** | Se sustituye por el backend centralizado Java + Spring Boot + PostgreSQL + Flyway. |
| **Generación de PDF cliente con iText7** | **ELIMINAR / DEPRECAR** | Se desacopla de la app móvil. Los reportes se generarán mediante endpoints backend si se requieren en el futuro. |

---

## 3. ADR 0001: Redefinición del Producto a Plataforma Colaborativa con Backend Hexagonal en Java

### Estado
Aprobado.

### Contexto
Cosplay Journal nació como una aplicación de notas e itinerario personal local. Sin embargo, las necesidades reales de los cosplayers abarcan la colaboración en grupo, la coordinación para eventos y la compartición segura de fotos tras las convenciones. Para soportar estas capacidades, se requiere un backend centralizado y profesional.

### Decisión
1. Redefinir la visión del producto hacia una plataforma colaborativa cliente-servidor.
2. Adoptar **Java 21**, **Spring Boot 3** y **Arquitectura Hexagonal (Puertos y Adaptadores)** como pilar técnico del backend.
3. Utilizar **PostgreSQL** con **Flyway** para la persistencia transaccional relacional.
4. Diseñar la comunicación mediante **API REST (`/api/v1/`)** documentada con OpenAPI 3.0.
5. Introducir **Apache Kafka** para el procesamiento asíncrono de eventos de dominio cuando la necesidad esté justificada.
6. Transformar la App Android existente en un cliente liviano (Adaptador de Entrada móvil) alimentado por la API del backend.

### Consecuencias Positivas
- Posibilita la colaboración grupal, invitación de miembros, asignación de personajes y fotos compartidas.
- Garantiza la independencia del dominio de cualquier framework o tecnología mediante Arquitectura Hexagonal.
- Cobertura de pruebas del 100% en el dominio sin depender de Android ni emuladores.
- Preparación para escalar a contenedores Docker y despliegue en Kubernetes.

### Consecuencias Negativas / Desafíos
- Requiere construir la infraestructura del backend desde cero (repositorio/módulos backend).
- Transición en la app Android de almacenamiento Room local a consumo de API REST.
