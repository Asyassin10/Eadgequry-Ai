# 🔧 FIX JENKINS - EXACT STEP-BY-STEP GUIDE

## ❌ YOUR PROBLEM:
```
Port mapping: "8982:8980" ← WRONG!
Jenkins runs on port 8080 INSIDE container, not 8980
```

---

## ✅ SOLUTION - FOLLOW THESE EXACT STEPS:

### STEP 1: Stop Jenkins
```bash
cd /path/to/Eadgequry-Ai

# Stop Jenkins
podman-compose stop jenkins

# Remove the container
podman-compose rm -f jenkins

# Or use podman compose v2:
podman compose stop jenkins
podman compose rm -f jenkins
```

### STEP 2: Fix podman-compose.yml

Open `podman-compose.yml` and find the Jenkins section (around line 280).

**CHANGE THIS:**
```yaml
ports:
  - "8982:8980"  # ❌ WRONG - Jenkins doesn't run on 8980
```

**TO THIS:**
```yaml
ports:
  - "8082:8080"  # ✅ CORRECT - Jenkins runs on 8080 inside container
```

**OR if you want to use port 8082 on your machine:**
```yaml
ports:
  - "8082:8080"  # Host port 8082 → Container port 8080
```

**Complete Jenkins section should look like this:**
```yaml
  jenkins:
    image: jenkins/jenkins:lts-jdk17
    container_name: jenkins-cicd
    user: root
    privileged: true
    ports:
      - "8082:8080"      # ✅ Correct mapping
      - "50000:50000"
    volumes:
      - jenkins-data:/var/jenkins_home
      - /run/podman/podman.sock:/var/run/docker.sock
      - ./jenkins-jobs:/var/jenkins_jobs
    environment:
      - JENKINS_OPTS=--prefix=/jenkins
      - JAVA_OPTS=-Djenkins.install.runSetupWizard=false
    networks:
      - edagequry-net
    healthcheck:
      test: ["CMD-SHELL", "curl -f http://localhost:8080/jenkins/login || exit 1"]
      interval: 30s
      timeout: 10s
      retries: 5
      start_period: 90s
```

### STEP 3: Start Jenkins
```bash
# Start Jenkins
podman-compose up -d jenkins

# Or with podman compose v2:
podman compose up -d jenkins
```

### STEP 4: Watch the Logs
```bash
# Watch Jenkins startup
podman-compose logs -f jenkins

# Or:
podman compose logs -f jenkins

# Wait until you see:
# "Jenkins is fully up and running"
```

### STEP 5: Get Admin Password
**Open a NEW terminal** and run:
```bash
podman exec jenkins-cicd cat /var/jenkins_home/secrets/initialAdminPassword
```

**Copy the password** - you'll need it!

### STEP 6: Access Jenkins

Open your browser:
```
http://localhost:8082/jenkins
```

**NOT:**
- ❌ http://localhost:8082/ (missing /jenkins)
- ❌ http://localhost:8080/jenkins (wrong port)
- ❌ http://localhost:8982/jenkins (wrong port)

**CORRECT:**
- ✅ http://localhost:8082/jenkins

---

## 🧪 COMPLETE TEST PROCEDURE

### TEST 1: Check Jenkins is Running
```bash
# Check container status
podman ps | grep jenkins

# You should see:
# jenkins-cicd ... Up ... 0.0.0.0:8082->8080/tcp
```

### TEST 2: Check Jenkins Health
```bash
# Check health status
podman inspect jenkins-cicd | grep -A 5 Health

# Should show "healthy"
```

### TEST 3: Access Jenkins UI

1. **Open Browser:** `http://localhost:8082/jenkins`

2. **You should see:**
   - "Unlock Jenkins" page
   - Field asking for admin password

3. **Paste the admin password** you got from Step 5

4. **Click "Continue"**

5. **Select "Install suggested plugins"**

6. **Wait for plugins to install** (2-3 minutes)

7. **Create admin user:**
   - Username: `admin`
   - Password: (your choice)
   - Full name: `Admin`
   - Email: `admin@localhost`

8. **Jenkins URL:** Keep as `http://localhost:8082/jenkins`

9. **Click "Start using Jenkins"**

---

## 🎯 TEST A PIPELINE

