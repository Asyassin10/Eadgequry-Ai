#!/bin/bash

# Check if podman-compose is available, otherwise use docker-compose
if command -v podman-compose &> /dev/null; then
    COMPOSE_CMD="podman-compose"
elif command -v docker-compose &> /dev/null; then
    COMPOSE_CMD="docker-compose"
else
    echo "❌ Error: Neither podman-compose nor docker-compose found!"
    exit 1
fi

# If a service name is provided, show logs for that service
# Otherwise, show logs for all services
if [ -z "$1" ]; then
    echo "📋 Showing logs for all services (press Ctrl+C to exit)..."
    echo ""
    $COMPOSE_CMD -f podman-compose.yml logs -f
else
    echo "📋 Showing logs for $1 (press Ctrl+C to exit)..."
    echo ""
    $COMPOSE_CMD -f podman-compose.yml logs -f "$1"
fi
