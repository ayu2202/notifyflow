# Demo Guide

## Overview

NotifyFlow provides simplified demo endpoints so you can test the full system without knowing the internal API structure.

## Prerequisites

Make sure the system is running:

```bash
bash scripts/start-local.sh
```

Wait until all services are healthy (~30 seconds).

## Step 1: Check System Status

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

## Step 2: Send a Demo Event

```bash
curl -X POST http://localhost:8083/demo/event \
  -H "Content-Type: application/json" \
  -d '{
    "email": "demo@notifyflow.com",
    "message": "Hello from NotifyFlow demo"
  }'
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

## Step 3: Verify Notification Delivery

Check the Notification Service logs:

```bash
docker logs notifyflow-notification-service | grep "Notification sent"
```

**Expected output:**

```
Notification sent to demo@notifyflow.com: Hello from NotifyFlow demo
```

## Step 4: Use the Automated Test Script

Run everything above in one command:

```bash
bash scripts/test-event.sh
```

## Full API Usage

For the complete event API (bypass demo endpoints):

```bash
curl -X POST http://localhost:8083/api/events \
  -H "Content-Type: application/json" \
  -d '{
    "eventType": "TASK_ASSIGNED",
    "userEmail": "operator1@notifyflow.com",
    "message": "New task assigned to you"
  }'
```

## API Documentation

Interactive Swagger UI is available while the system is running:

| Service          | URL                                   |
|------------------|---------------------------------------|
| Gateway API      | http://localhost:8083/swagger-ui.html |
| User Service API | http://localhost:8081/swagger-ui.html |

## Monitoring

After sending events, explore the observability stack:

| Tool       | URL                    | What to Look For                      |
|------------|------------------------|---------------------------------------|
| Grafana    | http://localhost:3000  | HTTP request rate, JVM metrics         |
| Zipkin     | http://localhost:9411  | Distributed traces across services     |
| Prometheus | http://localhost:9090  | Raw metrics and PromQL queries         |

### Viewing Traces in Zipkin

1. Open http://localhost:9411
2. Click **Run Query**
3. Select a trace to see the full request flow:
   ```
   gateway-service → user-service → notification-service
   ```

### Viewing Metrics in Grafana

1. Open http://localhost:3000 (admin / admin)
2. Navigate to **Dashboards** → **NotifyFlow Overview**
3. See real-time HTTP request rates, latency, and JVM memory usage
