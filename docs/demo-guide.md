# Demo Guide

## Overview

NotifyFlow provides simplified demo endpoints so you can test the full event pipeline without knowing the internal API details.

---

## Prerequisites

Start the system:

```bash
bash scripts/start-local.sh
```

Wait ~30 seconds for all services to become healthy.

---

## Step 1 — Check System Status

```bash
curl http://localhost:8083/demo/status
```

**Expected response:**

```json
{
  "system": "NotifyFlow",
  "status": "running",
  "services": [
    "gateway-service",
    "user-service",
    "notification-service",
    "rabbitmq"
  ]
}
```

---

## Step 2 — Send a Demo Event

```bash
curl -X POST http://localhost:8083/demo/event \
  -H "Content-Type: application/json" \
  -d '{"email":"demo@notifyflow.com","message":"Hello from NotifyFlow demo"}'
```

**Expected response:**

```json
{
  "status": "success",
  "message": "Event sent and notification triggered",
  "event": {
    "id": 1,
    "eventType": "DEMO_EVENT",
    "userEmail": "demo@notifyflow.com",
    "message": "Hello from NotifyFlow demo",
    "createdAt": "2026-03-14T12:30:45.123456"
  }
}
```

**What happened behind the scenes:**

```
Client → Gateway Service → User Service → H2 Database
                                        → RabbitMQ → Notification Service
```

---

## Step 3 — Verify Notification Delivery

Check the Notification Service logs:

```bash
docker logs notifyflow-notification-service | grep "Notification sent"
```

**Expected output:**

```
Notification sent to demo@notifyflow.com: Hello from NotifyFlow demo
```

---

## Step 4 — Automated Test Script

Run all of the above in one command:

```bash
bash scripts/test-event.sh
```

This script:
1. Checks system status
2. Sends a demo event
3. Verifies notification delivery in logs

---

## Full Event API

For the complete event API (bypassing demo endpoints):

```bash
curl -X POST http://localhost:8083/api/events \
  -H "Content-Type: application/json" \
  -d '{
    "eventType": "TASK_ASSIGNED",
    "userEmail": "operator1@notifyflow.com",
    "message": "New task assigned to you"
  }'
```

---

## API Documentation

Interactive Swagger UI is available while the system is running:

| Service          | URL                                   |
|------------------|---------------------------------------|
| Gateway API      | http://localhost:8083/swagger-ui.html |
| User Service API | http://localhost:8081/swagger-ui.html |

---

## Monitoring the Demo

After sending events, explore the observability stack:

### Traces in Zipkin

1. Open http://localhost:9411
2. Click **Run Query**
3. Select a trace to see the request flow across services:
   ```
   gateway-service → user-service → notification-service
   ```

### Metrics in Grafana

1. Open http://localhost:3000 (credentials in `infrastructure/.env.example`)
2. Navigate to **Dashboards** → **NotifyFlow Overview**
3. View HTTP request rates, latency percentiles, and JVM memory usage

### Raw Metrics in Prometheus

1. Open http://localhost:9090
2. Try queries like:
   - `http_server_requests_seconds_count` — request counts
   - `jvm_memory_used_bytes` — memory usage

---

## Live Demo (Render)

If deployed to Render:

```bash
# System status
curl https://notifyflow-gateway.onrender.com/demo/status

# Send event
curl -X POST https://notifyflow-gateway.onrender.com/demo/event \
  -H "Content-Type: application/json" \
  -d '{"email":"demo@notifyflow.com","message":"Hello from the cloud!"}'
```
