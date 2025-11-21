# 📊 Test Coverage & SonarQube Guide

Complete guide for generating test coverage reports and uploading to SonarQube using Podman.

## 🚀 Quick Start

### Prerequisites
1. **Start SonarQube** (if not already running):
   ```bash
   podman-compose up -d sonarqube sonarqube-db
   ```

2. **Wait for SonarQube to be ready** (takes 1-2 minutes):
   ```bash
   # Check status
   curl http://localhost:9000/api/system/status

   # Should return: {"status":"UP"}
   ```

3. **Access SonarQube**:
   - URL: http://localhost:9000
   - Default credentials: `admin / admin`
   - Change password on first login

## 📋 Method 1: Using Shell Script (Recommended)

### Run All Services
```bash
./run-coverage.sh
```

### Run Specific Service
```bash
# Auth service only
./run-coverage.sh --service auth

# Profile service only
./run-coverage.sh --service user-profile

# Notification service only
./run-coverage.sh --service notification
```

### Generate Coverage Only (No SonarQube Upload)
```bash
./run-coverage.sh --coverage-only
```

## 🐳 Method 2: Using Podman Compose

### Step 1: Create Podman Network (if not exists)
```bash
podman network create edagequry-net 2>/dev/null || true
```

### Step 2: Run Scanner for Each Service

#### Auth Service
```bash
podman-compose -f podman-compose.scanner.yml run --rm auth-scanner
```

#### Profile Service
```bash
podman-compose -f podman-compose.scanner.yml run --rm profile-scanner
```

#### Notification Service
```bash
podman-compose -f podman-compose.scanner.yml run --rm notification-scanner
```

### Run All Services (Sequential)
```bash
podman-compose -f podman-compose.scanner.yml run --rm auth-scanner && \
podman-compose -f podman-compose.scanner.yml run --rm profile-scanner && \
podman-compose -f podman-compose.scanner.yml run --rm notification-scanner
```

## 📦 Method 3: Using Maven Directly

### Prerequisites
- Java 21 installed
- Maven 3.9+ installed

### Run for Each Service

```bash
# Auth Service
cd auth
mvn clean test jacoco:report
mvn sonar:sonar
cd ..

# Profile Service
cd user-profile
mvn clean test jacoco:report
mvn sonar:sonar
cd ..

# Notification Service
cd notification
mvn clean test jacoco:report
mvn sonar:sonar
cd ..
```

## 🎯 Method 4: One-Line Podman Command

Run tests and upload to SonarQube using Podman:

### Auth Service
```bash
podman run --rm \
  --network edagequry-net \
  -v $(pwd)/auth:/workspace \
  -w /workspace \
  maven:3.9.6-eclipse-temurin-21 \
  bash -c "mvn clean test jacoco:report && mvn sonar:sonar -Dsonar.host.url=http://sonarqube:9000"
```

### Profile Service
```bash
podman run --rm \
  --network edagequry-net \
  -v $(pwd)/user-profile:/workspace \
  -w /workspace \
  maven:3.9.6-eclipse-temurin-21 \
  bash -c "mvn clean test jacoco:report && mvn sonar:sonar -Dsonar.host.url=http://sonarqube:9000"
```

### Notification Service
```bash
podman run --rm \
  --network edagequry-net \
  -v $(pwd)/notification:/workspace \
  -w /workspace \
  maven:3.9.6-eclipse-temurin-21 \
  bash -c "mvn clean test jacoco:report && mvn sonar:sonar -Dsonar.host.url=http://sonarqube:9000"
```

## 📈 View Results

### 1. Local Coverage Reports

Open HTML reports in browser:

```bash
# Auth Service
xdg-open auth/target/site/jacoco/index.html

# Profile Service
xdg-open user-profile/target/site/jacoco/index.html

# Notification Service
xdg-open notification/target/site/jacoco/index.html
```

### 2. SonarQube Dashboard

Visit: http://localhost:9000

