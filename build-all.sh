#!/bin/bash

set -e  # Exit on error

echo "🔨 Building all microservices and frontend..."
echo ""

# ===========================
# BUILD JAVA MICROSERVICES IN PARALLEL
# ===========================

echo "📦 Building all Java services in parallel..."
echo ""

# Array of service directories
SERVICES=(
    "naming-server"
    "api-gatway"
    "auth"
    "user-profile"
    "notification"
    "data-source"
    "chat-bot-service"
)

# Function to build a service
build_service() {
    local service=$1
    echo "  → Building $service..."
    cd "$service"
    ./mvnw clean package -DskipTests > "../build-$service.log" 2>&1
    local exit_code=$?
    cd ..
    if [ $exit_code -eq 0 ]; then
        echo "  ✓ $service built successfully"
    else
        echo "  ✗ $service build failed! Check build-$service.log"
        return 1
    fi
}

# Build all services in parallel
for service in "${SERVICES[@]}"; do
    build_service "$service" &
done

# Wait for all background jobs to complete
echo ""
echo "⏳ Waiting for all builds to complete..."
wait

# Check if all builds succeeded
failed=0
for service in "${SERVICES[@]}"; do
    if [ -f "build-$service.log" ]; then
        if grep -q "BUILD SUCCESS" "build-$service.log"; then
            rm "build-$service.log"  # Clean up successful build logs
        else
            failed=1
        fi
    fi
done

if [ $failed -eq 1 ]; then
    echo ""
    echo "❌ Some builds failed! Check the build logs."
    exit 1
fi

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
# BUILD CONTAINER IMAGES IN PARALLEL
# ===========================

echo "🐳 Building Podman/Docker images in parallel..."
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

# Array of images to build (name:context:containerfile)
declare -A IMAGES=(
    ["naming-server"]="naming-server:naming-server/Containerfile"
    ["api-gateway"]="api-gatway:api-gatway/Containerfile"
    ["auth-service"]="auth:auth/Containerfile"
    ["user-profile-service"]="user-profile:user-profile/Containerfile"
    ["notification-service"]="notification:notification/Containerfile"
    ["data-source-service"]="data-source:data-source/Containerfile"
    ["chatbot-service"]="chat-bot-service:chat-bot-service/Containerfile"
 )

# Function to build container image
build_image() {
    local name=$1
    local context_and_file=$2
    local context=$(echo $context_and_file | cut -d: -f1)
    local containerfile=$(echo $context_and_file | cut -d: -f2)

    echo "  → Building container image: $name..."
    $CONTAINER_CMD build -t "$name:latest" -f "$containerfile" "$context" > "build-image-$name.log" 2>&1
    local exit_code=$?

    if [ $exit_code -eq 0 ]; then
        echo "  ✓ $name image built successfully"
        rm "build-image-$name.log"
    else
        echo "  ✗ $name image build failed! Check build-image-$name.log"
        return 1
    fi
}

# Build all images in parallel
for name in "${!IMAGES[@]}"; do
    build_image "$name" "${IMAGES[$name]}" &
done

echo "⏳ Waiting for all image builds to complete..."
wait

echo ""
echo "✅ All container images built successfully!"
echo ""
echo "📋 Summary:"
echo "   ✓ 7 Java microservices compiled (parallel)"
 echo "   ✓ 8 container images created (parallel)"
echo ""
echo "🚀 To start all services, run:"
echo "   podman-compose up -d"
echo "   OR"
echo "   docker-compose up -d"
echo ""
