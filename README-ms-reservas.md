# ms-reservation-hotel-boutique

Microservicio de reservas y habitaciones (puerto 8082) de AndesStay / Hotel Boutique (DSY1107, EP2).
Aplica las reglas de negocio, persiste en PostgreSQL (Neon) y publica/consume mensajes en RabbitMQ.

## Requisitos
- Java 21, Maven 3.9+
- RabbitMQ (clúster de 2 nodos, ver repo `ms-rabbit-admin`, carpeta `infra/`)
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

## Variables de entorno (sin valores)
Copiar `.env.example` a `.env` (no versionado).

| Variable | Uso |
|---|---|
| `SPRING_PROFILES_ACTIVE` | `dev` (H2) o `prod` (PostgreSQL) |
| `DB_URL`, `DB_USER`, `DB_PASSWORD` | Conexión a PostgreSQL |
| `AWS_REGION`, `COGNITO_USER_POOL_ID` | Validación del JWT |
| `RABBITMQ_HOST`, `RABBITMQ_PORT`, `RABBITMQ_VHOST`, `RABBITMQ_USERNAME`, `RABBITMQ_PASSWORD` | Broker |
| `DLQ_CONSUMIR` | `true` (por defecto) activa los listeners de DLQ; `false` los apaga |

## Ejecutar y probar
```bash
mvn clean test
mvn spring-boot:run
```
