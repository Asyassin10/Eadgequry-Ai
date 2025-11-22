#!/bin/bash

echo "=================================="
echo "  PODMAN/DOCKER SERVICE SUMMARY"
echo "=================================="
echo ""

echo "📦 JAVA MICROSERVICES (7 services):"
echo "  ✓ naming-server        (Port 8761) - Eureka Discovery"
echo "  ✓ api-gateway          (Port 8765) - API Gateway"
echo "  ✓ auth-service         (Port 8081) - Authentication"
echo "  ✓ user-profile-service (Port 8082) - User Profiles"
echo "  ✓ notification-service (Port 8084) - Notifications"
echo "  ✓ data-source-service  (Port 8085) - Data Sources"
echo "  ✓ chatbot-service      (Port 8089) - Chatbot"
echo ""

echo "🌐 FRONTEND (1 service):"
echo "  ✓ frontend             (Port 3000) - Next.js App"
echo ""

echo "🗄️  INFRASTRUCTURE (11 services):"
echo "  ✓ mysql                (Port 3306) - Auth DB"
echo "  ✓ mysql-user-profile   (Port 3307) - Profile DB"
echo "  ✓ mysql-datasource     (Port 3308) - DataSource DB"
echo "  ✓ mysql-chatbot        (Port 3311) - Chatbot DB"
echo "  ✓ phpmyadmin           (Port 8080) - Auth DB Admin"
echo "  ✓ phpmyadmin-user-profile (Port 8083) - Profile DB Admin"
echo "  ✓ phpmyadmin-datasource (Port 8088) - DataSource DB Admin"
echo "  ✓ phpmyadmin-chatbot   (Port 8090) - Chatbot DB Admin"
echo "  ✓ kafka                (Port 9092) - Message Broker"
echo "  ✓ zookeeper            (Port 2181) - Kafka Coordinator"
echo "  ✓ sonarqube            (Port 9000) - Code Quality"
echo "  ✓ jenkins              (Port 8982) - CI/CD"
echo ""

echo "📁 CONTAINERFILES:"
for dir in naming-server api-gatway auth user-profile notification data-source chat-bot-service front-end-next-ts; do
    if [ -f "$dir/Containerfile" ]; then
        echo "  ✓ $dir/Containerfile"
    else
        echo "  ✗ $dir/Containerfile (MISSING!)"
    fi
done
echo ""

echo "🔧 HELPER SCRIPTS:"
for script in build-all.sh build-compose.sh start-all.sh stop-all.sh logs.sh; do
    if [ -f "$script" ]; then
        echo "  ✓ $script"
    else
        echo "  ✗ $script (MISSING!)"
    fi
done
echo ""

echo "=================================="
echo "TOTAL: 19 Services (8 app + 11 infra)"
echo "=================================="
echo ""
echo "🚀 Ready to run with:"
echo "   ./build-all.sh        # Build everything (PARALLEL - FAST!)"
echo "   ./build-compose.sh    # Build with compose (SIMPLEST)"
echo "   ./start-all.sh        # Start all services"
echo "   ./logs.sh             # View logs"
echo "   ./stop-all.sh         # Stop all services"
echo ""
