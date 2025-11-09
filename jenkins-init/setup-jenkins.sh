#!/bin/bash

# Jenkins Setup Script for EadgeQuery
# Automates Jenkins initial setup and job creation

set -e

JENKINS_URL="${JENKINS_URL:-http://localhost:8082}"
JENKINS_USER="${JENKINS_USER:-admin}"
JENKINS_PASSWORD="${JENKINS_PASSWORD:-admin}"

echo "╔════════════════════════════════════════════════════════╗"
echo "║     EadgeQuery Jenkins Setup                          ║"
echo "╠════════════════════════════════════════════════════════╣"
echo "║ URL: ${JENKINS_URL}"
echo "╚════════════════════════════════════════════════════════╝"
echo ""

# Wait for Jenkins to be ready
echo "⏳ Waiting for Jenkins to start..."
RETRY_COUNT=0
MAX_RETRIES=30

while [ $RETRY_COUNT -lt $MAX_RETRIES ]; do
    if curl -s -f "${JENKINS_URL}/login" > /dev/null 2>&1; then
        echo "✅ Jenkins is ready!"
        break
    fi

    echo "   Attempt $((RETRY_COUNT + 1))/${MAX_RETRIES}..."
    sleep 5
    RETRY_COUNT=$((RETRY_COUNT + 1))
done

if [ $RETRY_COUNT -eq $MAX_RETRIES ]; then
    echo "❌ Jenkins failed to start after ${MAX_RETRIES} attempts"
    exit 1
fi

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "🔑 Jenkins Initial Admin Password:"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

# Try to get initial admin password from container
if docker ps --filter "name=jenkins-cicd" --format "{{.Names}}" | grep -q jenkins; then
    INITIAL_PASSWORD=$(docker exec jenkins-cicd cat /var/jenkins_home/secrets/initialAdminPassword 2>/dev/null || echo "")

    if [ -n "$INITIAL_PASSWORD" ]; then
        echo ""
        echo "  📋 Initial Admin Password: ${INITIAL_PASSWORD}"
        echo ""
        echo "  Use this password to complete setup at:"
        echo "  ${JENKINS_URL}/jenkins"
        echo ""
    fi
fi

echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

# Define services
SERVICES=(
    "auth"
    "api-gatway"
    "chat-bot-service"
    "data-source"
    "user-profile"
    "notification"
    "naming-server"
)

echo "📦 Services to configure in Jenkins:"
for service in "${SERVICES[@]}"; do
    echo "  • $service"
done
echo ""

# Create job config XML template
create_job_xml() {
    local SERVICE_NAME=$1
    local SERVICE_PATH=$2

    cat << EOF
<?xml version='1.1' encoding='UTF-8'?>
<flow-definition plugin="workflow-job@2.40">
  <actions/>
  <description>CI/CD Pipeline for ${SERVICE_NAME} service</description>
  <keepDependencies>false</keepDependencies>
  <properties>
    <hudson.model.ParametersDefinitionProperty>
      <parameterDefinitions>
        <hudson.model.ChoiceParameterDefinition>
          <name>BUILD_TYPE</name>
          <description>Select build type</description>
          <choices class="java.util.Arrays\$ArrayList">
            <a class="string-array">
              <string>build</string>
              <string>test</string>
              <string>deploy</string>
              <string>full</string>
            </a>
          </choices>
        </hudson.model.ChoiceParameterDefinition>
        <hudson.model.BooleanParameterDefinition>
          <name>RUN_SONAR</name>
          <description>Run SonarQube analysis</description>
          <defaultValue>true</defaultValue>
        </hudson.model.BooleanParameterDefinition>
        <hudson.model.BooleanParameterDefinition>
          <name>RUN_TESTS</name>
          <description>Run unit tests</description>
          <defaultValue>true</defaultValue>
        </hudson.model.BooleanParameterDefinition>
        <hudson.model.BooleanParameterDefinition>
          <name>DEPLOY_TO_COMPOSE</name>
          <description>Deploy to Docker Compose</description>
          <defaultValue>false</defaultValue>
        </hudson.model.BooleanParameterDefinition>
      </parameterDefinitions>
    </hudson.model.ParametersDefinitionProperty>
  </properties>
  <definition class="org.jenkinsci.plugins.workflow.cps.CpsScmFlowDefinition" plugin="workflow-cps@2.90">
    <scm class="hudson.plugins.git.GitSCM" plugin="git@4.10.0">
      <configVersion>2</configVersion>
      <userRemoteConfigs>
        <hudson.plugins.git.UserRemoteConfig>
          <url>https://github.com/Asyassin10/Eadgequry-Ai.git</url>
        </hudson.plugins.git.UserRemoteConfig>
      </userRemoteConfigs>
      <branches>
        <hudson.plugins.git.BranchSpec>
          <name>*/main</name>
        </hudson.plugins.git.BranchSpec>
      </branches>
      <doGenerateSubmoduleConfigurations>false</doGenerateSubmoduleConfigurations>
      <submoduleCfg class="empty-list"/>
      <extensions/>
    </scm>
    <scriptPath>${SERVICE_PATH}/Jenkinsfile</scriptPath>
    <lightweight>true</lightweight>
  </definition>
  <triggers/>
  <disabled>false</disabled>
</flow-definition>
EOF
}

echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "📝 Manual Setup Instructions:"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "1. Open Jenkins at: ${JENKINS_URL}/jenkins"
echo ""
echo "2. Complete the initial setup wizard"
echo ""
echo "3. Install recommended plugins, plus:"
echo "   • Docker Pipeline"
echo "   • Pipeline Maven Integration"
echo "   • SonarQube Scanner"
echo "   • Git Plugin"
echo ""
echo "4. Create a new Pipeline job for each service:"
for service in "${SERVICES[@]}"; do
    echo "   • $service-pipeline"
done
echo ""
echo "5. For each job, configure:"
echo "   • Definition: Pipeline script from SCM"
echo "   • SCM: Git"
echo "   • Repository URL: https://github.com/Asyassin10/Eadgequry-Ai.git"
echo "   • Branch: */main (or your branch)"
echo "   • Script Path: <service-name>/Jenkinsfile"
echo ""
echo "6. Configure SonarQube server:"
echo "   • Manage Jenkins → Configure System"
echo "   • SonarQube servers → Add SonarQube"
echo "   • Name: SonarQube"
echo "   • Server URL: http://sonarqube:9000"
echo "   • Authentication token: (generate from SonarQube)"
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

# Generate job configs
echo "📁 Generating job configuration files..."
mkdir -p /tmp/jenkins-jobs

for service in "${SERVICES[@]}"; do
    create_job_xml "$service" "$service" > "/tmp/jenkins-jobs/${service}-job.xml"
    echo "  ✓ Created config for: ${service}"
done

echo ""
echo "✅ Job configuration files created in: /tmp/jenkins-jobs/"
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "🚀 Quick Start Commands:"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "# Start Jenkins"
echo "docker-compose up -d jenkins"
echo ""
echo "# View logs"
echo "docker-compose logs -f jenkins"
echo ""
echo "# Get initial admin password"
echo "docker exec jenkins-cicd cat /var/jenkins_home/secrets/initialAdminPassword"
echo ""
echo "# Restart Jenkins"
echo "docker-compose restart jenkins"
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "✨ Setup complete! Access Jenkins at: ${JENKINS_URL}/jenkins"
echo ""
