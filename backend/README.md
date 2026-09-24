# Cosplay Journal Backend — Java 21 / Spring Boot 3 Hexagonal

Backend centralizado para la plataforma **Cosplay Journal**, construido con **Java 21 LTS**, **Spring Boot 3** y **Arquitectura Hexagonal (Puertos y Adaptadores)**.

---

## 🏗️ Estructura de Capas (Arquitectura Hexagonal)

```text
com.cosplayjournal
├── CosplayJournalApplication.java   # Clase principal Spring Boot
├── domain/                         # Dominio Puro (Java puro sin Spring ni JPA)
│   ├── model/                      # Entidades e Invariantes (Cosplay, CosplayStatus)
│   ├── exception/                  # Excepciones de negocio (CosplayNotFoundException)
│   └── service/                    # Servicios de dominio si aplica
├── application/                    # Capa de Aplicación
│   ├── port/
│   │   ├── in/                     # Puertos de Entrada (CreateCosplayUseCase, GetCosplayUseCase)
│   │   └── out/                    # Puertos de Salida (CosplayRepositoryPort)
│   └── service/                    # Servicio de Aplicación (CosplayApplicationService)
└── infrastructure/                 # Capa de Infraestructura
    ├── adapter/
    │   ├── in/
    │   │   └── rest/               # Controlador REST, DTOs, Mappers y Manejo Global de Errores
    │   └── out/
    │       └── persistence/        # Adaptador de persistencia temporal (InMemoryCosplayRepositoryAdapter)
    └── config/                     # Configuración de Beans de Spring (BeanConfiguration)
```

---

## 🛠️ Requisitos Previos

- **Java 21 LTS** o superior
- **Apache Maven 3.9+** (o utilizar el wrapper incluido `./mvnw` / `.\mvnw.cmd`)

---

## 🚀 Compilación y Ejecución

### Ejecutar tests unitarios e integración:
```bash
./mvnw test
```
En Windows PowerShell:
```powershell
.\mvnw.cmd test
```

### Arrancar la aplicación Spring Boot:
```bash
./mvnw spring-boot:run
```
En Windows PowerShell:
```powershell
.\mvnw.cmd spring-boot:run
```

La aplicación arrancará en el puerto **8080**.

---

## 📡 Endpoints Disponibles (v1)

### 1. Comprobación de Estado (Health Check)
```http
GET /api/v1/health
```
**Respuesta (200 OK):**
```json
{
  "status": "UP"
}
```

### 2. Crear Proyecto de Cosplay
```http
POST /api/v1/cosplays
Content-Type: application/json

{
  "name": "Spider-Man Classic",
  "description": "Traje rojo y azul de Peter Parker",
  "characterName": "Spider-Man",
  "originSeries": "Marvel Comics"
}
```
**Respuesta (201 Created):**
```json
{
  "id": 1,
  "name": "Spider-Man Classic",
  "description": "Traje rojo y azul de Peter Parker",
  "characterName": "Spider-Man",
  "originSeries": "Marvel Comics",
  "status": "IDEA",
  "createdAt": "2026-09-24T12:00:00Z",
  "updatedAt": "2026-09-24T12:00:00Z"
}
```

### 3. Consultar Cosplay por ID
```http
GET /api/v1/cosplays/1
```

### 4. Consultar Todos los Cosplays
```http
GET /api/v1/cosplays
```

---

## 🧪 Pruebas y Cobertura

- **Pruebas de Dominio:** Pruebas unitarias ultrarrápidas (<10ms) en `com.cosplayjournal.domain.CosplayTest` sin contexto de Spring.
- **Pruebas de Aplicación:** Pruebas con fakes/mocks en `com.cosplayjournal.application.CosplayApplicationServiceTest`.
- **Pruebas de Integración REST:** Pruebas end-to-end con `@SpringBootTest` y `MockMvc` en `com.cosplayjournal.infrastructure.adapter.in.rest.CosplayControllerIntegrationTest`.
