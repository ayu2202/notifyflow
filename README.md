# NotifyFlow

**Event-driven notification microservices system** built with Spring Boot 3, Java 21, RabbitMQ, and Spring Cloud Gateway.

```
Client → Gateway Service → User Service → RabbitMQ → Notification Service
```

---

## Overview

NotifyFlow demonstrates a production-grade microservices architecture with asynchronous event processing, API gateway routing, resilience patterns, distributed tracing, and metrics monitoring — all containerized with Docker Compose.

A client sends an event through the **Gateway**, which routes it to the **User Service**. The event is persisted and published to **RabbitMQ**, where the **Notification Service** consumes it and delivers the notification.

## Architecture

```
┌────────┐     ┌─────────────┐     ┌──────────────┐     ┌──────────┐     ┌───────────────────┐
│ Client │────▶│   Gateway   │────▶│ User Service │────▶│ RabbitMQ │────▶│ Notification Svc  │
│        │     │   (8083)    │     │   (8081)     │     │  (5672)  │     │     (8082)        │
└────────┘     └─────────────┘     └──────┬───────┘     └──────────┘     └───────────────────┘
                                          │
                                   ┌──────┴──────┐
                                   │ H2 Database  │
                                   └─────────────┘
```

## Technology Stack

| Component        | Technology                            |
|------------------|---------------------------------------|
| Language         | Java 21                               |
| Framework        | Spring Boot 3.4.3                     |
| API Gateway      | Spring Cloud Gateway (WebFlux)        |
| Messaging        | RabbitMQ + Spring AMQP                |
| Database         | H2 (in-memory)                        |
| Resilience       | Resilience4j (retry, circuit breaker) |
| Metrics          | Micrometer + Prometheus + Grafana     |
| Tracing          | Micrometer Tracing + Zipkin           |
| API Docs         | SpringDoc OpenAPI (Swagger UI)        |
| Containerization | Docker + Docker Compose               |

## Quick Start

### Prerequisites

- Java 21 (JDK)
- Maven 3.8+
- Docker & Docker Compose

### Run Locally

```bash
git clone https://github.com/your-username/notifyflow.git
cd notifyflow

# Make scripts executable
chmod +x scripts/*.sh

# Build and start the entire system
bash scripts/start-local.sh
```

This builds all services and starts 7 containers (RabbitMQ, Zipkin, Prometheus, Grafana, and 3 microservices).

### Test the System

```bash
bash scripts/test-event.sh
```

Or manually:

```bash
# Check system status
curl http://localhost:8083/demo/status

# Send a demo event
curl -X POST http://localhost:8083/demo/event \
  -H "Content-Type: application/json" \
  -d '{"email":"demo@notifyflow.com","message":"Hello from NotifyFlow"}'
```

### Stop

```bash
bash scripts/stop-local.sh
```

## API Examples

### Check System Status

```bash
curl http://localhost:8083/demo/status
```

```json
{
  "system": "NotifyFlow",
  "status": "running",
  "services": ["gateway-service", "user-service", "notification-service", "rabbitmq"]
}
```

### Send Notification Event

```bash
curl -X POST http://localhost:8083/demo/event \
  -H "Content-Type: application/json" \
  -d '{"email":"demo@notifyflow.com","message":"Hello from NotifyFlow"}'
```

### Full Event API

```bash
curl -X POST http://localhost:8083/api/events \
  -H "Content-Type: application/json" \
  -d '{
    "eventType": "TASK_ASSIGNED",
    "userEmail": "operator1@notifyflow.com",
    "message": "New task assigned"
  }'
```

## Services

| Service              | Port | Description                          |
|----------------------|------|--------------------------------------|
| Gateway Service      | 8083 | API gateway, routing, demo endpoints |
| User Service         | 8081 | Event creation, RabbitMQ publishing  |
| Notification Service | 8082 | Event consumption, notifications     |
| RabbitMQ             | 5672 | Message broker (mgmt: 15672)         |

## Observability

