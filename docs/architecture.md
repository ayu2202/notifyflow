# Architecture

## System Overview

NotifyFlow is an **event-driven notification platform** that demonstrates how enterprise systems publish business events which trigger downstream notification services through asynchronous messaging.

The platform is composed of three microservices communicating via HTTP and AMQP (RabbitMQ), with a full observability stack for metrics, tracing, and dashboards.

---

## Architecture Diagram

```
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
```

---

## Service Responsibilities

### Gateway Service — port 8083

| Aspect         | Detail                                                 |
|----------------|--------------------------------------------------------|
| Role           | API gateway and single entry point for all clients     |
| Technology     | Spring Cloud Gateway (WebFlux)                         |
| Key Endpoints  | `GET /demo/status`, `POST /demo/event`, `POST /api/events` |
| Responsibilities | Route requests to downstream services, provide demo endpoints, expose Swagger UI, emit tracing spans |

### User Service — port 8081

| Aspect         | Detail                                                 |
|----------------|--------------------------------------------------------|
| Role           | Event creation and publishing                          |
| Technology     | Spring Boot, Spring Data JPA, Spring AMQP              |
| Key Endpoint   | `POST /api/events`                                     |
| Responsibilities | Accept events via REST API, persist to H2 database, publish to RabbitMQ with retry + circuit breaker |

### Notification Service — port 8082

| Aspect         | Detail                                                 |
|----------------|--------------------------------------------------------|
| Role           | Event consumption and notification delivery            |
| Technology     | Spring Boot, Spring AMQP                               |
| Listener       | `@RabbitListener` on `events.queue`                    |
| Responsibilities | Consume events from RabbitMQ, process events, log notification delivery |

### RabbitMQ — port 5672

| Aspect       | Detail                       |
|--------------|------------------------------|
| Exchange     | `events.exchange` (Topic)    |
| Queue        | `events.queue` (Durable)     |
| Routing Key  | `events.key`                 |
| Serialization| Jackson JSON                 |

---

## Observability Stack

| Tool       | Port | Purpose                                          |
|------------|------|--------------------------------------------------|
| Prometheus | 9090 | Scrapes `/actuator/prometheus` from all services  |
| Grafana    | 3000 | Pre-provisioned dashboards for HTTP and JVM metrics |
| Zipkin     | 9411 | Collects distributed traces across all services   |

All three services emit metrics and tracing data automatically via Micrometer.

---

## Resilience Patterns

Applied to the RabbitMQ publishing path in User Service:

| Pattern         | Configuration                                     |
|-----------------|---------------------------------------------------|
| **Retry**       | 3 attempts, exponential backoff (500ms base)      |
| **Circuit Breaker** | Opens at 50% failure rate, 10s wait in open state |

When the circuit breaker opens, a fallback method logs the failure and prevents cascading errors.

---

## Design Decisions

| Decision                          | Rationale                                                    |
|-----------------------------------|--------------------------------------------------------------|
| Spring Cloud Gateway (WebFlux)    | Non-blocking gateway for high throughput                     |
| RabbitMQ over Kafka               | Simpler setup for point-to-point notification delivery       |
| H2 in-memory database             | Zero-config, ideal for demonstration — swap for PostgreSQL in production |
| Resilience4j over Hystrix         | Modern, lightweight, actively maintained                     |
| Docker Compose for orchestration  | Single-command local environment for 7 containers            |
| Environment variable configuration| Credentials externalized — ready for cloud deployment        |
