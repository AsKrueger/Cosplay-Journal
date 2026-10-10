# ADR 0014: Auditoría de Preparación para Despliegue y Estrategia Cloud

- **Fecha:** 2025-02-18
- **Estado:** Aprobado
- **Autores:** Equipo de Arquitectura de Cosplay Journal

---

## 1. Contexto

Tras implementar el backend del sistema con Arquitectura Hexagonal, Seguridad JWT, Kafka, Testcontainers y CI/CD en GitHub Actions, se realizó una auditoría de preparación para el despliegue en entornos de producción y una evaluación de viabilidad técnica y económica en AWS frente a opciones PaaS.

---

## 2. Alternativas Evaluadas

### Opción A: Despliegue Completo en AWS Enterprise (App Runner + RDS PostgreSQL + Amazon MSK) (Rechazada para MVP)
- **Desventajas:** Amazon MSK y NAT Gateways en VPC privada generan un coste base mensual superior a **$110-$150/mes**, desproporcionado para la fase inicial del proyecto.

### Opción B: Despliegue Híbrido Económico en AWS ECS Fargate + RDS PostgreSQL + Confluent Cloud (Aprobada para Fase Cloud)
- **Ventajas:** Mantiene el backend en AWS mediante ECS Fargate ($12/mes) y RDS PostgreSQL ($15/mes), utilizando la capa gratuita de Confluent Cloud para Kafka. Coste total ~$30/mes.

### Opción C: Despliegue en PaaS (Render / Railway / Fly.io) mediante Docker Container (Seleccionada para MVP Inicial)
- **Ventajas:**
  - Despliegue automático basado en el [`Dockerfile`](file:///C:/Users/lovei/Documents/XD/Cosplay-Journal/backend/Dockerfile) multi-etapa de Java 21.
  - Soporte nativo para PostgreSQL 16 con Flyway.
  - Coste mínimo controlado (**~$7 - $15/mes**) sin riesgos de facturación oculta por NAT Gateways.

---

## 3. Decisión Aprobada

Se aprueba la **Opción C** para el MVP inicial y la **Opción B** para una posterior migración a AWS cuando la carga del sistema lo justifique:
1. Crear el archivo de perfil de producción [`application-prod.properties`](file:///C:/Users/lovei/Documents/XD/Cosplay-Journal/backend/src/main/resources/application-prod.properties) e inyectar variables de entorno requeridas en tiempo de ejecución.
2. Definir la imagen Docker multi-etapa [`Dockerfile`](file:///C:/Users/lovei/Documents/XD/Cosplay-Journal/backend/Dockerfile) con `eclipse-temurin:21-jre-alpine` y usuario no privilegiado.
3. Documentar los resultados y la hoja de ruta en [`DEPLOYMENT_READINESS.md`](file:///C:/Users/lovei/Documents/XD/Cosplay-Journal/docs/deployment/DEPLOYMENT_READINESS.md).

---

## 4. Consecuencias

### Positivas
- Evaluación financiera y técnica rigurosa que previene costes inesperados en servicios cloud.
- Backend preparado para producción con perfil `prod`, variables de entorno y contenedor Docker seguro.
- Hoja de ruta clara sin alterar la Arquitectura Hexagonal del proyecto.
