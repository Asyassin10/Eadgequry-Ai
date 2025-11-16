# Example: Adding Monitoring to Auth Service

This shows EXACTLY what to add to enable monitoring for the Auth service.
Repeat this for all other services.

---

## 📝 STEP 1: Update pom.xml

**File:** `auth/pom.xml`

Find the `<dependencies>` section and add these TWO dependencies:

```xml
<dependencies>
    <!-- Existing dependencies here... -->

    <!-- ADD THESE TWO DEPENDENCIES -->

    <!-- Spring Boot Actuator for metrics -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-actuator</artifactId>
    </dependency>

    <!-- Micrometer Prometheus registry -->
    <dependency>
        <groupId>io.micrometer</groupId>
        <artifactId>micrometer-registry-prometheus</artifactId>
        <scope>runtime</scope>
    </dependency>

    <!-- END OF NEW DEPENDENCIES -->
</dependencies>
```

---

## 📝 STEP 2: Update application.properties

**File:** `auth/src/main/resources/application.properties`

Add these lines at the end:

```properties
# ============================================
# MONITORING CONFIGURATION
# ============================================

# Expose actuator endpoints
management.endpoints.web.exposure.include=health,info,metrics,prometheus
management.endpoints.web.base-path=/actuator

# Health endpoint details
management.endpoint.health.show-details=always

# Enable Prometheus metrics
management.endpoint.prometheus.enabled=true
management.metrics.export.prometheus.enabled=true

# Application info
management.info.env.enabled=true
info.app.name=auth-service
info.app.version=1.0.0
info.app.description=EadgeQuery Authentication Service

# Optional: Add application name tag to all metrics
management.metrics.tags.application=auth
management.metrics.tags.service=authentication
```

### OR if using application.yml:

**File:** `auth/src/main/resources/application.yml`

```yaml
# Add this section
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
      base-path: /actuator
  endpoint:
    health:
      show-details: always
    prometheus:
      enabled: true
  metrics:
    export:
      prometheus:
        enabled: true
    tags:
      application: auth
      service: authentication

info:
  app:
    name: auth-service
    version: 1.0.0
    description: EadgeQuery Authentication Service
```

---

## 📝 STEP 3: Rebuild the Service

```bash
cd auth

# Clean and rebuild
mvn clean package -DskipTests

# Or with tests
mvn clean package

cd ..
```

---

## 📝 STEP 4: Restart the Service

```bash
# Stop the service
docker-compose stop auth

# Rebuild Docker image
docker-compose build auth

# Start the service
docker-compose up -d auth

# Check logs
docker-compose logs -f auth
```

---

## ✅ STEP 5: Verify It's Working

### Test the Actuator Health Endpoint:

```bash
curl http://localhost:8081/actuator/health
```

**Expected output:**
```json
{
  "status": "UP",
  "components": {
    "diskSpace": {
      "status": "UP",
      "details": {
        "total": 123456789,
        "free": 98765432,
        "threshold": 10485760
      }
    },
    "ping": {
      "status": "UP"
    }
  }
}
```

### Test the Prometheus Metrics Endpoint:

```bash
curl http://localhost:8081/actuator/prometheus
```

**Expected output:**
```
# HELP jvm_memory_used_bytes The amount of used memory
# TYPE jvm_memory_used_bytes gauge
jvm_memory_used_bytes{application="auth",area="heap",id="PS Eden Space",} 1.23456789E8
jvm_memory_used_bytes{application="auth",area="heap",id="PS Survivor Space",} 1234567.0
# HELP http_server_requests_seconds
# TYPE http_server_requests_seconds summary
http_server_requests_seconds_count{application="auth",exception="None",method="GET",outcome="SUCCESS",status="200",uri="/api/auth/login",} 42.0
http_server_requests_seconds_sum{application="auth",exception="None",method="GET",outcome="SUCCESS",status="200",uri="/api/auth/login",} 0.123456789
...
```

### Check in Prometheus:

1. Open: `http://localhost:9090`
2. Click **Status** → **Targets**
3. Find **auth-service** - should show **State: UP** (green)

---

## 🔄 Repeat for All Services

Now repeat STEPS 1-5 for these services:

1. **api-gatway**
   - Port: 8765
   - Tag: `application: api-gateway`

2. **naming-server**
   - Port: 8761
   - Tag: `application: naming-server`

3. **chat-bot-service**
   - Port: 8086
   - Tag: `application: chat-bot`

4. **data-source**
   - Port: 8087
   - Tag: `application: data-source`

5. **user-profile**
   - Port: 8088
   - Tag: `application: user-profile`

6. **notification**
   - Port: 8089
   - Tag: `application: notification`

---

## 📋 Quick Verification Checklist

For each service, verify:

- [ ] Added 2 dependencies to pom.xml
- [ ] Added monitoring config to application.properties
- [ ] Rebuilt with `mvn clean package`
- [ ] Restarted docker container
- [ ] Can access `/actuator/health`
- [ ] Can access `/actuator/prometheus`
- [ ] Shows as UP in Prometheus targets
- [ ] Appears in Grafana dashboard

---

## 🎯 Common Issues

### Issue: 404 on /actuator endpoints

**Cause:** Dependencies not added or service not rebuilt

**Fix:**
```bash
# Make sure you added dependencies to pom.xml
# Then rebuild:
cd <service-name>
mvn clean package -DskipTests
docker-compose build <service-name>
docker-compose up -d <service-name>
```

### Issue: Metrics not showing in Grafana

**Cause:** No traffic to the service yet

**Fix:**
```bash
# Generate some traffic
curl http://localhost:8081/actuator/health
curl http://localhost:8081/api/auth/login
# (Make actual API calls to your endpoints)

# Wait 15-30 seconds for Prometheus to scrape
# Then check Grafana
```

### Issue: "Connection refused" in Prometheus

**Cause:** Service name doesn't match docker-compose

**Fix:**
- Check `monitoring/prometheus/prometheus.yml`
- Service names must match container names in docker-compose.yml
- For auth service, target should be: `auth:8081` not `auth-service:8081`

---

## 💡 Pro Tips

1. **Use consistent naming:**
   - Service: `auth`
   - Application tag: `auth`
   - Container: `auth`

2. **Add meaningful tags:**
   ```properties
   management.metrics.tags.environment=development
   management.metrics.tags.team=backend
   management.metrics.tags.version=1.0.0
   ```

3. **Test locally first:**
   ```bash
   # Run service locally (not in Docker)
   cd auth
   mvn spring-boot:run

   # Test
   curl http://localhost:8081/actuator/prometheus
   ```

4. **Monitor the metrics:**
   ```bash
   # Watch metrics in real-time
   watch -n 1 'curl -s http://localhost:8081/actuator/prometheus | grep http_server_requests_seconds_count'
   ```

---

**This example is for the Auth service. Apply the same pattern to all other services!**
