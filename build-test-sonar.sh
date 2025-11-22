#!/bin/bash

set -e

echo "🚀 Complete Build, Test, Coverage, and SonarQube Pipeline"
echo "=========================================================="
echo ""

# Step 1: Build JARs
echo "Step 1/4: Building all services..."
./build-all.sh

echo ""
echo "=========================================================="
echo ""

# Step 2: Run tests with coverage
echo "Step 2/4: Running tests with coverage..."
./test-coverage.sh

echo ""
echo "=========================================================="
echo ""

# Step 3: SonarQube analysis
echo "Step 3/4: Running SonarQube analysis..."
./sonar-analyze.sh

echo ""
echo "=========================================================="
echo ""

# Step 4: Build container images (optional)
read -p "Build container images? (y/n) " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    echo "Step 4/4: Building container images..."

    if command -v podman &> /dev/null; then
        CONTAINER_CMD="podman"
    else
        CONTAINER_CMD="docker"
    fi

    podman-compose -f podman-compose.yml build || docker-compose -f podman-compose.yml build
    echo "✅ Container images built"
fi

echo ""
echo "=========================================================="
echo "✅ Pipeline Complete!"
echo "=========================================================="
echo ""
echo "📊 Next steps:"
echo "   1. View test coverage: ./*/target/site/jacoco/index.html"
echo "   2. View SonarQube: http://localhost:9000"
echo "   3. Start services: ./start-all.sh"
echo ""
