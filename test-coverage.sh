#!/bin/bash

set -e

echo "🧪 Running tests with coverage for all services..."
echo ""

SERVICES=(
    "auth"
    "user-profile"
    "notification"
    "data-source"
    "chat-bot-service"
)

failed_services=()

for service in "${SERVICES[@]}"; do
    echo "======================================"
    echo "📦 Testing: $service"
    echo "======================================"

    if [ -d "$service" ]; then
        cd "$service"

        if ./mvnw clean test jacoco:report; then
            echo "✅ $service tests passed"

            # Show coverage summary if available
            if [ -f "target/site/jacoco/index.html" ]; then
                echo "📊 Coverage report: $service/target/site/jacoco/index.html"
            fi
        else
            echo "❌ $service tests failed"
            failed_services+=("$service")
        fi

        cd ..
        echo ""
    else
        echo "⚠️  Service directory not found: $service"
        echo ""
    fi
done

echo "======================================"
echo "📊 TEST SUMMARY"
echo "======================================"

if [ ${#failed_services[@]} -eq 0 ]; then
    echo "✅ All services passed tests!"
    echo ""
    echo "📁 Coverage reports location:"
    for service in "${SERVICES[@]}"; do
        if [ -f "$service/target/site/jacoco/index.html" ]; then
            echo "   - $service/target/site/jacoco/index.html"
        fi
    done
    echo ""
    exit 0
else
    echo "❌ Failed services:"
    for service in "${failed_services[@]}"; do
        echo "   - $service"
    done
    echo ""
    exit 1
fi
