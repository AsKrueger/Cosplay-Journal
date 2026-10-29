# Arquitectura de Despliegue de Coste Prácticamente Cero (0 €/mes) — Cosplay Journal

## 1. Principio Económico y Objetivo

El objetivo de esta arquitectura es permitir el **despliegue público del MVP y portfolio de Cosplay Journal con un coste mensual objetivo de 0 €/mes (máximo < 5 €/mes)**, garantizando la persistencia fiable de los datos, la observabilidad, la resiliencia frente a reinicios y la seguridad sin incurrir en facturación inesperada.

---

## 2. Clasificación de Componentes del Backend

Se audita la totalidad de los componentes del backend Java 21 y se clasifican según su necesidad operativa en producción:

| Componente | Clasificación | Justificación y Estrategia de Producción |
|---|---|---|
| **Spring Boot 3 (Java 21 JRE)** | **Necesario en ejecución** | Núcleo de la API REST y lógica de aplicación. Se empaqueta en imagen Docker multi-etapa Alpine (512 MB RAM). |
| **PostgreSQL 16 + Flyway** | **Necesario en ejecución** | Persistencia relacional de usuarios, cosplays, eventos y participaciones. Requiere almacenamiento persistente que no expire. |
| **Apache Kafka Broker** | **Opcional / Sustituible en Prod** | Imprescindible para pruebas de integración con Testcontainers. En producción $0/mes se sustituye mediante **Upstash Kafka (plan gratuito)** o adaptador **In-Process Spring EventBus** sin alterar el dominio. |
| **Spring Security + JWT** | **Necesario en ejecución** | Autenticación stateless y control de ownership (`CurrentUserPort`). Inyecta `JWT_SECRET` por variable de entorno. |
| **Scraper ListadoManga (Jsoup)** | **Necesario en ejecución** | Ingesta y sincronización de eventos de cosplay en España. Reintentos exponenciales y aislamiento técnico. |
| **Scheduler (`@Scheduled`)** | **Necesario en ejecución** | Invocador interno que ejecuta la sincronización cada 6 horas ($0/mes adicional). |
| **Micrometer + Actuator** | **Necesario en ejecución** | Métrica e indicadores de salud (`/actuator/health`, `/actuator/metrics`). |
| **Testcontainers** | **Necesario en Dev / CI** | Exclusivo de la suite de pruebas automatizadas en GitHub Actions y entorno local. |

---

## 3. Evaluación de Kafka en Producción ($0 / mes)

Mantener un clúster administrado de Kafka (como AWS MSK, $100+/mes) es inviable para un presupuesto cero. Se evalúan dos alternativas que preservan la Arquitectura Hexagonal:

