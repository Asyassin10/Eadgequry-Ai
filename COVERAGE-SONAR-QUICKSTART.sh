#!/bin/bash

echo "========================================="
echo "  COVERAGE + SONARQUBE QUICK START"
echo "========================================="
echo ""

# Step 1: Start SonarQube
echo "1️⃣ Starting SonarQube..."
podman-compose up -d sonarqube

echo "   Waiting 60 seconds for SonarQube to start..."
sleep 60

echo ""
echo "2️⃣ SETUP SONARQUBE (First time only):"
echo "   → Open: http://localhost:9000"
echo "   → Login: admin / admin"
echo "   → Change password when prompted"
echo "   → Create these projects manually:"
echo "     - eadgequry-auth"
echo "     - eadgequry-user-profile"
echo "     - eadgequry-notification"
echo "     - eadgequry-data-source"
echo "     - eadgequry-chatbot"
echo ""
echo "   → Generate token:"
echo "     1. Click avatar (top right)"
echo "     2. My Account → Security"
echo "     3. Generate Tokens → Name: 'local'"
echo "     4. Click Generate"
echo "     5. COPY THE TOKEN"
echo ""
read -p "Press Enter when SonarQube is setup and you have the token..."

echo ""
read -p "Enter your SonarQube token: " SONAR_TOKEN
export SONAR_TOKEN

echo ""
echo "3️⃣ Running tests with coverage..."
./test-coverage.sh

echo ""
echo "4️⃣ Sending coverage to SonarQube..."
./sonar-analyze.sh

echo ""
echo "========================================="
echo "✅ DONE!"
echo "========================================="
echo ""
echo "📊 View results:"
echo "   http://localhost:9000"
echo ""
echo "📁 Local reports:"
echo "   auth/target/site/jacoco/index.html"
echo "   user-profile/target/site/jacoco/index.html"
echo "   notification/target/site/jacoco/index.html"
echo "   data-source/target/site/jacoco/index.html"
echo "   chat-bot-service/target/site/jacoco/index.html"
echo ""
