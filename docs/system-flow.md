# System Flow

## Event Lifecycle

This document describes how events flow through the NotifyFlow system from client request to notification delivery.

## Flow Diagram

```
Client (HTTP)
    │
    ▼
┌─────────────────────┐
│   Gateway Service    │   Receives POST /demo/event or POST /api/events
│     (port 8083)      │   Routes request to User Service
└──────────┬──────────┘
           │ HTTP
           ▼
┌─────────────────────┐
│    User Service      │   1. Validates the event payload
│     (port 8081)      │   2. Persists event to H2 database
│                      │   3. Publishes event to RabbitMQ
└──────────┬──────────┘
           │ AMQP
           ▼
┌─────────────────────┐
│      RabbitMQ        │   Exchange: events.exchange (Topic)
│     (port 5672)      │   Queue:    events.queue (Durable)
│                      │   Key:      events.key
└──────────┬──────────┘
           │ AMQP
           ▼
┌─────────────────────┐
│ Notification Service │   1. Consumes message from queue
│     (port 8082)      │   2. Processes event
│                      │   3. Logs notification delivery
└─────────────────────┘
```

## Step-by-Step Breakdown

### 1. Client Sends Request

The client sends a POST request to the Gateway Service:

```bash
curl -X POST http://localhost:8083/demo/event \
  -H "Content-Type: application/json" \
  -d '{"email":"demo@notifyflow.com","message":"Hello from NotifyFlow"}'
```

### 2. Gateway Routes Request

The Gateway Service receives the request and:
- For `/demo/event`: The `DemoService` transforms the simplified payload and calls User Service internally via `WebClient`
- For `/api/events`: Spring Cloud Gateway routes the request directly to `http://user-service:8081/api/events`

### 3. User Service Processes Event

The User Service:
1. Receives the event payload via `EventController`
2. `EventService.createEvent()` persists the event to the H2 database
3. `EventService.publishToRabbitMQ()` publishes the event to RabbitMQ
   - **Retry:** If publishing fails, Resilience4j retries up to 3 times with exponential backoff
   - **Circuit Breaker:** If failures exceed 50%, the circuit opens and calls go to `publishFallback()`

### 4. RabbitMQ Delivers Message

RabbitMQ:
1. Receives the message on `events.exchange` with routing key `events.key`
2. Routes the message to `events.queue` based on the binding
3. Message is serialized as JSON using `Jackson2JsonMessageConverter`

### 5. Notification Service Consumes Event

The Notification Service:
1. `NotificationService` has a `@RabbitListener` on `events.queue`
2. Deserializes the message into an `EventMessage` object
3. Logs the simulated notification: `Notification sent to {email}: {message}`

### 6. Observability

Throughout the flow:
- **Distributed tracing:** Each request generates a trace ID that propagates across all services (visible in Zipkin)
- **Metrics:** Each service exposes Prometheus metrics at `/actuator/prometheus`
- **Structured logging:** Every log entry includes `TRACE_ID` and `SPAN_ID` for correlation

## Error Handling

| Scenario               | Behavior                                                  |
|------------------------|-----------------------------------------------------------|
| RabbitMQ unavailable   | Retry 3 times → Circuit breaker opens → Fallback logs error |
| User Service down      | Gateway returns 503 Service Unavailable                   |
| Invalid payload        | User Service returns 400 Bad Request                      |
| Queue consumer fails   | RabbitMQ requeues the message for redelivery              |
