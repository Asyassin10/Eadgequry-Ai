#!/bin/bash

echo "🔨 Building all microservices..."

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

echo "✅ All services built successfully!"
echo "📍 JAR files location:"
echo "   - naming-server/target/*.jar"
echo "   - api-gatway/target/*.jar"
echo "   - auth/target/*.jar"
echo "   - user-profile/target/*.jar"
echo "   - notification/target/*.jar"
