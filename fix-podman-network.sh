#!/bin/bash

echo "🔧 Fixing Podman network issue..."
echo ""

# Stop all containers first
echo "1️⃣ Stopping all containers..."
podman stop $(podman ps -aq) 2>/dev/null || true

# Remove all containers
echo "2️⃣ Removing all containers..."
podman rm -f $(podman ps -aq) 2>/dev/null || true

# Remove the network
echo "3️⃣ Removing network..."
podman network rm eadgequry-ai_edagequry-net 2>/dev/null || true

# Clean up build cache (optional)
read -p "Clean build cache? (y/n) " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    echo "4️⃣ Cleaning build cache..."
    podman builder prune -f
fi

echo ""
echo "✅ Cleanup complete!"
echo ""
echo "Now run: podman-compose up -d --build"
