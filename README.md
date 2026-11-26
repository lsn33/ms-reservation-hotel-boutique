# ms-reservation-hotel-boutique

Microservicio de reservas y habitaciones (puerto 8082) de AndesStay / Hotel Boutique (DSY1107, EP2 y EP5).
Aplica las reglas de negocio, persiste en PostgreSQL (Neon), publica/consume mensajes en RabbitMQ y publica/consume eventos en Apache Kafka. Kafka convive con RabbitMQ: no lo reemplaza y no altera sus colas.

## Requisitos
- Java 21, Maven 3.9+
- RabbitMQ (clúster de 2 nodos, ver repo `ms-rabbit-admin`, carpeta `infra/`)
- Apache Kafka (3 Zookeeper + 3 brokers + Kafka UI, ver repo `ms-kafka-admin`, carpeta `infra/`)
- Pool de Amazon Cognito (IDaaS) y base PostgreSQL

## Mensajería (RabbitMQ)
Paquete `com.hotelboutique.reservas.messaging`; la topología (beans `Queue`, `Exchange`, `Binding`) está en `config/RabbitTopologyConfig`.

| Cola | Exchange / routing key | Productor | Consumidor |
|---|---|---|---|
| `q.tickets` | `hotel.eventos` (topic) / `reserva.creada` | `ReservaEventPublisher` | `TicketConsumer` |
| `q.documentos` | `hotel.documentos` (direct) / `comprobante` | `ReservaEventPublisher` | `DocumentoConsumer` |
| `q.notificaciones` | `hotel.eventos` (topic) / `reserva.#` | `ReservaEventPublisher` | `NotificacionConsumer` (en ms-auth) |

- Cada cola tiene su DLQ (`q.*.dlq`) enlazada al DLX `hotel.dlx` (direct). `DlqConsumer` consume las DLQ de tickets y documentos y registra el mensaje y el motivo (`x-death`) en logs.
- ACK manual. Si el JSON está roto o los datos son inválidos: NACK sin requeue y pasa a la DLQ (`ReservaEventoParser` convierte el mensaje a mano porque con ACK manual el conversor de Spring falla antes de entrar al listener).
- Errores recuperables: 3 intentos con backoff; si persiste, DLQ.
- Retención: TTL 24 h y máx. 10.000 mensajes por cola principal.

## Streaming de eventos (Apache Kafka)
Paquete `com.hotelboutique.reservas.kafka`. Los tópicos se crean con `ms-kafka-admin` (`POST /topics/inicializar`).

| Tópico | Config | Clave del mensaje | Rol de este servicio |
|---|---|---|---|
| `hotel.reservas.eventos` | 3 particiones, factor de replicación 3, `min.insync.replicas=2`, `cleanup.policy=delete`, retención 7 días | `habitacionId` (los eventos de una habitación quedan en orden dentro de la misma partición) | Productor y consumidor |
| `hotel.usuarios.eventos` | 3 particiones, factor de replicación 3, `min.insync.replicas=2`, `cleanup.policy=compact` (conserva el último evento por usuario) | `email` | Solo consumidor |

### Productor
`ReservaKafkaListener` escucha el mismo `ReservaEvento` que ya usa RabbitMQ (`@TransactionalEventListener` después del commit) y entrega el evento a `ReservaKafkaPublisher`, que lo envía con `KafkaTemplate`.
Un fallo de Kafka solo se registra en logs: nunca afecta la reserva ni el flujo de RabbitMQ. `KAFKA_ENABLED=false` apaga Kafka por completo.

### Evento (DTO `ReservaStreamEvent`, serializado como JSON)
| Campo | Uso |
|---|---|
| `eventId` | Identifica el evento (trazabilidad) |
| `tipo` | `reserva.creada`, `reserva.checkin`, `reserva.checkout` o `reserva.cancelada` |
| `reservaId`, `usuarioEmail` | Qué reserva y de quién |
| `habitacionId` | Habitación afectada (clave de partición) |
| `fechaCheckin`, `fechaCheckout`, `estado` | Foto de la reserva (`CONFIRMADA`, `CHECK_IN`, `CHECK_OUT`, `CANCELADA`) |
| `ocurridoEn` | Instante del evento |

`UsuarioStreamEvent` (`eventId`, `tipo`, `email`, `nombre`, `ocurridoEn`) es la copia del lado consumidor del evento que publica ms-auth.

### Consumidores y consumer groups
| Consumer group | Clase (`@KafkaListener`) | Tópico | Qué hace |
|---|---|---|---|
| `reservas-ocupacion` | `OcupacionConsumer` | `hotel.reservas.eventos` | Actualiza en tiempo real la ocupación: `checkin` => `OCUPADA`; `checkout` y `cancelada` => `LIBRE`; `creada` solo se registra. La lógica vive en `OcupacionService`, separada de la mensajería |
| `reservas-usuarios-cache` | `UsuarioReferenciaConsumer` | `hotel.usuarios.eventos` | Mantiene una copia local de los usuarios publicados por ms-auth |

La ocupación se consulta en `GET /habitaciones/ocupacion` (solo ADMIN; el BFF lo expone igual).
Otros grupos pueden leer el mismo tópico de forma independiente (ms-auth lo hace con `auth-actividad-usuario`).

### Manejo de errores
- `ErrorHandlingDeserializer`: un JSON roto no bloquea la partición; no se reintenta, se descarta y se registra.
- `DefaultErrorHandler` (`KafkaErrorHandlingConfig`): 3 reintentos con espera creciente (1 s, 2 s, 4 s). Si se agotan, se registra en logs el tópico, la partición, el offset y la causa, y se sigue con el siguiente mensaje.
- Payload inválido (sin `tipo` o sin `reservaId`): se registra una advertencia y se avanza.
- Productor: `acks=all`, idempotencia activada y `max.block.ms=3000` para no bloquear la petición HTTP si Kafka está caído.
- Consumidor: `enable-auto-commit=false` y `ack-mode=record` (el offset se confirma después de procesar cada registro), `auto-offset-reset=earliest`.

## Variables de entorno (sin valores)
Copiar `.env.example` a `.env` (no versionado).

| Variable | Uso |
|---|---|
| `SPRING_PROFILES_ACTIVE` | `dev` (H2) o `prod` (PostgreSQL) |
| `DB_URL`, `DB_USER`, `DB_PASSWORD` | Conexión a PostgreSQL |
| `AWS_REGION`, `COGNITO_USER_POOL_ID` | Validación del JWT |
| `RABBITMQ_HOST`, `RABBITMQ_PORT`, `RABBITMQ_VHOST`, `RABBITMQ_USERNAME`, `RABBITMQ_PASSWORD` | Broker |
| `KAFKA_BOOTSTRAP_SERVERS` | Brokers Kafka (IP privada de la EC2 de Kafka, puertos 9092, 9093 y 9094; en local `localhost`) |
| `KAFKA_ENABLED` | `true` (por defecto) activa productores y consumidores Kafka; `false` los apaga |
| `DLQ_CONSUMIR` | `true` (por defecto) activa los listeners de DLQ; `false` los apaga |

## Ejecutar y probar
```bash
mvn clean test
mvn spring-boot:run
```