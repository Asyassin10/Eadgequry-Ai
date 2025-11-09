# Jenkins Quick Start Guide

## 🚀 Get Jenkins Running in 5 Minutes

### Step 1: Check Ports (Optional)
```bash
./jenkins-init/scan-ports.sh
```

### Step 2: Start Jenkins
```bash
docker-compose up -d jenkins
```

### Step 3: Get Admin Password
```bash
# Wait 60-90 seconds for Jenkins to start, then:
docker exec jenkins-cicd cat /var/jenkins_home/secrets/initialAdminPassword
```

Copy the password output.

### Step 4: Access Jenkins
1. Open: `http://localhost:8082/jenkins`
2. Paste the admin password
3. Click **Install suggested plugins**
4. Wait for plugins to install (~3-5 minutes)
5. Create admin user or skip to use admin

### Step 5: Add Extra Plugins
1. Go to **Manage Jenkins** → **Manage Plugins**
2. Click **Available** tab
3. Search and install:
   - Docker Pipeline
   - Pipeline Maven Integration
   - SonarQube Scanner
4. Click **Install without restart**

### Step 6: Create Your First Pipeline

**Example: Auth Service Pipeline**

1. Click **New Item**
2. Name: `auth-pipeline`
3. Type: **Pipeline**
4. Click **OK**

5. Under **Pipeline** section:
   - Definition: **Pipeline script from SCM**
   - SCM: **Git**
   - Repository URL: `https://github.com/Asyassin10/Eadgequry-Ai.git`
   - Branch: `*/main`
   - Script Path: `auth/Jenkinsfile`

6. Click **Save**

### Step 7: Run Your First Build

1. Click **Build with Parameters**
2. Select:
   - BUILD_TYPE: `build` (for first test)
   - RUN_SONAR: `false` (we'll set this up later)
   - RUN_TESTS: `true`
   - DEPLOY_TO_COMPOSE: `false`
3. Click **Build**
4. Watch the build progress!

## ✅ Success Checklist

- [ ] Jenkins is accessible at http://localhost:8082/jenkins
- [ ] Initial setup completed
- [ ] Plugins installed
- [ ] First pipeline created
- [ ] First build successful

## 🎯 Next Steps

1. **Create pipelines for other services**:
   - api-gatway-pipeline
   - chat-bot-service-pipeline
   - data-source-pipeline
   - user-profile-pipeline
   - notification-pipeline
   - naming-server-pipeline

2. **Configure SonarQube** (see JENKINS_README.md)

3. **Set up automatic builds** with Git webhooks

## 📊 Service Dashboard

Once you've created all pipelines, you'll see this in Jenkins:

```
Jenkins Home
├── 📦 auth-pipeline              [Port: 8081]
├── 🌐 api-gatway-pipeline       [Port: 8765]
├── 💬 chat-bot-service-pipeline [Port: 8086]
├── 📊 data-source-pipeline      [Port: 8087]
├── 👤 user-profile-pipeline     [Port: 8088]
├── 📧 notification-pipeline     [Port: 8089]
└── 🔍 naming-server-pipeline    [Port: 8761]
```

## 🆘 Common Issues

**Problem**: Can't access Jenkins
```bash
# Check if running
docker ps | grep jenkins

# Check logs
docker-compose logs jenkins
```

**Problem**: Build fails
```bash
# Check console output in Jenkins UI
# Verify service has Dockerfile and Jenkinsfile
```

**Problem**: "Permission denied" for Docker
```bash
# Run in Jenkins container
docker exec -it jenkins-cicd bash
docker ps  # Should work
```

## 📚 Full Documentation

For complete documentation, see: `JENKINS_README.md`

## 🎉 You're All Set!

Jenkins is now ready to manage your CI/CD pipelines for all EadgeQuery microservices!
