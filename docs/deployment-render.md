# Deploying NotifyFlow on Render

## Overview

Each NotifyFlow microservice deploys as an independent **Docker web service** on [Render](https://render.com). RabbitMQ is hosted externally using [CloudAMQP](https://www.cloudamqp.com/) (free tier available).

---

## Prerequisites

- [Render](https://render.com) account
- [CloudAMQP](https://www.cloudamqp.com/) account (or any hosted RabbitMQ)
- This repository pushed to GitHub

---

## Step 1 — Set Up RabbitMQ

1. Go to [CloudAMQP](https://www.cloudamqp.com/) and create a free instance (Little Lemur plan)
2. Note down your connection details:
   - **Host** (e.g., `sparrow.rmq.cloudamqp.com`)
   - **Port** (`5672`)
   - **Username**
   - **Password**

---

## Step 2 — Deploy User Service

1. Render Dashboard → **New** → **Web Service**
2. Connect your GitHub repository
3. Configure:

| Setting        | Value                    |
|----------------|--------------------------|
| Name           | `notifyflow-user-service`|
| Root Directory | `services/user-service`  |
| Runtime        | Docker                   |
| Instance Type  | Free                     |

4. Environment variables:

| Variable                   | Value                  |
|----------------------------|------------------------|
| `PORT`                     | `8081`                 |
| `SPRING_RABBITMQ_HOST`     | your CloudAMQP host    |
| `SPRING_RABBITMQ_PORT`     | `5672`                 |
| `SPRING_RABBITMQ_USERNAME` | your CloudAMQP user    |
| `SPRING_RABBITMQ_PASSWORD` | your CloudAMQP pass    |

5. Health Check Path: `/actuator/health`

---

## Step 3 — Deploy Notification Service

1. **New** → **Web Service**
2. Configure:

| Setting        | Value                             |
|----------------|-----------------------------------|
| Name           | `notifyflow-notification-service` |
| Root Directory | `services/notification-service`   |
| Runtime        | Docker                            |
| Instance Type  | Free                              |

3. Same RabbitMQ environment variables as User Service
4. Health Check Path: `/actuator/health`

---

## Step 4 — Deploy Gateway Service

1. **New** → **Web Service**
2. Configure:

| Setting        | Value                       |
|----------------|-----------------------------|
| Name           | `notifyflow-gateway`        |
| Root Directory | `services/gateway-service`  |
| Runtime        | Docker                      |
| Instance Type  | Free                        |

3. Environment variables:

| Variable           | Value                                              |
|--------------------|----------------------------------------------------|
| `PORT`             | `8083`                                             |
| `USER_SERVICE_URL` | `https://notifyflow-user-service.onrender.com`     |

4. Health Check Path: `/actuator/health`

---

## Step 5 — Verify Deployment

```bash
# System status
curl https://notifyflow-gateway.onrender.com/demo/status

# Send test event
curl -X POST https://notifyflow-gateway.onrender.com/demo/event \
  -H "Content-Type: application/json" \
  -d '{"email":"demo@notifyflow.com","message":"Hello from Render!"}'
```

---

## Render Blueprint (Optional)

For one-click deployment, create `render.yaml` at the repository root:

```yaml
services:
  - type: web
    name: notifyflow-gateway
    rootDir: services/gateway-service
    runtime: docker
    healthCheckPath: /actuator/health
    envVars:
      - key: USER_SERVICE_URL
        fromService:
          name: notifyflow-user-service
          type: web
          property: host

  - type: web
    name: notifyflow-user-service
    rootDir: services/user-service
    runtime: docker
    healthCheckPath: /actuator/health
    envVars:
      - key: SPRING_RABBITMQ_HOST
        sync: false
      - key: SPRING_RABBITMQ_PORT
        value: 5672

  - type: web
    name: notifyflow-notification-service
    rootDir: services/notification-service
    runtime: docker
    healthCheckPath: /actuator/health
    envVars:
      - key: SPRING_RABBITMQ_HOST
        sync: false
      - key: SPRING_RABBITMQ_PORT
        value: 5672
```

---

## Troubleshooting

| Issue                            | Solution                                               |
|----------------------------------|--------------------------------------------------------|
| Service fails to start           | Check logs in Render dashboard                         |
| RabbitMQ connection refused      | Verify CloudAMQP credentials and IP whitelist          |
| Gateway can't reach User Service | Verify `USER_SERVICE_URL` points to correct Render URL |
| Health check failing             | Ensure `/actuator/health` returns 200                  |
| Slow cold starts                 | Free tier instances sleep after 15 min of inactivity   |
