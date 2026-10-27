# ms-pedidos360-rabbit-admin

Microservicio administrador de RabbitMQ de Pedidos360 (puerto **8083**). Permite crear y eliminar
colas, exchanges y bindings en tiempo de ejecución. El BFF expone estas mismas rutas a través de
`/api/rabbit-admin/**` (solo rol `ADMIN`).

## Configuración

| Variable | Default | Descripción |
|---|---|---|
| `SERVER_PORT` | `8083` | Puerto HTTP |
| `RABBITMQ_HOST` | `localhost` | Host de RabbitMQ |
| `RABBITMQ_PORT` | `5672` | Puerto AMQP |
| `RABBITMQ_USER` / `RABBITMQ_PASS` | `guest` / `guest` | Credenciales |

## Endpoints (`/api/rabbit-admin`)

| Método | Ruta | Descripción | Respuestas |
|---|---|---|---|
| POST | `/queues` | Crear cola `{"name":"...","durable":true}` | 201, 400 |
| DELETE | `/queues/{name}` | Eliminar cola | 200, 404 |
| POST | `/exchanges` | Crear exchange `{"name":"...","type":"direct\|topic\|fanout"}` | 201, 400 |
| DELETE | `/exchanges/{name}` | Eliminar exchange | 200, 404 |
| POST | `/bindings` | Crear binding `{"queueName":"...","exchangeName":"...","routingKey":"..."}` | 201, 400, 404 |
| DELETE | `/bindings?queueName=&exchangeName=&routingKey=` | Eliminar binding | 200, 400 |

Errores: `400` validación de parámetros (detalle por campo en `detalles`), `404/409/502` rechazo del
broker, `503` RabbitMQ no disponible.

Documentación interactiva (OpenAPI): `http://localhost:8083/swagger-ui.html`

## Estructura

- `controller/` endpoints REST · `service/RabbitAdminService` lógica de administración
- `dto/` DTOs con validaciones · `config/RabbitAdminConfig` bean `AmqpAdmin` · `exception/` manejo de errores
