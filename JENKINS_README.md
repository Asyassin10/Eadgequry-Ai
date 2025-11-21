# Jenkins CI/CD Setup for EadgeQuery

This document describes the Jenkins CI/CD setup for the EadgeQuery microservices project.

## 🏗️ Architecture

Jenkins is integrated with Podman Compose to manage CI/CD pipelines for each microservice independently:

- **Jenkins Web UI**: `http://localhost:8082/jenkins`
- **Jenkins Agent Port**: `50000`

### Services with Pipelines

1. **auth** (Port: 8081) - Authentication service
2. **api-gatway** (Port: 8765) - API Gateway
3. **chat-bot-service** (Port: 8086) - Chatbot service
4. **data-source** (Port: 8087) - Data source management
5. **user-profile** (Port: 8088) - User profile service
6. **notification** (Port: 8089) - Notification service
7. **naming-server** (Port: 8761) - Eureka discovery server

## 🚀 Quick Start

### 1. Start Jenkins

```bash
# Start Jenkins container
podman-compose up -d jenkins

# View logs
podman-compose logs -f jenkins

# Wait for Jenkins to be ready (takes about 60-90 seconds)
```

### 2. Get Initial Admin Password

```bash
# Get the initial admin password
podman exec jenkins-cicd cat /var/jenkins_home/secrets/initialAdminPassword
```

Copy this password - you'll need it for first-time setup.

### 3. Complete Initial Setup

1. Open browser: `http://localhost:8082/jenkins`
2. Paste the initial admin password
3. Select "Install suggested plugins"
4. Install additional plugins:
   - Docker Pipeline
   - Pipeline Maven Integration
   - SonarQube Scanner
   - Git Plugin
   - Blue Ocean (optional, for better UI)

5. Create admin user (or continue as admin)
6. Keep Jenkins URL as: `http://localhost:8082/jenkins`

### 4. Configure SonarQube Integration

1. Go to: **Manage Jenkins** → **Configure System**
2. Scroll to **SonarQube servers**
3. Click **Add SonarQube**
4. Configure:
   - Name: `SonarQube`
   - Server URL: `http://sonarqube:9000`
   - Server authentication token: Generate from SonarQube UI (admin/admin)

### 5. Create Pipeline Jobs

For each service, create a pipeline job:

1. Click **New Item**
2. Enter name: `<service-name>-pipeline` (e.g., `auth-pipeline`)
3. Select **Pipeline**
4. Click **OK**

5. Configure the pipeline:
   - **General** → Check "This project is parameterized"
   - Add parameters (these are defined in Jenkinsfile):
     - Choice: BUILD_TYPE (build, test, deploy, full)
     - Boolean: RUN_SONAR (default: true)
     - Boolean: RUN_TESTS (default: true)
     - Boolean: DEPLOY_TO_COMPOSE (default: false)

6. **Pipeline** section:
   - Definition: **Pipeline script from SCM**
   - SCM: **Git**
   - Repository URL: `https://github.com/Asyassin10/Eadgequry-Ai.git`
   - Branch Specifier: `*/main` (or your branch)
   - Script Path: `<service-name>/Jenkinsfile` (e.g., `auth/Jenkinsfile`)

7. Click **Save**

8. Repeat for all services:
   - `auth-pipeline` → Script Path: `auth/Jenkinsfile`
   - `api-gatway-pipeline` → Script Path: `api-gatway/Jenkinsfile`
   - `chat-bot-service-pipeline` → Script Path: `chat-bot-service/Jenkinsfile`
   - `data-source-pipeline` → Script Path: `data-source/Jenkinsfile`
   - `user-profile-pipeline` → Script Path: `user-profile/Jenkinsfile`
   - `notification-pipeline` → Script Path: `notification/Jenkinsfile`
   - `naming-server-pipeline` → Script Path: `naming-server/Jenkinsfile`

## 📦 Pipeline Features

Each service pipeline supports:

### Build Types

- **build**: Compile and package the application
- **test**: Run unit tests
- **deploy**: Build Docker image and deploy to Docker Compose
- **full**: Complete pipeline (test → build → analyze → deploy)

### Stages

1. **Checkout**: Clone the repository
2. **Environment Info**: Display build information
3. **Install Dependencies**: Maven/Gradle/NPM install
4. **Run Unit Tests**: Execute test suites
5. **SonarQube Analysis**: Code quality analysis
6. **Build Application**: Compile and package
7. **Build Docker Image**: Create Docker image
8. **Deploy to Docker Compose**: Deploy service
9. **Health Check**: Verify deployment

### Parameters

- `BUILD_TYPE`: Select pipeline stages to run
- `RUN_SONAR`: Enable/disable SonarQube analysis
- `RUN_TESTS`: Enable/disable unit tests
- `DEPLOY_TO_COMPOSE`: Enable/disable automatic deployment

## 🔧 Usage

### Running a Pipeline

1. Open Jenkins: `http://localhost:8082/jenkins`
2. Click on a pipeline (e.g., `auth-pipeline`)
3. Click **Build with Parameters**
4. Select options:
   - BUILD_TYPE: `full`
   - RUN_SONAR: ✓
   - RUN_TESTS: ✓
   - DEPLOY_TO_COMPOSE: ✓ (only if you want to deploy)
5. Click **Build**

### Build Types Explained

| Type | Stages | Use Case |
|------|--------|----------|
| **build** | Dependencies → Build → Docker Image | Quick build without tests |
| **test** | Dependencies → Tests | Run tests only |
| **deploy** | Build → Docker Image → Deploy | Build and deploy |
| **full** | All stages | Complete CI/CD pipeline |

