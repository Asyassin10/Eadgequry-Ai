#!/bin/bash

set -e

echo "🔄 Rebuilding all services with Java 21"
echo ""

# Stop all containers
echo "1️⃣ Stopping all containers..."
podman-compose down

# Clean old images (optional)
read -p "Remove old images? (y/n) " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    echo "🧹 Cleaning old images..."
    podman rmi -f $(podman images -q 'localhost/eadgequry-ai_*' 2>/dev/null) 2>/dev/null || true
fi

# Rebuild JARs
echo ""
echo "2️⃣ Building JARs..."
./build-all.sh

# Rebuild containers
echo ""
echo "3️⃣ Building container images..."
podman-compose build

# Start services
echo ""
echo "4️⃣ Starting all services..."
podman-compose up -d

# Wait a bit
sleep 5

# Check status
echo ""
echo "5️⃣ Checking status..."
podman-compose ps

echo ""
echo "✅ Rebuild complete!"
echo ""
echo "📋 View logs:"
echo "   podman-compose logs -f data-source-service"
echo "   podman-compose logs -f chatbot-service"
echo "   podman-compose logs -f notification-service"
echo ""