| Tool       | URL                                   | Credentials                  |
|------------|---------------------------------------|------------------------------|
| Swagger UI | http://localhost:8083/swagger-ui.html | —                            |
| Grafana    | http://localhost:3000                  | See `infrastructure/.env.example` |
| Prometheus | http://localhost:9090                  | —                            |
| Zipkin     | http://localhost:9411                  | —                            |
| RabbitMQ   | http://localhost:15672                 | See `infrastructure/.env.example` |

## Project Structure

```
notifyflow/
│
├── services/
│   ├── gateway-service/          # Spring Cloud Gateway — API entry point
│   │   ├── src/main/java/
│   │   │   ├── controller/       # Demo + health endpoints
│   │   │   ├── service/          # Demo event forwarding (WebClient)
│   │   │   ├── entity/           # Request/response DTOs
│   │   │   └── config/           # Gateway routes, OpenAPI
│   │   ├── Dockerfile
│   │   └── pom.xml
│   │
│   ├── user-service/             # Event creation + RabbitMQ publishing
│   │   ├── src/main/java/
│   │   │   ├── controller/       # REST API (POST /api/events)
│   │   │   ├── service/          # Business logic + Resilience4j
│   │   │   ├── entity/           # Event JPA entity
│   │   │   ├── repository/       # Spring Data JPA
│   │   │   └── config/           # RabbitMQ, OpenAPI
│   │   ├── Dockerfile
│   │   └── pom.xml
│   │
│   └── notification-service/     # Event consumer + notification delivery
│       ├── src/main/java/
│       │   ├── controller/       # Health endpoint
│       │   ├── service/          # RabbitMQ listener
│       │   ├── entity/           # Event message DTO
│       │   └── config/           # RabbitMQ bindings
│       ├── Dockerfile
│       └── pom.xml
│
├── infrastructure/
│   ├── docker-compose.yml        # Full stack: 7 containers
│   ├── prometheus.yml            # Prometheus scrape config
│   ├── grafana-dashboards/       # Pre-built Grafana dashboards
│   └── provisioning/             # Grafana datasource + dashboard provisioning
│
├── docs/
│   ├── architecture.md           # System architecture overview
│   ├── system-flow.md            # Event flow walkthrough
│   ├── local-setup.md            # Local development guide
│   ├── deployment-render.md      # Render deployment guide
│   └── demo-guide.md             # Demo walkthrough with curl examples
│
├── scripts/
│   ├── start-local.sh            # Build + start entire system
│   ├── stop-local.sh             # Stop all containers
│   ├── test-event.sh             # Send demo event + verify
│   └── build-all.sh              # Build all services
│
├── diagrams/
│   └── architecture-diagram.md   # Mermaid + text architecture diagrams
│
├── postman/
│   └── notifyflow-collection.json # Postman API collection
│
├── .gitignore
├── LICENSE
└── README.md
```

## Deployment

Each service can be deployed independently to [Render](https://render.com) as a Docker web service. See [docs/deployment-render.md](docs/deployment-render.md) for step-by-step instructions.

**Environment variables:**

| Variable               | Example Value                                  |
|------------------------|------------------------------------------------|
| `PORT`                 | `8083`                                         |
| `SPRING_RABBITMQ_HOST` | `sparrow.rmq.cloudamqp.com`                   |
| `USER_SERVICE_URL`     | `https://notifyflow-user-service.onrender.com` |

## Documentation

| Document                                              | Description                      |
|-------------------------------------------------------|----------------------------------|
| [Architecture](docs/architecture.md)                  | System design and components     |
| [System Flow](docs/system-flow.md)                    | How events move through services |
| [Local Setup](docs/local-setup.md)                    | Running locally with Docker      |
| [Deployment](docs/deployment-render.md)               | Deploying to Render              |
| [Demo Guide](docs/demo-guide.md)                      | Testing with curl examples       |
| [Architecture Diagram](diagrams/architecture-diagram.md) | Visual system diagrams        |

## License

[MIT](LICENSE)