### Monitoring Builds

- **Console Output**: Click build number → Console Output
- **Blue Ocean**: Better visualization (install Blue Ocean plugin)
- **Build History**: See all builds on the left sidebar

## 🛠️ Customizing Pipelines

### Modify Service-Specific Settings

Edit the Jenkinsfile in each service directory:

```groovy
// Example: auth/Jenkinsfile

environment {
    SERVICE_NAME = "auth"
    SERVICE_PATH = "auth"
    SERVICE_PORT = "8081"

    // Add custom environment variables
    CUSTOM_VAR = "value"
}
```

### Add Custom Stages

Add stages to the Jenkinsfile:

```groovy
stage('Custom Stage') {
    steps {
        script {
            echo "Running custom stage..."
            sh 'your-command-here'
        }
    }
}
```

## 📊 Port Scanner

Use the port scanner to check available ports:

```bash
# Make executable
chmod +x jenkins-init/scan-ports.sh

# Run scanner
./jenkins-init/scan-ports.sh podman-compose.yml

# Scan specific range
./jenkins-init/scan-ports.sh podman-compose.yml 8000 9000
```

Output shows:
- Ports currently in use
- Services using each port
- Available ports in range
- Recommended ports for Jenkins

## 🔄 Jenkins Automation Script

Run the setup automation script:

```bash
# Make executable
chmod +x jenkins-init/setup-jenkins.sh

# Run setup
./jenkins-init/setup-jenkins.sh
```

This script:
- Waits for Jenkins to start
- Displays initial admin password
- Lists all services
- Generates job configuration XML files
- Provides setup instructions

## 🐳 Podman Commands

```bash
# Start Jenkins
podman-compose up -d jenkins

# Stop Jenkins
podman-compose stop jenkins

# Restart Jenkins
podman-compose restart jenkins

# View logs
podman-compose logs -f jenkins

# Remove Jenkins (keeps data)
podman-compose down jenkins

# Remove Jenkins and data
podman-compose down -v jenkins
podman volume rm eadgequry-ai_jenkins-data

# Execute command in Jenkins container
podman exec -it jenkins-cicd bash

# Get initial password
podman exec jenkins-cicd cat /var/jenkins_home/secrets/initialAdminPassword
```

## 📁 Directory Structure

```
Eadgequry-Ai/
├── podman-compose.yml          # Jenkins service defined here
├── JENKINS_README.md            # This file
├── jenkins-init/
│   ├── scan-ports.sh           # Port scanner script
│   └── setup-jenkins.sh        # Jenkins automation script
├── jenkins-jobs/
│   └── Jenkinsfile.template    # Reusable template
├── auth/
│   └── Jenkinsfile             # Auth service pipeline
├── api-gatway/
│   └── Jenkinsfile             # API Gateway pipeline
├── chat-bot-service/
│   └── Jenkinsfile             # Chatbot pipeline
├── data-source/
│   └── Jenkinsfile             # Data source pipeline
├── user-profile/
│   └── Jenkinsfile             # User profile pipeline
├── notification/
│   └── Jenkinsfile             # Notification pipeline
└── naming-server/
    └── Jenkinsfile             # Eureka server pipeline
```

## 🔐 Security Notes

1. **Change default credentials** after initial setup
2. **Enable security** in Jenkins settings
3. **Use credentials plugin** for sensitive data
4. **Configure role-based access control** for team members
5. **Regularly backup** Jenkins data volume

## 🐛 Troubleshooting

### Jenkins won't start

```bash
# Check logs
podman-compose logs jenkins

# Check if port is in use
lsof -i :8082

# Try different port in podman-compose.yml
```

### Pipeline fails

1. Check console output for errors
2. Verify Jenkinsfile syntax
3. Check Docker socket access: `podman ps` inside container
4. Verify Maven/Java installation
5. Check network connectivity to services

### Can't access SonarQube

```bash
# Ensure SonarQube is running
podman-compose ps sonarqube

# Check SonarQube logs
podman-compose logs sonarqube

# Verify network
podman network inspect eadgequry-net
```

### Build fails with "Permission denied"

```bash
# Fix Docker socket permissions
podman exec -it jenkins-cicd chown root:root /var/run/docker.sock
```

## 📝 Best Practices

1. **Use branches**: Create feature branches for development
2. **Run tests locally** before pushing
3. **Review console output** for warnings
4. **Use SonarQube** to maintain code quality
5. **Schedule builds** for regular integration
6. **Tag Docker images** with build numbers
7. **Backup Jenkins** configuration regularly

## 🔗 Useful Links

- Jenkins Documentation: https://www.jenkins.io/doc/
- Pipeline Syntax: https://www.jenkins.io/doc/book/pipeline/syntax/
- Docker Pipeline Plugin: https://plugins.jenkins.io/docker-workflow/
- SonarQube Integration: https://docs.sonarqube.org/latest/analysis/scan/sonarscanner-for-jenkins/

## 📞 Support

For issues or questions:
1. Check Jenkins console output
2. Review pipeline logs
3. Check service-specific Jenkinsfiles
4. Consult EadgeQuery documentation

## 📈 Next Steps

1. **Set up webhooks**: Auto-trigger builds on Git push
2. **Configure notifications**: Slack/Email on build status
3. **Add deployment stages**: Deploy to staging/production
4. **Implement blue-green deployment**: Zero-downtime deployments
5. **Add security scans**: OWASP dependency check
6. **Create shared libraries**: Reuse common pipeline code
7. **Configure backup strategy**: Automate Jenkins backups

---

**Version**: 1.0
**Last Updated**: 2024
**Maintained by**: EadgeQuery DevOps Team
