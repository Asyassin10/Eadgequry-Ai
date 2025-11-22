#!/bin/bash

set -e  # Exit on error

echo "🔨 Building all microservices and frontend..."
echo ""

# ===========================
# BUILD JAVA MICROSERVICES
# ===========================

# ===== Naming Server =====
echo "📦 Building Naming Server..."
cd naming-server
./mvnw clean package -DskipTests
cd ..

# ===== API Gateway =====
echo "📦 Building API Gateway..."
cd api-gatway
./mvnw clean package -DskipTests
cd ..

# ===== Auth Service =====
echo "📦 Building Auth Service..."
cd auth
./mvnw clean package -DskipTests
cd ..

# ===== User Profile Service =====
echo "📦 Building User Profile Service..."
cd user-profile
./mvnw clean package -DskipTests
cd ..

# ===== Notification Service =====
echo "📦 Building Notification Service..."
cd notification
./mvnw clean package -DskipTests
cd ..

# ===== Data Source Service =====
echo "📦 Building Data Source Service..."
cd data-source
./mvnw clean package -DskipTests
cd ..

# ===== Chatbot Service =====
echo "📦 Building Chatbot Service..."
cd chat-bot-service
./mvnw clean package -DskipTests
cd ..

echo ""
echo "✅ All Java services built successfully!"
echo "📍 JAR files location:"
echo "   - naming-server/target/*.jar"
echo "   - api-gatway/target/*.jar"
echo "   - auth/target/*.jar"
echo "   - user-profile/target/*.jar"
echo "   - notification/target/*.jar"
echo "   - data-source/target/*.jar"
echo "   - chat-bot-service/target/*.jar"
echo ""

# ===========================
# BUILD FRONTEND
# ===========================

echo "📦 Building Next.js Frontend..."
cd front-end-next-ts
npm install
npm run build
cd ..

echo ""
echo "✅ Frontend built successfully!"
echo ""

# ===========================
# BUILD PODMAN IMAGES
# ===========================

echo "🐳 Building Podman/Docker images..."
echo ""

# Check if podman is available, otherwise use docker
if command -v podman &> /dev/null; then
    CONTAINER_CMD="podman"
    echo "Using Podman for container builds"
else
    CONTAINER_CMD="docker"
    echo "Using Docker for container builds"
fi

echo ""

# Build all service images
echo "📦 Building container image: naming-server..."
$CONTAINER_CMD build -t naming-server:latest -f naming-server/Containerfile naming-server/

echo "📦 Building container image: api-gateway..."
$CONTAINER_CMD build -t api-gateway:latest -f api-gatway/Containerfile api-gatway/

echo "📦 Building container image: auth-service..."
$CONTAINER_CMD build -t auth-service:latest -f auth/Containerfile auth/

echo "📦 Building container image: user-profile-service..."
$CONTAINER_CMD build -t user-profile-service:latest -f user-profile/Containerfile user-profile/

echo "📦 Building container image: notification-service..."
$CONTAINER_CMD build -t notification-service:latest -f notification/Containerfile notification/

echo "📦 Building container image: data-source-service..."
$CONTAINER_CMD build -t data-source-service:latest -f data-source/Containerfile data-source/

echo "📦 Building container image: chatbot-service..."
$CONTAINER_CMD build -t chatbot-service:latest -f chat-bot-service/Containerfile chat-bot-service/

echo "📦 Building container image: frontend..."
$CONTAINER_CMD build -t frontend:latest -f front-end-next-ts/Containerfile front-end-next-ts/

echo ""
echo "✅ All container images built successfully!"
echo ""
echo "📋 Summary:"
echo "   ✓ 7 Java microservices compiled"
echo "   ✓ 1 Next.js frontend built"
echo "   ✓ 8 container images created"
echo ""
echo "🚀 To start all services, run:"
echo "   podman-compose up -d"
echo "   OR"
echo "   docker-compose up -d"
echo ""