You should see 3 projects:
- **eadgequry-auth** - Auth Service
- **eadgequry-user-profile** - Profile Service
- **eadgequry-notification** - Notification Service

## 🔧 Configuration Details

### Coverage Tools Used
| Service | Project Key | JaCoCo Version | Min Coverage |
|---------|------------|----------------|--------------|
| Auth | `eadgequry-auth` | 0.8.11 | 90% |
| Profile | `eadgequry-user-profile` | 0.8.11 | 90% |
| Notification | `eadgequry-notification` | 0.8.11 | 90% |

### Files Excluded from Coverage
- Application main classes
- Configuration classes
- DTOs and Records
- Model/Entity classes (getters/setters)
- Repository interfaces
- Utility classes
- Templates (Notification service)

## 🐛 Troubleshooting

### SonarQube Not Starting
```bash
# Check logs
podman-compose logs sonarqube

# Restart services
podman-compose restart sonarqube sonarqube-db
```

### Network Issues
```bash
# Create network manually
podman network create edagequry-net

# Verify network exists
podman network ls | grep edagequry-net
```

### Maven Dependencies Not Downloading
```bash
# Clear Maven cache
rm -rf ~/.m2/repository

# Or use Podman volume
podman volume rm maven-cache
```

### Tests Failing
```bash
# Run tests locally without coverage
cd auth
mvn clean test

# Check test reports
cat auth/target/surefire-reports/*.txt
```

### SonarQube Authentication Required
```bash
# Generate token in SonarQube UI
# My Account → Security → Generate Token

# Use token with Maven
mvn sonar:sonar -Dsonar.login=YOUR_TOKEN_HERE
```

## 📊 Coverage Reports Location

After running tests, find reports at:

```
auth/
├── target/
│   └── site/
│       └── jacoco/
│           ├── index.html          # HTML report
│           └── jacoco.xml          # XML report for SonarQube

user-profile/
├── target/
│   └── site/
│       └── jacoco/
│           ├── index.html
│           └── jacoco.xml

notification/
├── target/
│   └── site/
│       └── jacoco/
│           ├── index.html
│           └── jacoco.xml
```

## 🎓 Understanding Coverage Metrics

### Line Coverage
Percentage of code lines executed during tests

### Branch Coverage
Percentage of decision branches (if/else, switch) tested

### Complexity
Cyclomatic complexity - number of independent paths

### Target Coverage
- **Overall**: 90%+
- **Service Layer**: 95%+
- **Controller Layer**: 90%+
- **Exception Handlers**: 100%

## 📝 Best Practices

1. **Run tests before committing**
   ```bash
   ./run-coverage.sh --coverage-only
   ```

2. **Check coverage locally first**
   ```bash
   cd auth && mvn clean test jacoco:report
   xdg-open target/site/jacoco/index.html
   ```

3. **Upload to SonarQube regularly**
   ```bash
   ./run-coverage.sh
   ```

4. **Review SonarQube issues**
   - Fix bugs
   - Address code smells
   - Improve test coverage

## 🔗 Useful Links

- **SonarQube Dashboard**: http://localhost:9000
- **SonarQube Docs**: https://docs.sonarqube.org
- **JaCoCo Docs**: https://www.jacoco.org/jacoco/trunk/doc/

## 💡 Tips

- Use `--coverage-only` during development to save time
- Run full scan before creating pull requests
- Check SonarQube quality gate status
- Aim for "A" rating in all projects
- Keep technical debt under 5%

## 🎯 CI/CD Integration

To integrate with CI/CD pipeline:

```yaml
# Example GitHub Actions
- name: Run Tests and Coverage
  run: ./run-coverage.sh

- name: Upload to SonarQube
  env:
    SONAR_TOKEN: ${{ secrets.SONAR_TOKEN }}
  run: |
    mvn sonar:sonar \
      -Dsonar.host.url=http://sonarqube:9000 \
      -Dsonar.login=$SONAR_TOKEN
```

---

**Need Help?** Check the troubleshooting section or open an issue in the repository.