### Opción A: Upstash Kafka (Plan Gratuito)
- **Cuota verificada:** 10.000 mensajes diarios, máximo 1.000 mensajes por segundo, 256 MB de retención y 10 particiones ([página de precios](https://upstash.com/pricing/kafka), [documentación](https://upstash.com/docs/kafka/overall/pricing)).
- **No confundir con QStash:** es otro producto de Upstash, con 1.000 mensajes diarios gratuitos ([precios QStash](https://upstash.com/qstash/pricing)).
- **Ventaja:** Conserva el adaptador Kafka nativo (`KafkaDomainEventPublisherAdapter`) enviando mensajes reales.
- **Riesgo:** Al superar la cuota, los eventos fallarán. Debe existir una política de reintento o de degradación antes de depender de esta opción.

### Opción B: Adaptador In-Process `SpringApplicationEventPublisherAdapter`
- **Estrategia:** Implementación del puerto [`DomainEventPublisherPort`](file:///C:/Users/lovei/Documents/XD/Cosplay-Journal/backend/src/main/java/com/cosplayjournal/application/port/out/DomainEventPublisherPort.java) utilizando `ApplicationEventPublisher` síncrono/asíncrono de Spring Boot.
- **Ventaja:** Cero dependencias externas en producción. La suite de testcontainers en CI mantiene las pruebas reales contra Kafka.

---

## 4. Persistencia PostgreSQL y Matriz de Riesgo de Datos

Se descarta el plan gratuito de Render Postgres porque las bases de datos gratuitas expiran 30 días después de su creación y se eliminan tras un periodo de gracia de 14 días (riesgo total de pérdida de datos).

| Proveedor | Cuota Gratuita / Precio | Persistencia | Copias de Seguridad | Riesgo / Recomendación |
|---|---|---|---|---|
| **Neon PostgreSQL** | 0,5 GB de almacenamiento y 100 CU-hora/mes de cómputo por proyecto ($0/mes) | El plan gratuito no expira; al agotar la cuota mensual se suspende el cómputo hasta el siguiente ciclo | **Sin copias diarias en el plan gratuito.** Solo una ventana de historial de 6 horas (1 GB) y 1 snapshot manual. Requiere `pg_dump` periódico propio | **RECOMENDADO ($0/mes)**, con 0,5 GB como límite duro y copias externas obligatorias |
| **Supabase Postgres** | 500 MB de base de datos, 5 GB de egreso según la tabla oficial (fuentes de terceros dan 10 GB), máximo 2 proyectos activos ($0/mes) | Se pausa tras 1 semana de inactividad; los datos no se borran, pero la base no responde hasta reactivarla | Manual con `pg_dump` | Aceptable con reactivación manual |
| **Railway Postgres** | Prueba de 30 días con 5 $ únicos. Después, plan Free con 1 $/mes de crédito que no se acumula, o Hobby desde 5 $/mes. Los volúmenes de cuentas de prueba se eliminan 30 días después de agotar el crédito | Persistente mientras la cuenta esté activa | Automáticas | **Descartado para $0/mes**: el crédito de 1 $ no garantiza un servicio siempre activo |
| **Oracle Always Free** | 200 GB de volúmenes de bloque en total (arranque y datos), y 2 bases Autonomous de 20 GB cada una según páginas regionales de Oracle | Persistente | Hasta 5 copias de volumen incluidas en el límite; sin copias automáticas de la base sin configurar | Viable, pero requiere mantener una VM o gestionar la base de forma propia |

---

## 5. Alojamiento del Backend Java 21 (PaaS vs Serverless)

- **Render (Plan Free):**
  - Ejecución de la imagen Docker `eclipse-temurin:21-jre-alpine`. **La cifra de RAM no está confirmada** en las fuentes consultadas.
  - Se suspende tras 15 minutos sin tráfico entrante. Vuelve en unos segundos al recibir una petición (cold start).
  - 750 horas de instancia gratuitas al mes. Las horas suspendidas no cuentan.
  - **Sistema de archivos efímero:** todo fichero escrito en disco (por ejemplo, imágenes subidas) se pierde en cada redeploy, reinicio o suspensión.
  - Coste: **0 €/mes**.
- **Railway (Plan Free tras la prueba):** 0,5 GB de RAM y 1 vCPU por servicio, con 1 $ de crédito mensual que no se acumula. Tras 30 días o al agotar los 5 $ de prueba, el servicio pasa a este plan. Un backend que esté siempre activo puede agotar ese crédito. **Riesgo de cargo** si se pasa a Hobby (5 $/mes).
- **Fly.io:** no revisado en esta pasada.

---

## 6. Almacenamiento de Fotografía e Imágenes

Para evitar almacenar binarios en el sistema de archivos efímero del contenedor:
- **Cloudinary (Plan gratuito):** 25 créditos mensuales en un pool compartido. Un crédito equivale a 1 GB de almacenamiento, 1 GB de ancho de banda de imagen o 1.000 transformaciones. No son 25 GB de cada cosa: el reparto depende del uso ([facturación](https://cloudinary.com/documentation/billing_and_plans)).
- **URLs Externas (Fallback MVP):** Permitir enlaces directos HTTP/HTTPS a fotos sin almacenamiento de archivos binarios.

---

## 7. Diagrama de la Arquitectura Propuesta (Target: 0 € / mes)

```text
               Android Client / Browser
                          │
                          ▼
           PaaS Container (Render / Railway)
            Java 21 JRE + Spring Boot 3
                          │
         ┌────────────────┼────────────────┐
         ▼                ▼                ▼
  Neon PostgreSQL     Upstash Kafka     Cloudinary
   (Data 0.5 GB)     (10k msgs/day)    (25 créditos/mes)
      [0 €/m]           [0 €/m]           [0 €/m]
```

**Coste Total Estimado de la Plataforma:** **0,00 € / mes**.

---

## 8. Hoja de Ruta de Migración Futura (Upgrade Path)

Cuando el tráfico y los usuarios activos superen las cuotas gratuitas:
1. **Paso 1 ($15 - $30/mes):** Migrar PostgreSQL a Amazon RDS `db.t4g.micro` o Supabase Pro.
2. **Paso 2 ($30 - $60/mes):** Desplegar backend en AWS ECS Fargate con balanceador de carga ALB y certificado SSL/TLS.
3. **Paso 3 (> $100/mes):** Migrar eventos a Amazon MSK Serverless o Confluent Cloud Pro.

---

## 9. Sincronización Automática y Frontend

**Sincronización de eventos.** El `@Scheduled` interno ejecuta la importación cada 6 horas (`cosplay-journal.events.sync.cron`). En los PaaS gratuitos el contenedor se suspende tras un periodo de inactividad, y mientras está suspendido el scheduler no se ejecuta. Por eso no se puede depender solo de él.

Estrategia recomendada para $0/mes:
- Mantener el scheduler activo cuando el servicio esté despierto.
- Permitir la sincronización bajo demanda mediante `POST /api/v1/admin/events/import`, protegido por rol administrador.
- No usar GitHub Actions como scheduler salvo que sus cuotas se verifiquen. Con esta estrategia no hace falta.

**Frontend.** El cliente principal es la app Android, que no requiere alojamiento. No existe frontend web en el repositorio, así que no se añade ninguna tecnología nueva por motivos de coste.

---

## 10. Fuentes Verificadas y Pendientes

Revisión del 28/10/2026 contra las fuentes oficiales indicadas. Los precios cambian: revisarlos de nuevo antes de cualquier despliegue.

**Verificado:**
- Neon: [planes y límites](https://neon.com/docs/introduction/free-tier) (0,5 GB, 100 CU-hora/mes) y [ventana de historial](https://neon.com/docs/postgres/backup-restore/history-window) (6 horas en el plan gratuito).
- Oracle Always Free: [referencia de recursos](https://docs.oracle.com/en-us/iaas/Content/FreeTier/resourceref.htm) (200 GB de bloques). Las bases de 20 GB proceden de páginas regionales y guías de terceros.
- Render: [expiración de bases gratuitas a 30 días](https://render.com/changelog/free-postgresql-instances-now-expire-after-30-days-previously-90) y [planes gratuitos](https://render.com/docs/free).
- Upstash Kafka: [precios](https://upstash.com/pricing/kafka) y [documentación](https://upstash.com/docs/kafka/overall/pricing).
- Supabase: [precios](https://supabase.com/pricing) (500 MB, pausa tras una semana de inactividad).
- Railway: [planes](https://docs.railway.com/pricing/plans), [prueba gratuita](https://docs.railway.com/pricing/free-trial) (prueba de 30 días con 5 $, plan Free con 0,5 GB de RAM y 1 $/mes de crédito).
- Render: [planes gratuitos](https://render.com/docs/free) (suspensión tras 15 minutos, sistema de archivos efímero, 750 horas al mes).
- Supabase: tabla de egreso de [precios](https://supabase.com/pricing) (5 GB para el plan Free).
- Cloudinary: [facturación](https://cloudinary.com/documentation/billing_and_plans) (pool de créditos mensuales).

**Pendiente de verificar en la página oficial:**
- RAM del plan gratuito de Render: no confirmada.
- Fly.io: no revisado.
- Egreso de Supabase: 5 GB según la tabla oficial, pero las fuentes de terceros dan 10 GB. Confirmar en la página de precios antes de desplegar.
- Bases Autonomous de 20 GB de Oracle: solo confirmadas en páginas regionales y guías de terceros.
- Uso del historial de Neon: las páginas antiguas de neon.tech dan 24 horas o 7 días; la vigente indica 6 horas.
