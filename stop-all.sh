#!/bin/bash

set -e  # Exit on error

echo "🛑 Stopping all services..."
echo ""

# Check if podman-compose is available, otherwise use docker-compose
if command -v podman-compose &> /dev/null; then
    COMPOSE_CMD="podman-compose"
    echo "Using podman-compose"
elif command -v docker-compose &> /dev/null; then
    COMPOSE_CMD="docker-compose"
    echo "Using docker-compose"
else
    echo "❌ Error: Neither podman-compose nor docker-compose found!"
    exit 1
fi

echo ""

# Stop all services
$COMPOSE_CMD -f podman-compose.yml down

echo ""
echo "✅ All services stopped!"
echo ""
echo "💡 To remove volumes as well, run:"
echo "   $COMPOSE_CMD down -v"
echo ""
