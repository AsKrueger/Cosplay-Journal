# Auditoría de Preparación para Despliegue y Viabilidad de AWS — Cosplay Journal

## 1. Visión General y Estado Actual del Backend

Esta auditoría evalúa la madurez técnica, seguridad, operabilidad, arquitectura de infraestructura y viabilidad económica para desplegar el backend de **Cosplay Journal** en producción.

### Estado Técnico Actual (Issues #0 a #16 Completadas):
- **Lenguaje y Framework:** Java 21 LTS, Spring Boot 3.3.5, Maven Wrapper (`mvnw`).
- **Arquitectura:** Hexagonal pura (Dominio y Aplicación 100% aislados de frameworks).
- **Base de Datos:** PostgreSQL 16 + Flyway Migrations (V1 a V5).
- **Mensajería Asíncrona:** Apache Kafka (Topics: `cosplay-journal.participation`, `cosplay-journal.photo`, `cosplay-journal.event`).
- **Seguridad y Autorización:** Spring Security + JWT Stateless + Resource Ownership (`CurrentUserPort`).
- **Integraciones Externas:** Scraper ListadoManga (Jsoup) con reintentos exponenciales, timeouts y ejecutor programado `@Scheduled`.
- **API REST & OpenAPI:** Especificación OpenAPI 3.0 / Swagger UI en `/swagger-ui/index.html`.
- **Observabilidad:** Spring Boot Actuator (`/actuator/health`, `/actuator/metrics`) + Micrometer.
- **Testing:** 87 tests (Unitarios, Arquitectura, Resiliencia, OpenAPI y Testcontainers PostgreSQL 16/Kafka real).
- **CI/CD:** Pipeline automatizado en GitHub Actions ([`.github/workflows/backend-ci.yml`](file:///C:/Users/lovei/Documents/XD/Cosplay-Journal/.github/workflows/backend-ci.yml)).

---

## 2. Bloqueos Identificados y Brechas antes de Producción

1. **Gestión de Archivos Multimedia (Fotos):** Actualmente las referencias de fotos (`storageReference`) son URIs. En producción se requiere integración con un almacenamiento de objetos ([`PhotoStoragePort`](file:///C:/Users/lovei/Documents/XD/Cosplay-Journal/backend/src/main/java/com/cosplayjournal/application/port/out/PhotoRepositoryPort.java)) como Amazon S3 o MinIO.
2. **Estrategia para Apache Kafka en Producción:** Kafka es ideal para eventos de dominio asíncronos. En un entorno sin presupuesto elevado para MVP, mantener un cluster administrado de Kafka (AWS MSK) es costoso. Se evalúa el uso de un bus de eventos en memoria/transaccional como fallback sin alterar la arquitectura.
3. **Política de CORS (Cross-Origin Resource Sharing):** Debe habilitarse explícitamente en `SpringSecurityConfig` para permitir llamadas desde clientes móviles/web autenticados.
4. **Perfil de Producción ([`application-prod.properties`](file:///C:/Users/lovei/Documents/XD/Cosplay-Journal/backend/src/main/resources/application-prod.properties)):** Creado para inyectar variables de entorno requeridas (`POSTGRES_HOST`, `POSTGRES_PASSWORD`, `JWT_SECRET`).

---

## 3. Requisitos Mínimos para Entorno de Producción

- **Contenedor Docker Mínimo y Seguro:** Creado [`Dockerfile`](file:///C:/Users/lovei/Documents/XD/Cosplay-Journal/backend/Dockerfile) multi-etapa basado en `eclipse-temurin:21-jre-alpine`, ejecutado como usuario no privilegiado (`cosplayuser`), con `HEALTHCHECK` activo.
- **Secretos:** `JWT_SECRET` debe tener un mínimo de 256 bits y proceder de variables de entorno inyectadas en tiempo de ejecución.
- **Salud del Servidor:** Integración de `/api/v1/health` y `/actuator/health` en las sondas de liveness/readiness del orquestador.

---

## 4. Comparativa de Opciones de Despliegue y Estimación de Costes

Se comparan las alternativas de infraestructura en **AWS** frente a plataformas **PaaS** (Render / Railway / Fly.io):

| Criterio | Opción A: AWS App Runner + RDS PostgreSQL + AWS MSK | Opción B: AWS ECS Fargate + RDS PostgreSQL + Confluent Cloud | Opción C: PaaS (Render / Railway / Fly.io) + Managed Postgres |
|---|---|---|---|
| **Servidor App Java 21** | AWS App Runner (0.5 vCPU, 1 GB RAM) | AWS ECS Fargate (0.25 vCPU, 0.5 GB RAM) | Web Service Docker (512 MB - 1 GB RAM) |
| **Base de Datos** | Amazon RDS PostgreSQL (`db.t4g.micro`) | Amazon RDS PostgreSQL (`db.t4g.micro`) | Managed PostgreSQL 16 (1 GB RAM) |
| **Kafka Broker** | Amazon MSK Serverless | Confluent Cloud / CloudKarafka (Free Tier) | Upstash Kafka / CloudKarafka (Free) |
| **Coste Estimado Mensual** | **~$110 - $160 / mes** (MSK eleva el coste) | **~$30 - $45 / mes** | **~$7 - $15 / mes** |
| **Complejidad Operativa** | Media (Gestión VPC, Subnets, IAM) | Alta (ECS Task definitions, VPC, Security Groups) | Muy Baja (Despliegue directo con `git push`) |
| **Free Tier / Cuota Gratuita** | RDS `db.t4g.micro` 750h/mes gratis por 12 meses | RDS `db.t4g.micro` 750h/mes gratis por 12 meses | Créditos iniciales / Planes Developer económicos |
| **Riesgo de Cargos Ocultos** | **Alto** (NAT Gateways en VPC privada ~$32/mes) | **Medio** (Tráfico de red inter-AZ) | **Nulo** (Coste fijo mensual) |

### Riesgos de Costes Inesperados en AWS:
- **NAT Gateways:** Colocar instancias de RDS o ECS en subredes privadas con NAT Gateway genera un coste fijo base de ~$32,40/mes sin importar el tráfico.
- **Amazon MSK:** MSK no forma parte de la capa gratuita de AWS. El coste mínimo mensual por clúster es de ~$100/mes.

---

## 5. Evolución de CI a CD (Continuous Deployment)

Para evolucionar la Integración Continua actual ([`.github/workflows/backend-ci.yml`](file:///C:/Users/lovei/Documents/XD/Cosplay-Journal/.github/workflows/backend-ci.yml)) hacia un despliegue continuo (CD):
1. **Aislamiento de Entornos:** Mantener el workflow de CI para PRs y crear `.github/workflows/backend-cd.yml` que se active al fusionar cambios en `main` o al etiquetar un release (`v1.0.0`).
2. **Gestión de Secretos en GitHub:** Almacenar `PROD_DATABASE_URL`, `PROD_POSTGRES_USER`, `PROD_POSTGRES_PASS` y `PROD_JWT_SECRET` en GitHub Repository Secrets.
3. **Estrategia de Despliegue:** Construir la imagen Docker, publicarla en un registro (GitHub Container Registry `ghcr.io` o AWS ECR) e invocar el webhook de actualización de la plataforma de producción.

---

## 6. Recomendación Final y Hoja de Ruta Priorizada

### Recomendación Arquitectónica:
1. **Fase Inicial MVP / Portfolio (Fase Actual):** Desplegar en **PaaS (Render / Railway / Fly.io)** o **AWS ECS Fargate + RDS PostgreSQL** usando la imagen [`Dockerfile`](file:///C:/Users/lovei/Documents/XD/Cosplay-Journal/backend/Dockerfile) recién creada. Esto mantiene los costes por debajo de **$15/mes** sin comprometer la arquitectura.
2. **Fase Enterprise / Escala (> 10.000 usuarios activos):** Migrar a **AWS App Runner / EKS + Amazon RDS Multi-AZ + Amazon MSK** cuando la carga y alta disponibilidad lo justifiquen.

### Próximas Tareas Recomendadas:
1. **[Fase Android] Issue #17:** Integración del Cliente Móvil Android con la API REST de Cosplay Journal.
2. **[Fase Media] Issue #18:** Integración de Amazon S3 / MinIO para el almacenamiento real de fotografías de cosplay ([`PhotoStoragePort`](file:///C:/Users/lovei/Documents/XD/Cosplay-Journal/backend/src/main/java/com/cosplayjournal/application/port/out/PhotoRepositoryPort.java)).
