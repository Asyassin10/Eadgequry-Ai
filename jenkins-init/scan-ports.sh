#!/bin/bash

# Port Scanner for Docker Compose
# Scans docker-compose.yml and finds used/available ports

set -e

COMPOSE_FILE="${1:-docker-compose.yml}"
START_PORT="${2:-8000}"
END_PORT="${3:-9999}"

echo "╔════════════════════════════════════════════════════════╗"
echo "║     EadgeQuery Port Scanner                           ║"
echo "╠════════════════════════════════════════════════════════╣"
echo "║ Scanning: ${COMPOSE_FILE}"
echo "║ Range: ${START_PORT}-${END_PORT}"
echo "╚════════════════════════════════════════════════════════╝"
echo ""

# Check if file exists
if [ ! -f "$COMPOSE_FILE" ]; then
    echo "❌ Error: $COMPOSE_FILE not found!"
    exit 1
fi

# Extract all ports from docker-compose.yml
echo "📊 Ports currently in use:"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

# Parse ports section and extract host ports
USED_PORTS=$(grep -oE '[0-9]+:[0-9]+' "$COMPOSE_FILE" | \
    cut -d: -f1 | \
    sort -n | \
    uniq)

# Display used ports with service names
while IFS= read -r port; do
    if [ -n "$port" ]; then
        # Try to find the service name for this port
        SERVICE=$(grep -B 20 "${port}:" "$COMPOSE_FILE" | \
            grep -E '^\s+[a-z][a-z0-9-]*:' | \
            tail -1 | \
            sed 's/:.*//g' | \
            sed 's/^[[:space:]]*//')

        if [ -n "$SERVICE" ]; then
            printf "  %-6s → %s\n" "$port" "$SERVICE"
        else
            printf "  %-6s → %s\n" "$port" "unknown"
        fi
    fi
done <<< "$USED_PORTS"

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

# Find available ports
echo "✅ Available ports in range ${START_PORT}-${END_PORT}:"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

AVAILABLE_COUNT=0
for port in $(seq $START_PORT $END_PORT); do
    if ! echo "$USED_PORTS" | grep -q "^${port}$"; then
        # Check if port is in use on the system
        if ! lsof -i :$port > /dev/null 2>&1 && ! netstat -tuln 2>/dev/null | grep -q ":${port} "; then
            printf "  %s\n" "$port"
            AVAILABLE_COUNT=$((AVAILABLE_COUNT + 1))

            # Only show first 20 available ports
            if [ $AVAILABLE_COUNT -ge 20 ]; then
                echo "  ... (and more)"
                break
            fi
        fi
    fi
done

echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

# Summary
TOTAL_USED=$(echo "$USED_PORTS" | wc -l)
echo "📈 Summary:"
echo "  • Total ports in use: $TOTAL_USED"
echo "  • Available ports found: $AVAILABLE_COUNT+"
echo ""

# Suggest port for Jenkins if not already in use
JENKINS_SUGGESTED_PORT=8082
if echo "$USED_PORTS" | grep -q "^${JENKINS_SUGGESTED_PORT}$"; then
    echo "⚠️  Jenkins port ${JENKINS_SUGGESTED_PORT} is already in use!"
    echo "   Suggesting alternative ports for Jenkins:"
    for alt_port in 8090 8091 8092 8093 8094; do
        if ! echo "$USED_PORTS" | grep -q "^${alt_port}$"; then
            echo "   ✓ Port ${alt_port} is available"
            JENKINS_SUGGESTED_PORT=$alt_port
            break
        fi
    done
else
    echo "✅ Jenkins can use port: ${JENKINS_SUGGESTED_PORT}"
fi

echo ""
echo "╔════════════════════════════════════════════════════════╗"
echo "║  Recommendation for Jenkins:                          ║"
echo "║  - Web UI: ${JENKINS_SUGGESTED_PORT}:8080                              ║"
echo "║  - Agent: 50000:50000                                 ║"
echo "╚════════════════════════════════════════════════════════╝"