### TEST 1: Create Your First Pipeline

1. **Click "New Item"**

2. **Enter name:** `test-auth-pipeline`

3. **Select:** Pipeline

4. **Click OK**

5. **Scroll to "Pipeline" section**

6. **Definition:** Pipeline script from SCM

7. **SCM:** Git

8. **Repository URL:** `https://github.com/Asyassin10/Eadgequry-Ai.git`

9. **Branch:** `*/main`

10. **Script Path:** `auth/Jenkinsfile`

11. **Click "Save"**

### TEST 2: Run the Pipeline

1. **Click "Build with Parameters"**

2. **Set these values:**
   - BUILD_TYPE: **build**
   - RUN_SONAR: **unchecked** ❌
   - RUN_TESTS: **checked** ✅
   - BUILD_DOCKER: **checked** ✅

3. **Click "Build"**

4. **Click on the build number** (e.g., #1) in the left sidebar

5. **Click "Console Output"**

6. **You should see:**
   ```
   ╔════════════════════════════════════════╗
   ║   AUTH Service Pipeline
   ╠════════════════════════════════════════╣
   ║ Build: #1
   ║ Branch: main
   ║ Type: build
   ╚════════════════════════════════════════╝

   [... build output ...]

   ✅ auth pipeline succeeded!
   Finished: SUCCESS
   ```

---

## 🐛 TROUBLESHOOTING

### Error: "Connection refused" or "Cannot connect"

**Fix:**
```bash
# Stop everything
podman-compose down

# Start Jenkins only
podman-compose up -d jenkins

# Wait and watch logs
podman-compose logs -f jenkins
```

### Error: "500 Internal Server Error"

**Cause:** Port mapping is wrong

**Fix:** Go back to STEP 2 and make sure port is `8082:8080` NOT `8082:8980`

### Error: "404 Not Found"

**Cause:** Missing `/jenkins` in URL

**Fix:** Use `http://localhost:8082/jenkins` (with /jenkins at end)

### Error: Jenkins keeps restarting

**Check logs:**
```bash
podman-compose logs jenkins | tail -50
```

**Common causes:**
- Port already in use → Change `8082` to `8083` or another free port
- Podman socket permission denied → Run: `sudo chmod 666 /run/podman/podman.sock`

### Error: "Maven not found" during build

**Fix:**
```bash
# Enter Jenkins container
podman exec -it jenkins-cicd bash

# Install Maven
apt-get update
apt-get install -y maven

# Exit
exit

# Or Jenkins will use ./mvnw from each service automatically
```

---

## 📋 QUICK REFERENCE

### Start Jenkins
```bash
podman-compose up -d jenkins
```

### Stop Jenkins
```bash
podman-compose stop jenkins
```

### Restart Jenkins
```bash
podman-compose restart jenkins
```

### View Logs
```bash
podman-compose logs -f jenkins
```

### Get Admin Password
```bash
podman exec jenkins-cicd cat /var/jenkins_home/secrets/initialAdminPassword
```

### Access Jenkins
```
http://localhost:8082/jenkins
```

### Check Status
```bash
podman ps | grep jenkins
```

### Completely Reset Jenkins
```bash
podman-compose down
podman volume rm eadgequry-ai_jenkins-data
podman-compose up -d jenkins
```

---

## ✅ SUCCESS CHECKLIST

After following all steps, you should have:

- [ ] Jenkins accessible at `http://localhost:8082/jenkins`
- [ ] No 500 errors
- [ ] Can login with admin credentials
- [ ] Plugins installed
- [ ] Created a test pipeline
- [ ] Pipeline builds successfully
- [ ] Can see "SUCCESS" in console output

**If all checked ✅ - YOUR JENKINS IS WORKING 100%!** 🎉

---

## 🚀 NEXT STEPS

1. **Create pipelines for all services:**
   - auth-pipeline
   - api-gateway-pipeline
   - chatbot-pipeline
   - datasource-pipeline
   - user-profile-pipeline
   - notification-pipeline
   - naming-server-pipeline

2. **Test each service build**

3. **Configure webhooks** (optional) for auto-build on git push

4. **Set up SonarQube** (optional) if you want code quality analysis

---

**Need help?** Check the logs:
```bash
podman-compose logs jenkins | grep -i error
```
