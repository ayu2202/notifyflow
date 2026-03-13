# Local Setup Guide

## Prerequisites

| Requirement    | Version | Check Command            |
|----------------|---------|--------------------------|
| Java (JDK)     | 21+     | `java -version`          |
| Maven          | 3.8+    | `mvn -version`           |
| Docker         | 20+     | `docker --version`       |
| Docker Compose | v2+     | `docker compose version` |

---

## Quick Start (Recommended)

One command to build and start everything:

```bash
git clone https://github.com/your-username/notifyflow.git
cd notifyflow
chmod +x scripts/*.sh

bash scripts/start-local.sh
```

Wait ~30 seconds for all services to become healthy, then test:

```bash
bash scripts/test-event.sh
```

---

## What Gets Started

The system starts 7 Docker containers:

| Container            | Port  | Purpose                   |
|----------------------|-------|---------------------------|
| RabbitMQ             | 5672  | Message broker            |
| RabbitMQ Management  | 15672 | Broker dashboard          |
| Zipkin               | 9411  | Distributed tracing       |
| Prometheus           | 9090  | Metrics collection        |
| Grafana              | 3000  | Metrics dashboards        |
| User Service         | 8081  | Event API                 |
| Notification Service | 8082  | Event consumer            |
| Gateway Service      | 8083  | API gateway (entry point) |

---

## Manual Setup

If you prefer running each step yourself:

### 1. Build All Services

```bash
bash scripts/build-all.sh
```

Or individually:

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

---

## Running Without Docker (Development)

### 1. Start RabbitMQ Only

```bash
docker run -d --name rabbitmq \
  -p 5672:5672 -p 15672:15672 \
  rabbitmq:3-management
```

### 2. Start Services in Separate Terminals

**Terminal 1 — User Service:**
```bash
cd services/user-service && mvn spring-boot:run
```

**Terminal 2 — Notification Service:**
```bash
cd services/notification-service && mvn spring-boot:run
```

**Terminal 3 — Gateway Service:**
```bash
cd services/gateway-service && mvn spring-boot:run
```

---

## Testing

### Automated

```bash
bash scripts/test-event.sh
```

### Manual

```bash
# System status
curl http://localhost:8083/demo/status

# Send demo event
curl -X POST http://localhost:8083/demo/event \
  -H "Content-Type: application/json" \
  -d '{"email":"demo@notifyflow.com","message":"Test notification"}'

# Verify notification delivery
docker logs notifyflow-notification-service | grep "Notification sent"
```

---

## Accessing UIs

| Service            | URL                                   | Credentials                       |
|--------------------|---------------------------------------|-----------------------------------|
| Swagger UI         | http://localhost:8083/swagger-ui.html | —                                 |
| RabbitMQ Dashboard | http://localhost:15672                 | See `infrastructure/.env.example` |
| Grafana            | http://localhost:3000                  | See `infrastructure/.env.example` |
| Prometheus         | http://localhost:9090                  | —                                 |
| Zipkin             | http://localhost:9411                  | —                                 |

---

## Stopping

```bash
bash scripts/stop-local.sh
```

To remove volumes and images:

```bash
docker compose -f infrastructure/docker-compose.yml down -v --rmi local
```
