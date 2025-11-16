# Jenkins Multi-Repository Setup Guide for EadgeQuery

## 📋 Overview

This guide shows you how to set up Jenkins for a **microservices architecture** where each service has its own Git repository (NOT a monorepo).

## ✅ What's Been Fixed

### 1. **Created Missing Dockerfiles**
All services now have Dockerfiles:
- ✓ `auth/Dockerfile`
- ✓ `api-gatway/Dockerfile`
- ✓ `naming-server/Dockerfile`
- ✓ `chat-bot-service/Dockerfile` (NEW)
- ✓ `data-source/Dockerfile` (NEW)
- ✓ `user-profile/Dockerfile` (NEW)
- ✓ `notification/Dockerfile` (NEW)

### 2. **Updated Jenkinsfiles for Separate Repos**
- ✓ Removed `@Library('eadgequry-shared')` dependency
- ✓ Removed `SERVICE_PATH` (services are now at root of their own repos)
- ✓ Updated all `dir("${SERVICE_PATH}")` commands to work at root level
- ✓ Changed `DEPLOY_TO_COMPOSE` to `BUILD_DOCKER` (more appropriate for CI/CD)
- ✓ Added proper test reporting with JUnit
- ✓ Set SonarQube to optional (defaultValue: false)

### 3. **Services Ready for Separation**
```
✓ auth              - Ready for own repository
✓ api-gatway        - Ready for own repository
✓ naming-server     - Ready for own repository
✓ chat-bot-service  - Ready for own repository
✓ data-source       - Ready for own repository
✓ user-profile      - Ready for own repository
✓ notification      - Ready for own repository
```

---

## 🗂️ Repository Structure (Recommended)

### Option 1: Separate Repositories (Recommended for Microservices)

Create individual repositories:
```
GitHub/
├── eadgequry-auth/
│   ├── src/
│   ├── pom.xml
│   ├── Dockerfile
│   ├── Jenkinsfile
│   └── README.md
│
├── eadgequry-api-gateway/
│   ├── src/
│   ├── pom.xml
│   ├── Dockerfile
│   ├── Jenkinsfile
│   └── README.md
│
├── eadgequry-chat-bot/
│   └── ...
│
└── eadgequry-infrastructure/
    ├── docker-compose.yml
    ├── jenkins/
    └── scripts/
```

**Benefits:**
- ✅ Independent versioning per service
- ✅ Smaller repositories (faster clone/build)
- ✅ Better access control (team-specific repos)
- ✅ True microservice autonomy
- ✅ Easier CI/CD per service

### Option 2: Keep Monorepo (Current Setup)

Keep everything in one repository but configure Jenkins to build specific paths:
```
Eadgequry-Ai/
├── auth/
├── api-gatway/
├── chat-bot-service/
├── data-source/
├── user-profile/
├── notification/
├── naming-server/
└── docker-compose.yml
```

**Benefits:**
- ✅ Single source of truth
- ✅ Easier to coordinate changes across services
- ✅ Simpler for small teams

---

## 🚀 Setup Instructions

### Step 1: Start Jenkins Locally

```bash
# Start Jenkins with Docker Compose
docker-compose up -d jenkins

# Wait for Jenkins to start (60-90 seconds)
docker-compose logs -f jenkins

# Get initial admin password
docker exec jenkins-cicd cat /var/jenkins_home/secrets/initialAdminPassword
```

### Step 2: Access and Configure Jenkins

1. Open: `http://localhost:8082/jenkins`
2. Enter the initial admin password
3. **Install Suggested Plugins** + these additional:
   - Docker Pipeline
   - Pipeline Maven Integration
   - Git Plugin
   - SonarQube Scanner (optional)
   - GitHub Integration (if using GitHub)

4. Create admin user
5. Jenkins URL: `http://localhost:8082/jenkins`

### Step 3: Configure Jenkins Tools

**Java Configuration:**
1. Go to: **Manage Jenkins** → **Global Tool Configuration**
2. Under **JDK**:
   - Click **Add JDK**
   - Name: `JDK17`
   - Install automatically OR specify path: `/usr/lib/jvm/java-17-openjdk-amd64`

**Maven Configuration:**
1. Under **Maven**:
   - Click **Add Maven**
   - Name: `Maven3`
   - Install automatically OR specify path

**Docker Configuration:**
1. Under **Docker**:
   - Click **Add Docker**
   - Name: `Docker`
   - Install automatically: checked

### Step 4: Create Pipeline Jobs

#### For Separate Repositories:

For **each service**, create a pipeline job:

1. Click **New Item**
2. Enter name: `auth-service` (or `api-gateway-service`, etc.)
3. Select: **Pipeline**
4. Click **OK**

