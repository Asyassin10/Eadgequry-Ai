#!/bin/bash

set -e  # Exit on error

echo "🔨 Building all services using compose (simplest method)..."
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
    echo "Please install one of them:"
    echo "  - Podman: pip install podman-compose"
    echo "  - Docker: https://docs.docker.com/compose/install/"
    exit 1
fi

echo ""
echo "📦 Building all Java services (JARs)..."
echo ""

# Build all Java services in parallel
SERVICES=(
    "naming-server"
    "api-gatway"
    "auth"
    "user-profile"
    "notification"
    "data-source"
    "chat-bot-service"
)

build_service() {
    local service=$1
    echo "  → Building $service..."
    cd "$service"
    ./mvnw clean package -DskipTests > "../build-$service.log" 2>&1
    local exit_code=$?
    cd ..
    if [ $exit_code -eq 0 ]; then
        echo "  ✓ $service built successfully"
        rm "build-$service.log"
    else
        echo "  ✗ $service build failed! Check build-$service.log"
        return 1
    fi
}

for service in "${SERVICES[@]}"; do
    build_service "$service" &
done

echo "⏳ Waiting for all JAR builds to complete..."
wait

echo ""
echo "📦 Building frontend..."
cd front-end-next-ts
npm install
npm run build
cd ..

echo ""
echo "🐳 Building all container images with compose..."
echo ""

# This command builds all images defined in podman-compose.yml
$COMPOSE_CMD -f podman-compose.yml build

echo ""
echo "✅ All services and images built successfully!"
echo ""
echo "📋 Summary:"
echo "   ✓ 7 Java microservices compiled"
echo "   ✓ 1 Next.js frontend built"
echo "   ✓ 8 container images created"
echo ""
echo "🚀 To start all services, run:"
echo "   ./start-all.sh"
echo "   OR"
echo "   $COMPOSE_CMD -f podman-compose.yml up -d"
echo ""
