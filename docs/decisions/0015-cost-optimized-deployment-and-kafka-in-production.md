# ADR 0015: Despliegue de Coste Cero y Kafka en Producción

- **Fecha:** 2026-10-28
- **Estado:** Propuesta (pendiente de aprobación)
- **Autores:** Equipo de Arquitectura de Cosplay Journal
- **Relacionada con:** [ADR 0014](0014-deployment-readiness-and-cloud-strategy.md), que aprobó un PaaS para el MVP.

---

## 1. Contexto

La [ADR 0014](0014-deployment-readiness-and-cloud-strategy.md) eligió un PaaS para la fase inicial, con Kafka en la capa gratuita de Confluent Cloud. El objetivo de esta propuesta es llegar a **0 €/mes** de coste recurrente, con persistencia fiable de datos.

Para cumplir ese objetivo hay que decidir qué hacer con Kafka en producción. La [guía de arquitectura](../deployment/COST_OPTIMIZED_ARCHITECTURE.md) documenta los límites verificados de cada proveedor.

---

## 2. Alternativas Evaluadas

### Opción A: Upstash Kafka (plan gratuito)
- **Ventajas:** Conserva el adaptador `KafkaDomainEventPublisherAdapter` en producción, con mensajes reales.
- **Límites verificados:** 10.000 mensajes al día, 256 MB de retención y 10 particiones.
- **Desventajas:** Un proveedor externo más. Si se superan los 10.000 mensajes diarios, los eventos fallan.

### Opción B: Adaptador in-process (`SpringApplicationEventPublisherAdapter`)
- **Ventajas:** Cero dependencias externas. Mantiene el puerto `DomainEventPublisherPort` y el aislamiento hexagonal.
- **Desventajas:** Los eventos no sobreviven a un reinicio del contenedor si no se persisten. Pierde el desacoplamiento que aporta Kafka entre productores y consumidores.

### Opción C: Mantener Kafka autogestionado o administrado
- **Desventajas:** Incompatible con 0 €/mes. Amazon MSK supera los 100 $/mes.

---

## 3. Propuesta

Mantener el puerto `DomainEventPublisherPort` sin cambios y ofrecer dos adaptadores, seleccionables por perfil de Spring:

- **Perfil `prod` con Upstash:** `KafkaDomainEventPublisherAdapter` apuntando a Upstash.
- **Perfil `prod` sin Kafka:** `SpringApplicationEventPublisherAdapter`.

La suite de Testcontainers con Kafka real se mantiene en CI, para no perder cobertura de la integración.

**No se elimina Kafka de la arquitectura.** Solo se decide qué broker usa el perfil de producción.

---

## 4. Consecuencias

- Se mantiene el aislamiento hexagonal: el dominio no conoce el adaptador.
- Riesgo de cuota: si Upstash supera 10.000 mensajes al día, hay que definir la política de fallo (reintento, descarte o degradación a in-process).
- Pérdida de garantías con el adaptador in-process: entrega no duplicada y reintentos persistentes, que el diseño actual de Kafka sí ofrece.
- Coste de mantenimiento: dos adaptadores que probar.

---

## 5. Decisión pendiente

Esta ADR queda en **Propuesta**. Se aprobará o rechazará cuando el equipo elija:

1. Upstash en producción, o
2. Adaptador in-process desde el inicio.