5. **Configure Pipeline:**
   - ☑ **This project is parameterized** (automatically added from Jenkinsfile)
   - **Pipeline** section:
     - Definition: **Pipeline script from SCM**
     - SCM: **Git**
     - Repository URL: `https://github.com/YourOrg/eadgequry-auth.git`
     - Credentials: (add GitHub credentials if private)
     - Branch Specifier: `*/main`
     - Script Path: `Jenkinsfile` (at root of repo)

6. Click **Save**

Repeat for all services:
- `auth-service` → `https://github.com/YourOrg/eadgequry-auth.git`
- `api-gateway-service` → `https://github.com/YourOrg/eadgequry-api-gateway.git`
- `chat-bot-service` → `https://github.com/YourOrg/eadgequry-chatbot.git`
- `data-source-service` → `https://github.com/YourOrg/eadgequry-datasource.git`
- `user-profile-service` → `https://github.com/YourOrg/eadgequry-user-profile.git`
- `notification-service` → `https://github.com/YourOrg/eadgequry-notification.git`
- `naming-server-service` → `https://github.com/YourOrg/eadgequry-naming-server.git`

#### For Monorepo (Current Setup):

If keeping the monorepo, configure each job to use the same repo but different Jenkinsfile paths:

- `auth-pipeline` → Script Path: `auth/Jenkinsfile`
- `api-gateway-pipeline` → Script Path: `api-gatway/Jenkinsfile`
- `chat-bot-pipeline` → Script Path: `chat-bot-service/Jenkinsfile`
- etc.

---

## 🧪 Testing Jenkins Pipelines

### Test 1: Quick Build Test (No Tests, No Docker)

1. Go to a pipeline (e.g., `auth-service`)
2. Click **Build with Parameters**
3. Select:
   - BUILD_TYPE: **build**
   - RUN_SONAR: **unchecked**
   - RUN_TESTS: **unchecked**
   - BUILD_DOCKER: **unchecked**
4. Click **Build**
5. Check **Console Output** for:
   ```
   ✅ BUILD SUCCESS
   🏗️ Building auth...
   [INFO] BUILD SUCCESS
   ```

### Test 2: Full Test Pipeline

1. Build with Parameters:
   - BUILD_TYPE: **full**
   - RUN_SONAR: **unchecked** (unless SonarQube is running)
   - RUN_TESTS: **checked**
   - BUILD_DOCKER: **checked**
2. Should see all stages complete:
   - ✅ Checkout
   - ✅ Environment Info
   - ✅ Run Tests
   - ✅ Build Application
   - ✅ Build Docker Image

### Test 3: Local Manual Test (Before Jenkins)

Test that each service can build locally:

```bash
# Test auth service
cd auth
./mvnw clean package -DskipTests
docker build -t eadgequry/auth:test .

# Test api-gateway
cd ../api-gatway
./mvnw clean package -DskipTests
docker build -t eadgequry/api-gatway:test .

# Repeat for other services...
```

---

## 📊 Pipeline Parameters Explained

Each Jenkinsfile supports these build parameters:

| Parameter | Options | Description |
|-----------|---------|-------------|
| **BUILD_TYPE** | build, test, deploy, full | Controls which stages run |
| **RUN_SONAR** | true/false | Enable SonarQube code analysis |
| **RUN_TESTS** | true/false | Run unit tests (default: true) |
| **BUILD_DOCKER** | true/false | Build Docker image (default: true) |

### Build Type Matrix:

| BUILD_TYPE | Stages Executed |
|------------|----------------|
| **build** | Build Application → SonarQube (if enabled) → Build Docker (if enabled) |
| **test** | Run Tests only |
| **deploy** | Build Application → Build Docker |
| **full** | All stages (Tests → Build → SonarQube → Docker) |

---

## 🔄 How to Split Monorepo into Separate Repos

### Method 1: Git Filter-Branch (Preserves History)

For each service:

```bash
# Clone the monorepo
git clone https://github.com/Asyassin10/Eadgequry-Ai.git eadgequry-auth
cd eadgequry-auth

# Filter to keep only auth directory
git filter-branch --subdirectory-filter auth -- --all

# Move files to root (they're already there after filter-branch)
# Add new remote
git remote remove origin
git remote add origin https://github.com/YourOrg/eadgequry-auth.git

# Push to new repo
git push -u origin main
```

Repeat for each service: `api-gatway`, `chat-bot-service`, `data-source`, etc.

### Method 2: Fresh Start (Simpler, No History)

For each service:

```bash
# Create new repo on GitHub first
# Then:
cd auth
git init
git add .
git commit -m "Initial commit: Auth service"
git remote add origin https://github.com/YourOrg/eadgequry-auth.git
git push -u origin main
```

---

## 🐳 Docker Registry (Optional)

If you want Jenkins to push images to a registry:

### 1. Configure Docker Hub Credentials

