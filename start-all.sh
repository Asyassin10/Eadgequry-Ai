#!/bin/bash

set -e  # Exit on error

echo "🚀 Starting all services with Podman/Docker..."
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
echo "📋 Starting services in order:"
echo "   1. Infrastructure (MySQL, Kafka, etc.)"
echo "   2. Naming Server (Eureka)"
echo "   3. Microservices"
echo "   4. API Gateway"
echo "   5. Frontend"
echo ""

# Start all services
$COMPOSE_CMD -f podman-compose.yml up -d

echo ""
echo "✅ All services started!"
echo ""
echo "📊 Service URLs:"
echo "   🌐 Frontend:              http://localhost:3000"
echo "   🚪 API Gateway:           http://localhost:8765"
echo "   🔍 Eureka Dashboard:      http://localhost:8761"
echo "   🔐 Auth Service:          http://localhost:8081"
echo "   👤 User Profile Service:  http://localhost:8082"
echo "   📧 Notification Service:  http://localhost:8084"
echo "   💾 Data Source Service:   http://localhost:8085"
echo "   🤖 Chatbot Service:       http://localhost:8089"
echo ""
echo "🛠️  Management Tools:"
echo "   📊 SonarQube:            http://localhost:9000"
echo "   🔧 Jenkins:              http://localhost:8982/jenkins"
echo "   🗄️  phpMyAdmin (auth):    http://localhost:8080"
echo "   🗄️  phpMyAdmin (profile): http://localhost:8083"
echo "   🗄️  phpMyAdmin (datasrc): http://localhost:8088"
echo "   🗄️  phpMyAdmin (chatbot): http://localhost:8090"
echo ""
echo "📝 Useful commands:"
echo "   View logs:        $COMPOSE_CMD logs -f [service-name]"
echo "   Stop all:         $COMPOSE_CMD down"
echo "   Restart service:  $COMPOSE_CMD restart [service-name]"
echo "   View status:      $COMPOSE_CMD ps"
echo ""
