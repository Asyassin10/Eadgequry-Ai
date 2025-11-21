# SonarQube Setup and Configuration Guide

Complete guide for running SonarQube code quality analysis on Eadgequry microservices.

---

## Table of Contents
1. [Overview](#overview)
2. [Prerequisites](#prerequisites)
3. [Start SonarQube](#start-sonarqube)
4. [Initial SonarQube Setup](#initial-sonarqube-setup)
5. [Run Analysis - Auth Service](#run-analysis---auth-service)
6. [Commands Reference](#commands-reference)
7. [Configuration Files](#configuration-files)
8. [What Gets Analyzed](#what-gets-analyzed)
9. [Troubleshooting](#troubleshooting)
10. [Adding Other Services](#adding-other-services)

---

## Overview

**SonarQube** is a continuous code quality inspection tool that:
- ✅ Analyzes code for bugs, vulnerabilities, and code smells
- ✅ Tracks test coverage from JaCoCo reports
- ✅ Enforces coding standards and best practices
- ✅ Provides detailed reports and metrics
- ✅ Tracks quality gates and technical debt

**Architecture:**
```
┌─────────────────┐
│  Auth Service   │──┐
└─────────────────┘  │
                     │ Maven Sonar Scanner
┌─────────────────┐  │    ↓
│ Other Services  │──┼──> SonarQube Server (Port 9000)
└─────────────────┘  │    ↓
                     │ PostgreSQL Database
┌─────────────────┐  │
│  JaCoCo Reports │──┘
└─────────────────┘
```

---

## Prerequisites

- Podman and Podman Compose installed
- Java 21+ installed
- Maven installed
- Auth service with tests completed

---

## Start SonarQube

### 1. Start SonarQube Containers

```bash
# Start only SonarQube and its database
podman-compose up -d sonarqube-db sonarqube

# Or start all services including MySQL, Zipkin, etc.
podman-compose up -d
```

**Wait time:** SonarQube takes ~60-90 seconds to start completely

### 2. Check SonarQube Status

```bash
# View logs
podman logs -f sonarqube

# Wait for this message:
# "SonarQube is operational"
```

### 3. Verify SonarQube is Running

```bash
# Check health
curl http://localhost:9000/api/system/status

# Expected response:
# {"status":"UP"}
```

### 4. Access SonarQube Web Interface

Open in browser:
```
http://localhost:9000
```

**Default credentials:**
- Username: `admin`
- Password: `admin`

---

## Initial SonarQube Setup

### 1. First Login

1. Open `http://localhost:9000`
2. Login with `admin` / `admin`
3. You'll be prompted to change the password
4. **IMPORTANT:** Change to a new secure password

### 2. Create Authentication Token

1. Click your profile icon (top right) → **My Account**
2. Click **Security** tab
3. Enter token name: `eadgequry-maven`
4. Click **Generate**
5. **COPY THE TOKEN** (you can't see it again!)

Example token:
```
squ_1234567890abcdefghijklmnopqrstuvwxyz
```

### 3. Save Token for Later Use

```bash
# Save to environment variable (current session only)
export SONAR_TOKEN="squ_your_token_here"

# Or save to file for reuse
echo "squ_your_token_here" > ~/.sonar-token
chmod 600 ~/.sonar-token
```

---

## Run Analysis - Auth Service

### Method 1: Using Maven with Token (Recommended)

```bash
# Navigate to auth service
cd auth

# Run tests and generate coverage
./mvnw clean test

# Run SonarQube analysis
./mvnw sonar:sonar -Dsonar.login=$SONAR_TOKEN
```

### Method 2: Using Maven with Token from File

```bash
cd auth

# Run tests and generate coverage
./mvnw clean test

# Run SonarQube analysis with token from file
./mvnw sonar:sonar -Dsonar.login=$(cat ~/.sonar-token)
```

### Method 3: One Command (Test + Analyze)

```bash
cd auth

# Run tests and SonarQube analysis in one command
./mvnw clean verify sonar:sonar -Dsonar.login=$SONAR_TOKEN
```

### Method 4: Using sonar-project.properties (No Token Required for Local)

```bash
cd auth

# Run tests first
./mvnw clean test

# Run SonarQube scanner
./mvnw sonar:sonar
```

**Note:** This works for local SonarQube without authentication. For production or if you've enabled authentication, use the token methods above.

---

## Commands Reference

### Docker Commands

```bash
# Start SonarQube only
podman-compose up -d sonarqube-db sonarqube

# Start all services
podman-compose up -d

# Stop SonarQube
podman-compose stop sonarqube sonarqube-db

# Restart SonarQube
podman-compose restart sonarqube

# View logs
podman logs -f sonarqube

# Check status
podman ps | grep sonarqube
```

### Maven Commands

```bash
# Full analysis workflow
cd auth
./mvnw clean test                              # Run tests
./mvnw jacoco:report                           # Generate coverage report
./mvnw sonar:sonar -Dsonar.login=$SONAR_TOKEN  # Upload to SonarQube

# One-liner
./mvnw clean verify sonar:sonar -Dsonar.login=$SONAR_TOKEN

# With custom SonarQube URL
./mvnw sonar:sonar \
  -Dsonar.host.url=http://localhost:9000 \
  -Dsonar.login=$SONAR_TOKEN \
  -Dsonar.projectKey=eadgequry-auth

# Skip tests (use existing coverage)
./mvnw sonar:sonar -Dsonar.login=$SONAR_TOKEN -Dmaven.test.skip=true

# Verbose mode (for debugging)
./mvnw sonar:sonar -Dsonar.login=$SONAR_TOKEN -X
```

### SonarQube API Commands

```bash
# Check system status
curl http://localhost:9000/api/system/status

# Check server version
curl http://localhost:9000/api/server/version

# Get project analysis status
curl -u admin:your_password http://localhost:9000/api/qualitygates/project_status?projectKey=eadgequry-auth

# Get project metrics
curl -u admin:your_password \
  "http://localhost:9000/api/measures/component?component=eadgequry-auth&metricKeys=coverage,bugs,vulnerabilities,code_smells"
```

---

## Configuration Files

### 1. podman-compose.yml

SonarQube configuration in podman-compose:

```yaml
services:
  sonarqube-db:
    image: postgres:15-alpine
    container_name: sonarqube-postgres
    environment:
      POSTGRES_USER: sonar
      POSTGRES_PASSWORD: sonar
      POSTGRES_DB: sonarqube
    volumes:
      - sonarqube-db-data:/var/lib/postgresql/data
    ports:
      - "5432:5432"  # Optional: for direct DB access

  sonarqube:
    image: sonarqube:10.4-community
    container_name: sonarqube
    depends_on:
      sonarqube-db:
        condition: service_healthy
    environment:
      SONAR_JDBC_URL: jdbc:postgresql://sonarqube-db:5432/sonarqube
      SONAR_JDBC_USERNAME: sonar
      SONAR_JDBC_PASSWORD: sonar
    volumes:
      - sonarqube-data:/opt/sonarqube/data
      - sonarqube-extensions:/opt/sonarqube/extensions
      - sonarqube-logs:/opt/sonarqube/logs
    ports:
      - "9000:9000"

volumes:
  sonarqube-db-data:
  sonarqube-data:
  sonarqube-extensions:
  sonarqube-logs:
```

---

### 2. auth/sonar-project.properties

Project-specific SonarQube configuration:

```properties
# Project Information
sonar.projectKey=eadgequry-auth
sonar.projectName=Eadgequry Auth Service
sonar.projectVersion=1.0.0

# Source Code
sonar.sources=src/main/java
sonar.tests=src/test/java
sonar.sourceEncoding=UTF-8
sonar.java.source=21
sonar.java.target=21

# Coverage and Test Reports
sonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml
sonar.junit.reportPaths=target/surefire-reports

# Exclusions (Same as JaCoCo)
sonar.coverage.exclusions=\
  **/AuthApplication.java,\
  **/config/jwt/JwtSecurityConfiguration.java,\
  **/dto/**,\
  **/model/**,\
  **/repository/**,\
  **/util/**

# Maven Integration
sonar.java.binaries=target/classes
sonar.java.test.binaries=target/test-classes

# Server Connection
sonar.host.url=http://localhost:9000
```

---

### 3. auth/pom.xml (Added Properties)

SonarQube Maven properties in pom.xml:

```xml
<properties>
    <java.version>21</java.version>
    <spring-cloud.version>2025.0.0</spring-cloud.version>

    <!-- SonarQube Properties -->
    <sonar.projectKey>eadgequry-auth</sonar.projectKey>
    <sonar.projectName>Eadgequry Auth Service</sonar.projectName>
    <sonar.host.url>http://localhost:9000</sonar.host.url>
    <sonar.java.coveragePlugin>jacoco</sonar.java.coveragePlugin>
    <sonar.coverage.jacoco.xmlReportPaths>${project.build.directory}/site/jacoco/jacoco.xml</sonar.coverage.jacoco.xmlReportPaths>
    <sonar.exclusions>**/target/**,**/bin/**,**/*.xml,**/*.yml,**/*.properties</sonar.exclusions>
    <sonar.coverage.exclusions>**/AuthApplication.java,**/config/jwt/JwtSecurityConfiguration.java,**/dto/**,**/model/**,**/repository/**,**/util/**</sonar.coverage.exclusions>
</properties>

<build>
    <plugins>
        <!-- SonarQube Scanner Plugin -->
        <plugin>
            <groupId>org.sonarsource.scanner.maven</groupId>
            <artifactId>sonar-maven-plugin</artifactId>
            <version>3.10.0.2594</version>
        </plugin>
    </plugins>
</build>
```

---

## What Gets Analyzed

### Code Quality Metrics

| Metric | Description |
|--------|-------------|
| **Bugs** | Code that is demonstrably wrong or highly likely to yield unexpected behavior |
| **Vulnerabilities** | Security-related issues that could be exploited |
| **Code Smells** | Maintainability issues that make code confusing or hard to maintain |
| **Coverage** | Percentage of code covered by unit tests (from JaCoCo) |
| **Duplications** | Duplicated code blocks |
| **Security Hotspots** | Security-sensitive code that requires manual review |
| **Technical Debt** | Estimated time to fix all code smells |

### What's Included in Analysis

✅ **Analyzed (Auth Service):**
- `src/main/java/com/eadgequry/auth/controller/AuthController.java`
- `src/main/java/com/eadgequry/auth/services/AuthService.java`
- `src/main/java/com/eadgequry/auth/services/CustomUserDetailsService.java`
- `src/main/java/com/eadgequry/auth/exception/GlobalExceptionHandler.java`
- `src/main/java/com/eadgequry/auth/controller/JwkSetController.java`
- `src/main/java/com/eadgequry/auth/config/jwt/JwtAuthenticationResource.java`

❌ **Excluded from Coverage:**
- `AuthApplication.java` (main class)
- `JwtSecurityConfiguration.java` (config)
- All DTOs (`dto/**`)
- All models (`model/**`)
- All repositories (`repository/**`)
- All utilities (`util/**`)

---

## Viewing Results

### 1. Access SonarQube Dashboard

```
http://localhost:9000/dashboard?id=eadgequry-auth
```

### 2. Key Sections to Review

**Overview Tab:**
- Overall quality gate status (Pass/Fail)
- Bugs, Vulnerabilities, Code Smells count
- Coverage percentage
- Duplications percentage
- Technical debt

**Issues Tab:**
- Detailed list of all issues found
- Filter by type (Bug, Vulnerability, Code Smell)
- Filter by severity (Blocker, Critical, Major, Minor, Info)

**Measures Tab:**
- All metrics in detail
- Historical trends (after multiple analyses)

**Code Tab:**
- Browse source code with issue highlights
- See which lines have issues

**Activity Tab:**
- Analysis history
- Quality gate history

---

## Expected Results for Auth Service

### Quality Gate Criteria (Default)

| Condition | Threshold | Expected Result |
|-----------|-----------|-----------------|
| Coverage on New Code | ≥ 80% | ✅ Pass (100%) |
| Duplicated Lines (%) | ≤ 3% | ✅ Pass |
| Maintainability Rating | ≤ A | ✅ Pass |
| Reliability Rating | ≤ A | ✅ Pass |
| Security Rating | ≤ A | ✅ Pass |

### Coverage Report

Based on our unit tests, expected coverage for **included code only**:

- **AuthController**: 100% coverage (5 tests)
- **AuthService**: 100% coverage (6 tests)
- **CustomUserDetailsService**: 100% coverage (7 tests)
- **GlobalExceptionHandler**: 100% coverage (4 tests)
- **JwkSetController**: 100% coverage (2 tests)
- **JwtAuthenticationResource**: 100% coverage (4 tests)

**Total: ~100% coverage for code we added**

---

## Troubleshooting

### Issue 1: SonarQube Won't Start

**Problem:** Container exits or doesn't start

**Check logs:**
```bash
podman logs sonarqube
```

**Common causes:**
1. Insufficient memory
   ```bash
   # Increase Podman memory to 4GB minimum
   # For Podman Machine: podman machine set --memory 4096
   ```

2. Port 9000 already in use
   ```bash
   # Check what's using port 9000
   sudo lsof -i :9000

   # Change port in podman-compose.yml
   ports:
     - "9001:9000"  # Use 9001 instead
   ```

3. Database not ready
   ```bash
   # Restart database first
   podman-compose restart sonarqube-db
   sleep 10
   podman-compose restart sonarqube
   ```

---

### Issue 2: Connection Refused

**Problem:** `mvn sonar:sonar` fails with connection refused

**Solution:**
```bash
# Check SonarQube is running
podman ps | grep sonarqube

# Check SonarQube status
curl http://localhost:9000/api/system/status

# If not UP, wait and retry
podman logs -f sonarqube

# Verify host URL in pom.xml
<sonar.host.url>http://localhost:9000</sonar.host.url>
```

---

### Issue 3: Coverage Not Showing

**Problem:** SonarQube shows 0% coverage

**Solution:**
```bash
# 1. Verify JaCoCo XML report exists
ls -la auth/target/site/jacoco/jacoco.xml

# If not found, regenerate:
cd auth
./mvnw clean test jacoco:report

# 2. Check JaCoCo XML path in pom.xml
<sonar.coverage.jacoco.xmlReportPaths>
    ${project.build.directory}/site/jacoco/jacoco.xml
</sonar.coverage.jacoco.xmlReportPaths>

# 3. Run analysis again
./mvnw sonar:sonar -Dsonar.login=$SONAR_TOKEN
```

---

### Issue 4: Authentication Required Error

**Problem:** `Not authorized. Please check the user token`

**Solution:**
```bash
# 1. Generate new token in SonarQube
# Profile → My Account → Security → Generate Token

# 2. Use token in command
./mvnw sonar:sonar -Dsonar.login=squ_your_token_here

# 3. Or save to environment
export SONAR_TOKEN="squ_your_token_here"
./mvnw sonar:sonar -Dsonar.login=$SONAR_TOKEN
```

---

### Issue 5: Analysis Fails with "Project Already Exists"

**Problem:** Cannot create project, key already exists

**Solution:**
```bash
# Option 1: Use existing project
./mvnw sonar:sonar -Dsonar.login=$SONAR_TOKEN

# Option 2: Delete and recreate
# Go to SonarQube → Administration → Projects → Delete project

# Option 3: Change project key
./mvnw sonar:sonar \
  -Dsonar.projectKey=eadgequry-auth-v2 \
  -Dsonar.login=$SONAR_TOKEN
```

---

### Issue 6: Maven Dependencies Not Downloading

**Problem:** Network errors downloading SonarQube plugin

**Solution:**
```bash
# Clear Maven cache
rm -rf ~/.m2/repository/org/sonarsource

# Retry with verbose output
./mvnw clean install -X

# Check internet connection
ping repo.maven.apache.org
```

---

## Adding Other Services

When you create other microservices, follow this pattern:

### 1. Create sonar-project.properties

```bash
# In your service directory (e.g., api-gateway/)
cd api-gateway
```

Create `sonar-project.properties`:

```properties
sonar.projectKey=eadgequry-api-gateway
sonar.projectName=Eadgequry API Gateway Service
sonar.projectVersion=1.0.0

sonar.sources=src/main/java
sonar.tests=src/test/java
sonar.sourceEncoding=UTF-8
sonar.java.source=21
sonar.java.target=21

sonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml
sonar.junit.reportPaths=target/surefire-reports

sonar.java.binaries=target/classes
sonar.java.test.binaries=target/test-classes

sonar.host.url=http://localhost:9000
```

### 2. Add to pom.xml

Add SonarQube properties:

```xml
<properties>
    <!-- SonarQube Properties -->
    <sonar.projectKey>eadgequry-api-gateway</sonar.projectKey>
    <sonar.projectName>Eadgequry API Gateway Service</sonar.projectName>
    <sonar.host.url>http://localhost:9000</sonar.host.url>
    <sonar.java.coveragePlugin>jacoco</sonar.java.coveragePlugin>
    <sonar.coverage.jacoco.xmlReportPaths>${project.build.directory}/site/jacoco/jacoco.xml</sonar.coverage.jacoco.xmlReportPaths>
</properties>

<build>
    <plugins>
        <plugin>
            <groupId>org.sonarsource.scanner.maven</groupId>
            <artifactId>sonar-maven-plugin</artifactId>
            <version>3.10.0.2594</version>
        </plugin>
        <plugin>
            <groupId>org.jacoco</groupId>
            <artifactId>jacoco-maven-plugin</artifactId>
            <version>0.8.11</version>
            <!-- ... executions ... -->
        </plugin>
    </plugins>
</build>
```

### 3. Run Analysis

```bash
cd api-gateway
./mvnw clean verify sonar:sonar -Dsonar.login=$SONAR_TOKEN
```

### 4. Multi-Module Analysis (Optional)

For analyzing all services at once, create a parent `sonar-project.properties` at root:

```properties
# Root sonar-project.properties
sonar.projectKey=eadgequry-microservices
sonar.projectName=Eadgequry Microservices Platform
sonar.projectVersion=1.0.0

# Modules
sonar.modules=auth,api-gateway,naming-server

# Auth module
auth.sonar.projectKey=eadgequry-auth
auth.sonar.sources=auth/src/main/java
auth.sonar.tests=auth/src/test/java

# API Gateway module
api-gateway.sonar.projectKey=eadgequry-api-gateway
api-gateway.sonar.sources=api-gateway/src/main/java
api-gateway.sonar.tests=api-gateway/src/test/java

# Naming Server module
naming-server.sonar.projectKey=eadgequry-naming-server
naming-server.sonar.sources=naming-server/src/main/java
naming-server.sonar.tests=naming-server/src/test/java

sonar.host.url=http://localhost:9000
```

Run from root:
```bash
./mvnw clean verify sonar:sonar -Dsonar.login=$SONAR_TOKEN
```

---

## Best Practices

### 1. Regular Analysis

Run SonarQube analysis:
- ✅ Before every commit (optional)
- ✅ After completing a feature
- ✅ Before creating a pull request
- ✅ As part of CI/CD pipeline

### 2. Quality Gates

Set up quality gates in SonarQube:
- Minimum coverage: 80%
- No new bugs on new code
- No new vulnerabilities on new code
- Maintainability rating: A

### 3. Fix Issues by Priority

1. **Blockers** - Critical bugs that will cause failures
2. **Critical** - Severe bugs or security vulnerabilities
3. **Major** - Important code smells or bugs
4. **Minor** - Small maintainability issues
5. **Info** - Suggestions for improvement

### 4. Track Technical Debt

- Review technical debt regularly
- Allocate time to fix code smells
- Don't let debt grow unbounded

---

## Quick Reference

### Essential Commands

```bash
# Start SonarQube
podman-compose up -d sonarqube

# Run full analysis (auth service)
cd auth && ./mvnw clean verify sonar:sonar -Dsonar.login=$SONAR_TOKEN

# View results
open http://localhost:9000/dashboard?id=eadgequry-auth

# Stop SonarQube
podman-compose stop sonarqube sonarqube-db

# Clean everything and restart
podman-compose down -v
podman-compose up -d sonarqube
```

### URLs

- **SonarQube Dashboard**: http://localhost:9000
- **Auth Service Project**: http://localhost:9000/dashboard?id=eadgequry-auth
- **API Documentation**: http://localhost:9000/web_api
- **Quality Gates**: http://localhost:9000/quality_gates

### Default Credentials

- **Username**: `admin`
- **Password**: `admin` (change on first login)

---

## Summary

✅ SonarQube running in Podman on port 9000
✅ PostgreSQL database for SonarQube data
✅ Auth service configured with sonar-project.properties
✅ Maven plugin for SonarQube Scanner
✅ JaCoCo integration for test coverage
✅ Exclusions configured (DTOs, models, config, etc.)
✅ Ready to analyze other services

**Next Steps:**
1. Start SonarQube: `podman-compose up -d sonarqube`
2. Login and create token: http://localhost:9000
3. Run analysis: `cd auth && ./mvnw clean verify sonar:sonar -Dsonar.login=$SONAR_TOKEN`
4. View results: http://localhost:9000/dashboard?id=eadgequry-auth

Happy code quality scanning! 🚀