1. Go to: **Manage Jenkins** → **Manage Credentials**
2. Click: **Global** → **Add Credentials**
3. Select: **Username with password**
4. ID: `docker-hub-credentials`
5. Username: (your Docker Hub username)
6. Password: (your Docker Hub token)

### 2. Update Jenkinsfile

Uncomment the "Push Docker Image" stage in each Jenkinsfile:

```groovy
stage('Push Docker Image') {
    when {
        expression {
            params.BUILD_DOCKER &&
            params.BUILD_TYPE in ['deploy', 'full']
        }
    }
    steps {
        script {
            echo "📤 Pushing Docker image to registry..."
            docker.withRegistry('https://registry.hub.docker.com', 'docker-hub-credentials') {
                sh "docker push ${DOCKER_IMAGE}:${DOCKER_TAG}"
                sh "docker push ${DOCKER_IMAGE}:latest"
            }
        }
    }
}
```

### 3. Set Environment Variable

In Jenkins job configuration or Jenkinsfile:
```groovy
environment {
    DOCKER_REGISTRY_CREDENTIALS = 'docker-hub-credentials'
}
```

---

## 🔐 Jenkins Security Best Practices

1. **Enable CSRF Protection**: Manage Jenkins → Configure Global Security
2. **Use Role-Based Access Control**: Install "Role-based Authorization Strategy" plugin
3. **Secure Credentials**: Never hardcode passwords in Jenkinsfiles
4. **Enable Audit Trail**: Install "Audit Trail" plugin
5. **Regular Backups**: Backup `/var/jenkins_home` volume

---

## 🛠️ Troubleshooting

### Issue: "Maven not found"

**Solution:**
```bash
# Enter Jenkins container
docker exec -it jenkins-cicd bash

# Install Maven
apt-get update
apt-get install -y maven

# Or use Maven wrapper (already in each service)
# Jenkins will use ./mvnw automatically
```

### Issue: "Docker command not found in Jenkins"

**Solution:**
```bash
# Check Docker socket is mounted
docker exec jenkins-cicd ls -la /var/run/docker.sock

# Fix permissions if needed
docker exec -u root jenkins-cicd chown jenkins:docker /var/run/docker.sock
```

### Issue: "Tests fail in Jenkins but pass locally"

**Solution:**
- Check Java version: Jenkins must use Java 17
- Check Maven settings: Ensure same Maven version
- Check environment variables: DB connections, etc.
- View Console Output for specific error

### Issue: "Pipeline doesn't trigger on Git push"

**Solution (for GitHub):**
1. Install "GitHub Integration" plugin
2. Configure webhook in GitHub repo:
   - URL: `http://your-jenkins-url/github-webhook/`
   - Content type: `application/json`
   - Events: `Push events`

---

## 📝 Next Steps

### For Separate Repositories:
1. ✅ **Create GitHub/GitLab repositories** for each service
2. ✅ **Split the monorepo** using one of the methods above
3. ✅ **Push each service** to its own repository
4. ✅ **Create Jenkins jobs** for each service repo
5. ✅ **Test pipelines** with "Build with Parameters"
6. ✅ **Configure webhooks** for auto-build on git push

### For Monorepo (Keep Current Setup):
1. ✅ **Create Jenkins jobs** pointing to different Jenkinsfile paths
2. ✅ **Update Jenkinsfiles** to work from subdirectories (already done)
3. ✅ **Test pipelines** with "Build with Parameters"
4. ✅ **Configure path-based triggers** (only build changed services)

---

## 📦 Quick Reference Commands

```bash
# Start Jenkins
docker-compose up -d jenkins

# Stop Jenkins
docker-compose stop jenkins

# View Jenkins logs
docker-compose logs -f jenkins

# Get admin password
docker exec jenkins-cicd cat /var/jenkins_home/secrets/initialAdminPassword

# Enter Jenkins container
docker exec -it jenkins-cicd bash

# Restart Jenkins
docker-compose restart jenkins

# Build service locally
cd <service-name>
./mvnw clean package
docker build -t eadgequry/<service-name>:test .

# Test all services
for dir in auth api-gatway chat-bot-service data-source user-profile notification naming-server; do
    echo "Testing $dir..."
    cd $dir && ./mvnw clean test && cd ..
done
```

---

## 🎯 Summary

**What's Fixed:**
- ✅ All services have Dockerfiles
- ✅ All Jenkinsfiles work for separate repos (no SERVICE_PATH)
- ✅ Removed dependency on shared library
- ✅ Proper test reporting and SonarQube integration

**What You Need to Do:**
1. Decide: Separate repos OR keep monorepo
2. Start Jenkins locally
3. Create pipeline jobs in Jenkins
4. Test builds
5. (Optional) Configure Docker registry
6. (Optional) Set up GitHub webhooks

**Your Jenkins is ready for 100% local testing!** 🚀

