#!/bin/bash

echo "============================================"
echo "  NotifyFlow — Demo Event Test"
echo "============================================"
echo ""

GATEWAY_URL="${GATEWAY_URL:-http://localhost:8083}"

echo "1. Checking system status..."
echo ""
curl -s "$GATEWAY_URL/demo/status" | python3 -m json.tool 2>/dev/null || curl -s "$GATEWAY_URL/demo/status"
echo ""
echo ""

echo "2. Sending demo event..."
echo ""
curl -s -X POST "$GATEWAY_URL/demo/event" \
  -H "Content-Type: application/json" \
  -d '{"email":"demo@notifyflow.com","message":"Hello from NotifyFlow demo"}' | python3 -m json.tool 2>/dev/null || \
curl -s -X POST "$GATEWAY_URL/demo/event" \
  -H "Content-Type: application/json" \
  -d '{"email":"demo@notifyflow.com","message":"Hello from NotifyFlow demo"}'
echo ""
echo ""

echo "3. Checking notification logs..."
echo ""
docker logs notifyflow-notification-service 2>&1 | grep "Notification sent" | tail -5
echo ""

echo "============================================"
echo "  ✓ Demo complete"
echo "============================================"
