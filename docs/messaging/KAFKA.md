# Arquitectura de Mensajería Asíncrona con Apache Kafka — Cosplay Journal

## 1. Visión General

**Cosplay Journal** utiliza **Apache Kafka** como plataforma de streaming de eventos asíncronos para desacoplar el procesamiento de la actividad colaborativa del grupo (notificaciones, registro de actividad, feeds comunitarios y métricas) fuera del ciclo síncrono HTTP de la API REST.

La integración respeta rigurosamente la **Arquitectura Hexagonal**:
- El módulo de **Dominio** emite eventos puros (`com.cosplayjournal.domain.event`).
- La capa de **Aplicación** publica los eventos mediante el puerto abstracto [`DomainEventPublisherPort`](file:///C:/Users/lovei/Documents/XD/Cosplay-Journal/backend/src/main/java/com/cosplayjournal/application/port/out/DomainEventPublisherPort.java).
- La capa de **Infraestructura** implementa la mensajería mediante el adaptador de salida Kafka ([`KafkaDomainEventPublisherAdapter`](file:///C:/Users/lovei/Documents/XD/Cosplay-Journal/backend/src/main/java/com/cosplayjournal/infrastructure/adapter/out/messaging/kafka/adapter/KafkaDomainEventPublisherAdapter.java)) y los consumidores de entrada ([`ParticipantJoinedKafkaConsumerAdapter`](file:///C:/Users/lovei/Documents/XD/Cosplay-Journal/backend/src/main/java/com/cosplayjournal/infrastructure/adapter/in/kafka/ParticipantJoinedKafkaConsumerAdapter.java)).

---

## 2. Diagrama de Arquitectura de Eventos

```text
                     REST CONTROLLER (HTTP Input Adapter)
                                    │
                                    ▼
                         APPLICATION SERVICE
                                    │
                         Domain Event emitido
                                    │
                                    ▼
                         DomainEventPublisherPort
                                    │
                                    ▼
                 KafkaDomainEventPublisherAdapter (Output Adapter)
                                    │
                                    ▼
                 ┌──────────────────────────────────────┐
                 │       Apache Kafka Broker            │
                 ├──────────────────────────────────────┤
                 │ Topic: cosplay-journal.participation │
                 │ Topic: cosplay-journal.photo         │
                 └──────────────────┬───────────────────┘
                                    │
                                    ▼
              ParticipantJoinedKafkaConsumerAdapter (Input Adapter)
                                    │
                                    ▼
                         ProcessParticipantJoinedUseCase
                                    │
                                    ▼
                 ProcessParticipantJoinedService (Idempotente)
```

---

## 3. Topics y Claves de Mensaje

| Topic Kafka | Eventos Asociados | Clave de Mensaje (`Kafka Key`) | Propósito / Garantía de Orden |
|---|---|---|---|
| `cosplay-journal.participation` | `ParticipationCreatedEvent`, `ParticipantJoinedEvent` | `participationId` | Garantiza que todos los eventos relativos a la misma participación se procesan secuencialmente en la misma partición. |
| `cosplay-journal.photo` | `PhotoUploadedEvent` | `participationId` | Permite el procesamiento de fotos asociadas a una participación en orden. |

---

## 4. Contratos de Mensajes Kafka (DTOs)

Los mensajes Kafka utilízanse DTOs desacoplados de los modelos de dominio internos (`com.cosplayjournal.infrastructure.adapter.out.messaging.kafka.dto`):

### Estructura JSON Estandarizada:
```json
{
  "eventId": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
  "eventType": "ParticipantJoined",
  "occurredAt": "2026-09-30T14:30:00Z",
  "aggregateId": "part-100",
  "userId": "user-45",
  "role": "MEMBER"
}
```

---

## 5. Estrategia de Idempotencia y Manejo de Errores

1. **Garantía de Entrega:** Kafka garantiza *at least once delivery*.
2. **Estrategia de Idempotencia en Consumidor:** El servicio [`ProcessParticipantJoinedService`](file:///C:/Users/lovei/Documents/XD/Cosplay-Journal/backend/src/main/java/com/cosplayjournal/application/service/ProcessParticipantJoinedService.java) almacena las claves procesadas (`participationId:userId`) para evitar duplicados en reprocesamientos de mensajes.
3. **Manejo de Errores:** Excepciones de deserialización o formato inválido son capturadas por el adaptador consumidor evitando caídas en bucle del listener.

---

## 6. Despliegue Local con Docker Compose (KRaft Mode)

Kafka se ejecuta localmente mediante **Apache Kafka en modo KRaft (sin necesidad de Zookeeper)** configurado en [`docker-compose.yml`](file:///C:/Users/lovei/Documents/XD/Cosplay-Journal/backend/docker-compose.yml):

```bash
cd backend
docker-compose up -d kafka
```

- **Host Broker:** `localhost:9092`
- **Internal Network:** `kafka:29092`
