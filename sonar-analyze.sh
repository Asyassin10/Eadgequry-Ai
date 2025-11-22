#!/bin/bash

set -e

echo "📊 Running SonarQube analysis for all services..."
echo ""

# SonarQube server details
SONAR_HOST="${SONAR_HOST:-http://localhost:9000}"
SONAR_TOKEN="${SONAR_TOKEN:-}"

if [ -z "$SONAR_TOKEN" ]; then
    echo "⚠️  SONAR_TOKEN not set. Using default token or will prompt for login."
    echo "   Set it with: export SONAR_TOKEN=your_token"
    echo ""
fi

SERVICES=(
    "auth"
    "user-profile"
    "notification"
    "data-source"
    "chat-bot-service"
)

for service in "${SERVICES[@]}"; do
    echo "======================================"
    echo "📦 Analyzing: $service"
    echo "======================================"

    if [ -d "$service" ]; then
        cd "$service"

        # Build and test first to generate coverage
        echo "  → Running tests and generating coverage..."
        ./mvnw clean verify jacoco:report

        # Run SonarQube analysis
        echo "  → Running SonarQube analysis..."
        if [ -n "$SONAR_TOKEN" ]; then
            ./mvnw sonar:sonar \
                -Dsonar.host.url="$SONAR_HOST" \
                -Dsonar.login="$SONAR_TOKEN" \
                -Dsonar.projectKey="eadgequry-$service" \
                -Dsonar.projectName="Eadgequry $service"
        else
            ./mvnw sonar:sonar \
                -Dsonar.host.url="$SONAR_HOST" \
                -Dsonar.projectKey="eadgequry-$service" \
                -Dsonar.projectName="Eadgequry $service"
        fi

        echo "✅ $service analysis complete"
        cd ..
        echo ""
    else
        echo "⚠️  Service directory not found: $service"
        echo ""
    fi
done

echo "======================================"
echo "✅ SonarQube Analysis Complete"
echo "======================================"
echo ""
echo "🌐 View results at: $SONAR_HOST"
echo ""
echo "Projects analyzed:"
for service in "${SERVICES[@]}"; do
    echo "   - eadgequry-$service"
done
echo ""
