# NotifyFlow

### Event-Driven Notification Microservices Platform

---

## Overview

NotifyFlow simulates how enterprise systems publish business events that trigger downstream notification services using asynchronous messaging.

The project demonstrates **event-driven architecture**, **microservices communication**, and **containerized deployment** — built with Spring Boot 3, Java 21, and RabbitMQ.

**This project showcases:**

- Microservices architecture with API gateway routing
- Event-driven messaging via RabbitMQ
- Container orchestration with Docker Compose
- Distributed system design with observability and resilience

---

## Architecture

![Architecture](diagrams/architecture.png)

```
                    ┌──────────────────────────────────────────────┐
                    │            NotifyFlow Platform               │
                    └──────────────────────────────────────────────┘

  ┌────────┐       ┌───────────────┐      ┌──────────────┐       ┌──────────────┐
  │        │ HTTP  │               │ HTTP │              │ AMQP  │              │
  │ Client │──────▶│    Gateway    │─────▶│    User      │──────▶│   RabbitMQ   │
  │        │       │    Service    │      │   Service    │       │              │
  └────────┘       │   (8083)     │      │   (8081)    │       └──────┬───────┘
                   └───────────────┘      └──────┬───────┘              │
                                                 │                     │ AMQP
                                                 ▼                     ▼
                                          ┌──────────────┐   ┌──────────────────┐
                                          │ H2 Database  │   │  Notification    │
                                          │ (in-memory)  │   │  Service (8082)  │
                                          └──────────────┘   └──────────────────┘

  ┌──────────────────────────────────────────────────────────────────────────────┐
  │                         Observability Layer                                  │
  │  Prometheus (9090) ──▶ Grafana (3000)         Zipkin (9411)                  │
  └──────────────────────────────────────────────────────────────────────────────┘
```

---

## System Flow

How events move through the platform:

```
1.  Client sends event request       →  POST /demo/event
2.  Gateway Service routes request   →  Forwards to User Service
3.  User Service stores event        →  Persists to H2 database
4.  User Service publishes event     →  Sends to RabbitMQ exchange
5.  RabbitMQ queues the message      →  Routes to events.queue
6.  Notification Service consumes    →  Reads from queue
7.  Notification is delivered        →  Logs notification confirmation
```

---

## Technology Stack

| Layer            | Technology                            |
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
| Build Tool       | Maven                                 |

---

## Quick Start

### Prerequisites

- Java 21 (JDK)
- Maven 3.8+
- Docker & Docker Compose

### Start the System

```bash
git clone https://github.com/your-username/notifyflow.git
cd notifyflow
chmod +x scripts/*.sh

bash scripts/start-local.sh
```

This builds all services and starts 7 containers — RabbitMQ, Zipkin, Prometheus, Grafana, and 3 microservices.

### Test the Event Pipeline

After the system starts (~30 seconds):

```bash
bash scripts/test-event.sh
```

This triggers the full event flow:

```
Gateway → User Service → RabbitMQ → Notification Service
```

### Stop the System

```bash
bash scripts/stop-local.sh
```

---

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

### Trigger a Notification Event

```bash
curl -X POST http://localhost:8083/demo/event \
  -H "Content-Type: application/json" \
  -d '{"email":"demo@notifyflow.com","message":"Hello from NotifyFlow"}'
```

```json
{
  "status": "success",
  "message": "Event sent and notification triggered",
  "event": {
    "id": 1,
    "eventType": "DEMO_EVENT",
    "userEmail": "demo@notifyflow.com",
    "message": "Hello from NotifyFlow"
  }
}
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

---

## Services

| Service              | Port | Role                                 |
|----------------------|------|--------------------------------------|
| Gateway Service      | 8083 | API gateway, routing, demo endpoints |
| User Service         | 8081 | Event creation, RabbitMQ publishing  |
| Notification Service | 8082 | Event consumption, notifications     |
| RabbitMQ             | 5672 | Async message broker (mgmt: 15672)   |

## Observability

| Tool       | URL                                   | Purpose                |
|------------|---------------------------------------|------------------------|
| Swagger UI | http://localhost:8083/swagger-ui.html | API documentation      |
| Grafana    | http://localhost:3000                  | Metrics dashboards     |
| Prometheus | http://localhost:9090                  | Metrics collection     |
| Zipkin     | http://localhost:9411                  | Distributed tracing    |
| RabbitMQ   | http://localhost:15672                 | Message broker console |

> Default credentials are in `infrastructure/.env.example`

---

## Live Demo

> When deployed to Render:

```bash
# Check system status
curl https://notifyflow-gateway.onrender.com/demo/status

# Send notification event
curl -X POST https://notifyflow-gateway.onrender.com/demo/event \
  -H "Content-Type: application/json" \
  -d '{"email":"demo@notifyflow.com","message":"Hello from the cloud"}'
```

See [deployment guide](docs/deployment-render.md) for setup instructions.

---

## Project Structure

```
notifyflow/
│
├── services/                          # Microservices
│   ├── gateway-service/               #   API gateway (Spring Cloud Gateway)
│   ├── user-service/                  #   Event creation + RabbitMQ publishing
│   └── notification-service/          #   Event consumer + notification delivery
│
├── infrastructure/                    # Container orchestration
│   ├── docker-compose.yml             #   Full stack (7 containers)
│   ├── prometheus.yml                 #   Metrics scrape config
│   ├── .env.example                   #   Environment variable template
│   └── provisioning/                  #   Grafana dashboards + datasources
│
├── docs/                              # Documentation
│   ├── architecture.md                #   System architecture
│   ├── system-flow.md                 #   Event flow walkthrough
│   ├── local-setup.md                 #   Local development guide
│   ├── deployment-render.md           #   Cloud deployment guide
│   └── demo-guide.md                  #   Demo walkthrough
│
├── scripts/                           # Automation
│   ├── start-local.sh                 #   Build + start system
│   ├── stop-local.sh                  #   Stop all containers
│   ├── test-event.sh                  #   Test event pipeline
│   └── build-all.sh                   #   Build all services
│
├── diagrams/                          # Architecture diagrams
│   └── architecture-diagram.md        #   Mermaid + text diagrams
│
├── postman/                           # API testing
│   └── notifyflow-collection.json     #   Postman collection
│
├── .gitignore
├── LICENSE
└── README.md
```

---

## Deployment

Each service deploys independently to [Render](https://render.com) as a Docker web service.

| Variable               | Example Value                                  |
|------------------------|------------------------------------------------|
| `PORT`                 | `8083`                                         |
| `SPRING_RABBITMQ_HOST` | `sparrow.rmq.cloudamqp.com`                   |
| `USER_SERVICE_URL`     | `https://notifyflow-user-service.onrender.com` |

See [docs/deployment-render.md](docs/deployment-render.md) for step-by-step instructions.

---

## Documentation

| Document                                                  | Description                      |
|-----------------------------------------------------------|----------------------------------|
| [Architecture](docs/architecture.md)                      | System design and components     |
| [System Flow](docs/system-flow.md)                        | Event lifecycle walkthrough      |
| [Local Setup](docs/local-setup.md)                        | Running locally with Docker      |
| [Deployment](docs/deployment-render.md)                   | Deploying to Render              |
| [Demo Guide](docs/demo-guide.md)                          | Testing with curl examples       |
| [Architecture Diagram](diagrams/architecture-diagram.md)  | Mermaid + text diagrams          |

---

## License

[MIT](LICENSE)
