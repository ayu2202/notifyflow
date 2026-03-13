# NotifyFlow Architecture

## System Overview

NotifyFlow is an event-driven notification microservices system built with **Spring Boot 3**, **Java 21**, and **RabbitMQ**. It demonstrates production-grade patterns including API gateway routing, asynchronous messaging, resilience, observability, and structured logging.

## Architecture Diagram

```
┌──────────────┐       ┌──────────────────┐       ┌──────────────┐       ┌────────────────────────┐
│              │       │                  │       │              │       │                        │
│    Client    │──────▶│  Gateway Service │──────▶│ User Service │──────▶│       RabbitMQ         │
│              │       │    (port 8083)   │       │  (port 8081) │       │    (port 5672)         │
│              │       │                  │       │              │       │                        │
└──────────────┘       └──────────────────┘       └──────────────┘       └───────────┬────────────┘
                                                                                     │
                                                                                     ▼
                                                                        ┌────────────────────────┐
                                                                        │                        │
                                                                        │ Notification Service   │
                                                                        │     (port 8082)        │
                                                                        │                        │
                                                                        └────────────────────────┘
```

## Services

### Gateway Service (port 8083)

- **Role:** API gateway and single entry point for all clients
- **Technology:** Spring Cloud Gateway (WebFlux)
- **Responsibilities:**
  - Route HTTP requests to downstream microservices
  - Provide demo endpoints (`/demo/status`, `/demo/event`) for quick testing
  - Expose aggregated Swagger UI for API documentation
  - Emit distributed tracing spans

### User Service (port 8081)

- **Role:** Event creation and publishing
- **Technology:** Spring Boot, Spring Data JPA, Spring AMQP
- **Responsibilities:**
  - Accept event creation requests via REST API (`POST /api/events`)
  - Persist events in H2 in-memory database
  - Publish events to RabbitMQ exchange with Resilience4j retry and circuit breaker
  - Expose Swagger UI for its own API documentation

### Notification Service (port 8082)

- **Role:** Event consumption and notification delivery
- **Technology:** Spring Boot, Spring AMQP
- **Responsibilities:**
  - Listen to RabbitMQ queue for incoming events
  - Process events and simulate notification delivery
  - Log notification status with structured logging

### RabbitMQ

- **Role:** Asynchronous message broker
- **Exchange:** `events.exchange` (Topic)
- **Queue:** `events.queue` (Durable)
- **Routing Key:** `events.key`

## Observability Stack

| Tool       | Port | Purpose                |
|------------|------|------------------------|
| Prometheus | 9090 | Metrics collection     |
| Grafana    | 3000 | Metrics visualization  |
| Zipkin     | 9411 | Distributed tracing    |

## Resilience Patterns

| Pattern         | Configuration                                        |
|-----------------|------------------------------------------------------|
| Retry           | 3 attempts, exponential backoff (500ms base)         |
| Circuit Breaker | Opens at 50% failure rate, 10s wait in open state    |

Both patterns are applied to the RabbitMQ publishing path in User Service.
