#!/bin/bash
set -e

echo "============================================"
echo "  NotifyFlow — Starting Local Environment"
echo "============================================"
echo ""

# Navigate to project root (parent of scripts/)
cd "$(dirname "$0")/.."

echo "Building Docker images (multi-stage — no local Maven required)..."
echo ""

docker compose -f infrastructure/docker-compose.yml build

echo ""
echo "Starting all services..."
echo ""

docker compose -f infrastructure/docker-compose.yml up -d

echo ""
echo "✓ NotifyFlow is starting. Services will be ready in ~30 seconds."
echo ""
echo "  Gateway:       http://localhost:8083"
echo "  Swagger UI:    http://localhost:8083/swagger-ui.html"
echo "  RabbitMQ:      http://localhost:15672"
echo "  Grafana:       http://localhost:3000"
echo "  Prometheus:    http://localhost:9090"
echo "  Zipkin:        http://localhost:9411"
echo ""
echo "Run 'bash scripts/test-event.sh' to verify the event pipeline."

