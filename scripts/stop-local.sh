#!/bin/bash

echo "============================================"
echo "  NotifyFlow — Stopping Local Environment"
echo "============================================"
echo ""

cd "$(dirname "$0")/.."

docker compose -f infrastructure/docker-compose.yml down

echo ""
echo "✓ All services stopped."
echo "  To also remove volumes and images: docker compose -f infrastructure/docker-compose.yml down -v --rmi local"
