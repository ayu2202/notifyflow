#!/bin/bash
set -e

echo "============================================"
echo "  NotifyFlow — Starting Local Environment"
echo "============================================"
echo ""

# Navigate to project root (parent of scripts/)
cd "$(dirname "$0")/.."

echo "Building all services..."
echo ""

for service in user-service notification-service gateway-service; do
  echo "  Building $service..."
  (cd "services/$service" && mvn clean package -DskipTests -q)
  echo "  ✓ $service built"
done

echo ""
echo "Starting Docker Compose..."
echo ""

docker compose -f infrastructure/docker-compose.yml up --build

