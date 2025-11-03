#!/bin/bash

echo "🔨 Building all services..."

# Build Naming Server
echo "📦 Building Naming Server..."
cd naming-server
./mvnw clean package - 
cd ..

# Build API Gateway
echo "📦 Building API Gateway..."
cd api-gatway
./mvnw clean package
cd ..

# Build Auth Service
echo "📦 Building Auth Service..."
cd auth
./mvnw clean package -DskipTests
cd ..

echo "✅ All services built successfully!"
echo "📍 JAR files location:"
echo "   - naming-server/target/*.jar"
echo "   - api-gatway/target/*.jar"
echo "   - auth/target/*.jar"