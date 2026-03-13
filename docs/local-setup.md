# Local Setup Guide

## Prerequisites

| Requirement        | Version |
|--------------------|---------|
| Java (JDK)         | 21+     |
| Maven              | 3.8+    |
| Docker             | 20+     |
| Docker Compose     | v2+     |

## Quick Start

The fastest way to run NotifyFlow locally:

```bash
git clone https://github.com/your-username/notifyflow.git
cd notifyflow

# Build and start everything (one command)
bash scripts/start-local.sh
```

This builds all three services and starts 7 Docker containers:

| Container            | Port  | Purpose                  |
|----------------------|-------|--------------------------|
| RabbitMQ             | 5672  | Message broker           |
| RabbitMQ Management  | 15672 | Broker dashboard         |
| Zipkin               | 9411  | Distributed tracing      |
| Prometheus           | 9090  | Metrics collection       |
| Grafana              | 3000  | Metrics dashboards       |
| User Service         | 8081  | Event API                |
| Notification Service | 8082  | Event consumer           |
| Gateway Service      | 8083  | API gateway (entry point)|

## Step-by-Step Setup

If you prefer to run steps manually:

### 1. Build All Services

```bash
bash scripts/build-all.sh
```

Or build individually:

```bash
cd services/user-service && mvn clean package -DskipTests
cd services/notification-service && mvn clean package -DskipTests
cd services/gateway-service && mvn clean package -DskipTests
```

### 2. Start the System

```bash
docker compose -f infrastructure/docker-compose.yml up --build
```

### 3. Verify

```bash
curl http://localhost:8083/demo/status
curl http://localhost:8083/actuator/health
```

## Running Without Docker

### 1. Start RabbitMQ Only

```bash
docker run -d --name rabbitmq \
  -p 5672:5672 -p 15672:15672 \
  rabbitmq:3-management
```

### 2. Start Services (3 terminal windows)

**Terminal 1 — User Service:**
```bash
cd services/user-service
mvn spring-boot:run
```

**Terminal 2 — Notification Service:**
```bash
cd services/notification-service
mvn spring-boot:run
```

**Terminal 3 — Gateway Service:**
```bash
cd services/gateway-service
mvn spring-boot:run
```

## Testing

Run the demo test script:

```bash
bash scripts/test-event.sh
```

Or test manually:

```bash
# Check system status
curl http://localhost:8083/demo/status

# Send a demo event
curl -X POST http://localhost:8083/demo/event \
  -H "Content-Type: application/json" \
  -d '{"email":"demo@notifyflow.com","message":"Test notification"}'

# Check notification logs
docker logs notifyflow-notification-service | grep "Notification sent"
```

## Accessing UIs

| Service            | URL                                   | Credentials                          |
|--------------------|---------------------------------------|--------------------------------------|
| Swagger UI         | http://localhost:8083/swagger-ui.html | —                                    |
| RabbitMQ Dashboard | http://localhost:15672                 | See `infrastructure/.env.example`    |
| Grafana            | http://localhost:3000                  | See `infrastructure/.env.example`    |
| Prometheus         | http://localhost:9090                  | —                                    |
| Zipkin             | http://localhost:9411                  | —                                    |

## Stopping

```bash
bash scripts/stop-local.sh
```

To remove volumes and images:

```bash
docker compose -f infrastructure/docker-compose.yml down -v --rmi local
```
