# System Flow

## Event Lifecycle

This document describes how events travel through the NotifyFlow platform — from client request to notification delivery.

---

## Visual Flow

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

---

## Step-by-Step Breakdown

### Step 1 — Client Sends Request

The client sends a POST request to the Gateway Service:

```bash
curl -X POST http://localhost:8083/demo/event \
  -H "Content-Type: application/json" \
  -d '{"email":"demo@notifyflow.com","message":"Hello from NotifyFlow"}'
```

### Step 2 — Gateway Routes Request

The Gateway Service receives the request and forwards it:

| Path          | Behavior                                                           |
|---------------|--------------------------------------------------------------------|
| `/demo/event` | `DemoService` transforms the payload, calls User Service via `WebClient` |
| `/api/events` | Spring Cloud Gateway routes directly to `user-service:8081/api/events`   |

### Step 3 — User Service Processes Event

The User Service handles the event in two phases:

**Persist phase:**
1. `EventController` receives the request
2. `EventService.createEvent()` saves the event to the H2 database

**Publish phase:**
3. `EventService.publishToRabbitMQ()` sends the event to RabbitMQ
   - **Retry:** On failure, Resilience4j retries up to 3 times with exponential backoff
   - **Circuit Breaker:** If failures exceed 50%, the circuit opens and calls route to `publishFallback()`

### Step 4 — RabbitMQ Delivers Message

RabbitMQ processes the event:

1. Receives the message on `events.exchange` with routing key `events.key`
2. Routes the message to `events.queue` based on the topic binding
3. Message is serialized as JSON using `Jackson2JsonMessageConverter`

### Step 5 — Notification Service Consumes Event

The Notification Service completes the flow:

1. `NotificationService` has a `@RabbitListener` on `events.queue`
2. Deserializes the message into an `EventMessage` object
3. Logs the notification: `Notification sent to {email}: {message}`

### Step 6 — Observability (Throughout)

Every step in the flow is observable:

| What              | How                                                        |
|-------------------|------------------------------------------------------------|
| Distributed traces| Trace ID propagates across all services → visible in Zipkin |
| Metrics           | Each service exposes `/actuator/prometheus`                 |
| Structured logs   | Every log entry includes `TRACE_ID` and `SPAN_ID`          |

---

## Error Handling

| Scenario               | Behavior                                                       |
|------------------------|----------------------------------------------------------------|
| RabbitMQ unavailable   | Retry 3 times → circuit breaker opens → fallback logs error    |
| User Service down      | Gateway returns 503 Service Unavailable                        |
| Invalid payload        | User Service returns 400 Bad Request                           |
| Queue consumer fails   | RabbitMQ requeues the message for redelivery                   |

---

## Verifying the Flow

After triggering an event, verify each step:

```bash
# 1. Send event
curl -X POST http://localhost:8083/demo/event \
  -H "Content-Type: application/json" \
  -d '{"email":"demo@notifyflow.com","message":"Test"}'

# 2. Check notification logs
docker logs notifyflow-notification-service | grep "Notification sent"

# 3. View trace in Zipkin
open http://localhost:9411
```
