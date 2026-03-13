#!/bin/bash
set -e

echo "============================================"
echo "  NotifyFlow — Building All Services"
echo "============================================"
echo ""

cd "$(dirname "$0")/.."

FAILED=0

for service in user-service notification-service gateway-service; do
  echo "Building services/$service..."
  if (cd "services/$service" && mvn clean package -DskipTests -q); then
    echo "  ✓ $service built successfully"
  else
    echo "  ✗ $service build FAILED"
    FAILED=1
  fi
  echo ""
done

if [ $FAILED -eq 0 ]; then
  echo "============================================"
  echo "  ✓ All services built successfully"
  echo "============================================"
else
  echo "============================================"
  echo "  ✗ Some builds failed — check output above"
  echo "============================================"
  exit 1
fi
