# 📊 Grafana + Prometheus Monitoring Setup Guide

Complete guide to set up monitoring for all EadgeQuery microservices.

---

## 🎯 What You Get

- **Grafana Dashboard** - Beautiful visualizations at `http://localhost:3000`
- **Prometheus** - Metrics collection at `http://localhost:9090`
- **Real-time metrics** for all 7 microservices:
  - HTTP request rates
  - Response times
  - CPU usage
  - Memory usage
  - JVM metrics
  - Database connections
  - Custom business metrics

---

## 📋 Architecture

```
Spring Boot Services → Actuator/Prometheus Endpoint → Prometheus → Grafana
     (metrics)              (/actuator/prometheus)      (scrape)    (visualize)
```

---

## ⚙️ STEP 1: Add Spring Boot Actuator to Your Services

You need to add Actuator and Prometheus dependencies to **EACH** microservice.

### For Maven Projects (All your services):

Open the `pom.xml` file in each service directory and add these dependencies:

**File: `auth/pom.xml`, `api-gatway/pom.xml`, etc.**

Add this inside `<dependencies>` section:

```xml
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
```

### Services to Update:

- ✅ `auth/pom.xml`
- ✅ `api-gatway/pom.xml`
- ✅ `naming-server/pom.xml`
- ✅ `chat-bot-service/pom.xml`
- ✅ `data-source/pom.xml`
- ✅ `user-profile/pom.xml`
- ✅ `notification/pom.xml`

---

## ⚙️ STEP 2: Configure Actuator Endpoints

Add this configuration to `application.properties` or `application.yml` in **EACH** service.

### Option A: Using application.properties

Add to your `src/main/resources/application.properties`:

```properties
# Actuator Configuration
management.endpoints.web.exposure.include=health,info,metrics,prometheus
management.endpoints.web.base-path=/actuator
management.endpoint.health.show-details=always
management.endpoint.prometheus.enabled=true
management.metrics.export.prometheus.enabled=true

# Optional: Custom application info
management.info.env.enabled=true
info.app.name=@project.artifactId@
info.app.version=@project.version@
info.app.description=EadgeQuery Microservice
```

### Option B: Using application.yml

Add to your `src/main/resources/application.yml`:

```yaml
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

info:
  app:
    name: @project.artifactId@
    version: @project.version@
    description: EadgeQuery Microservice
```

### Add to All Services:

- ✅ `auth/src/main/resources/application.properties`
- ✅ `api-gatway/src/main/resources/application.properties`
- ✅ `naming-server/src/main/resources/application.properties`
- ✅ `chat-bot-service/src/main/resources/application.properties`
- ✅ `data-source/src/main/resources/application.properties`
- ✅ `user-profile/src/main/resources/application.properties`
- ✅ `notification/src/main/resources/application.properties`

---

## ⚙️ STEP 3: Rebuild Your Services

After adding dependencies and configuration:

```bash
# Rebuild each service
cd auth
mvn clean package -DskipTests
cd ..

cd api-gatway
mvn clean package -DskipTests
cd ..

# Repeat for all services...
```

Or use the build script:

```bash
./build-all.sh
```

---

## 🚀 STEP 4: Start Monitoring Stack

### Start All Services with Monitoring:

```bash
# Start everything
docker-compose up -d

# Or start monitoring stack only
docker-compose up -d prometheus grafana

# Check if running
docker-compose ps prometheus grafana
```

### Verify Prometheus is Running:

```bash
# Check logs
docker-compose logs -f prometheus

# You should see:
# "Server is ready to receive web requests"
```

### Verify Grafana is Running:

```bash
# Check logs
docker-compose logs -f grafana

# You should see:
# "HTTP Server Listen"
```

---

## 🔍 STEP 5: Verify Metrics Are Being Collected

### Test Actuator Endpoints:

Once your services are running, test if they're exposing metrics:

```bash
# Test Auth service
curl http://localhost:8081/actuator/prometheus

# Test API Gateway
curl http://localhost:8765/actuator/prometheus

# Test Naming Server
curl http://localhost:8761/actuator/prometheus

# You should see Prometheus metrics format output
```

### Check Prometheus Targets:

1. Open Prometheus: `http://localhost:9090`
2. Click **Status** → **Targets**
3. You should see all 7 services listed:
   - auth-service
   - api-gateway
   - naming-server
   - chat-bot-service
   - data-source
   - user-profile
   - notification

4. All should show **State: UP** (green)

---

## 📊 STEP 6: Access Grafana Dashboard

### Login to Grafana:

1. **Open:** `http://localhost:3000`

2. **Login:**
   - Username: `admin`
   - Password: `admin`

3. **Skip password change** (or change it if you want)

### Verify Datasource:

1. Click **⚙️ Configuration** (gear icon) → **Data Sources**
2. You should see **Prometheus** listed
3. Click on it
4. Click **"Test"** button at bottom
5. Should show: **"Data source is working"** ✅

### View the Dashboard:

1. Click **☷ Dashboards** (four squares icon) → **Browse**
2. Click folder: **"EadgeQuery Microservices"**
3. Click dashboard: **"EadgeQuery Spring Boot Microservices"**

### What You'll See:

- **HTTP Request Rate** - Requests per second for each service
- **HTTP Request Duration** - Average response time
- **JVM Memory Usage** - Heap and non-heap memory
- **CPU Usage** - System and process CPU

### Filter by Service:

At the top, use the **"Application"** dropdown to:
- Select specific services
- Select all services
- Compare multiple services

---

## 🎨 STEP 7: Explore Metrics

### Query Prometheus Directly:

1. Open: `http://localhost:9090`
2. Click **Graph**
3. Try these queries:

```promql
# Total HTTP requests per service
rate(http_server_requests_seconds_count[5m])

# Memory usage by service
jvm_memory_used_bytes{application="auth"}

# CPU usage
system_cpu_usage{application="api-gateway"}

# Request duration p95
histogram_quantile(0.95, sum(rate(http_server_requests_seconds_bucket[5m])) by (le, application))
```

### Available Metrics:

**HTTP Metrics:**
- `http_server_requests_seconds_count` - Request count
- `http_server_requests_seconds_sum` - Total duration
- `http_server_requests_seconds_bucket` - Histogram

**JVM Metrics:**
- `jvm_memory_used_bytes` - Memory usage
- `jvm_memory_max_bytes` - Max memory
- `jvm_gc_pause_seconds` - GC pause time
- `jvm_threads_live` - Thread count

**System Metrics:**
- `system_cpu_usage` - System CPU
- `process_cpu_usage` - Process CPU
- `system_load_average_1m` - Load average

**Database Metrics (if using JPA):**
- `hikaricp_connections_active` - Active connections
- `hikaricp_connections_idle` - Idle connections

---

## 🔧 STEP 8: Create Custom Dashboards

### Import Pre-built Dashboards:

1. In Grafana, click **+** → **Import**
2. Enter dashboard ID: **4701** (Spring Boot 2.1 Statistics)
3. Click **Load**
4. Select datasource: **Prometheus**
5. Click **Import**

### Popular Spring Boot Dashboards:

- **4701** - Spring Boot 2.1 Statistics
- **6756** - Spring Boot Statistics
- **11378** - JVM (Micrometer)
- **12900** - Spring Boot Observability

---

## 📱 Port Summary

| Service | Port | URL |
|---------|------|-----|
| Grafana | 3000 | http://localhost:3000 |
| Prometheus | 9090 | http://localhost:9090 |
| Auth | 8081 | http://localhost:8081/actuator/prometheus |
| API Gateway | 8765 | http://localhost:8765/actuator/prometheus |
| Naming Server | 8761 | http://localhost:8761/actuator/prometheus |
| Chat Bot | 8086 | http://localhost:8086/actuator/prometheus |
| Data Source | 8087 | http://localhost:8087/actuator/prometheus |
| User Profile | 8088 | http://localhost:8088/actuator/prometheus |
| Notification | 8089 | http://localhost:8089/actuator/prometheus |

---

## 🐛 Troubleshooting

### Issue: "Target Down" in Prometheus

**Cause:** Service not running or not exposing metrics

**Fix:**
```bash
# Check if service is running
docker-compose ps

# Check service logs
docker-compose logs auth

# Test actuator endpoint
curl http://localhost:8081/actuator/health

# Restart service
docker-compose restart auth
```

### Issue: "No Data" in Grafana Dashboard

**Cause:** No metrics data available yet

**Fix:**
- Wait 1-2 minutes for first scrape
- Generate some traffic to your services
- Verify Prometheus is scraping: `http://localhost:9090/targets`

### Issue: "404 Not Found" on /actuator/prometheus

**Cause:** Actuator not configured properly

**Fix:**
1. Verify dependencies in pom.xml
2. Verify application.properties configuration
3. Rebuild the service: `mvn clean package`
4. Restart: `docker-compose restart <service-name>`

### Issue: Grafana Can't Connect to Prometheus

**Cause:** Wrong datasource URL

**Fix:**
1. In Grafana, go to Configuration → Data Sources → Prometheus
2. URL should be: `http://prometheus:9090` (NOT localhost!)
3. Click "Save & Test"

---

## 📈 Advanced: Custom Metrics

### Add Custom Metrics to Your Code:

**Add to any Spring Boot service:**

```java
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Service;

@Service
public class MyService {

    private final Counter loginCounter;
    private final Counter errorCounter;

    public MyService(MeterRegistry registry) {
        this.loginCounter = Counter.builder("user.login.count")
            .description("Number of user logins")
            .tag("service", "auth")
            .register(registry);

        this.errorCounter = Counter.builder("user.login.errors")
            .description("Number of login errors")
            .tag("service", "auth")
            .register(registry);
    }

    public void login() {
        // Your login logic
        loginCounter.increment();
    }

    public void loginFailed() {
        errorCounter.increment();
    }
}
```

### Query Custom Metrics:

```promql
# In Prometheus/Grafana
rate(user_login_count_total[5m])
user_login_errors_total
```

---

## 🔐 Security Best Practices

### 1. Secure Actuator Endpoints (Production):

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: when-authorized

# Add Spring Security
spring:
  security:
    user:
      name: admin
      password: ${ACTUATOR_PASSWORD}
```

### 2. Secure Grafana (Production):

```yaml
# In docker-compose.yml
environment:
  - GF_SECURITY_ADMIN_PASSWORD=${GRAFANA_ADMIN_PASSWORD}
  - GF_USERS_ALLOW_SIGN_UP=false
```

### 3. Secure Prometheus (Production):

Add basic auth or use reverse proxy with authentication.

---

## 📊 Useful Dashboards & Queries

### Top 10 Slowest Endpoints:

```promql
topk(10, histogram_quantile(0.95,
  sum(rate(http_server_requests_seconds_bucket[5m]))
  by (le, uri, application)
))
```

### Error Rate by Service:

```promql
sum(rate(http_server_requests_seconds_count{status=~"5.."}[5m]))
by (application)
```

### Database Connection Pool:

```promql
hikaricp_connections_active{application="auth"}
hikaricp_connections_idle{application="auth"}
```

### Memory Usage Percentage:

```promql
(jvm_memory_used_bytes / jvm_memory_max_bytes) * 100
```

---

## 🎯 Quick Start Checklist

- [ ] Add Actuator dependencies to all 7 services
- [ ] Configure application.properties in all services
- [ ] Rebuild all services
- [ ] Start Prometheus and Grafana: `docker-compose up -d prometheus grafana`
- [ ] Verify targets in Prometheus: `http://localhost:9090/targets`
- [ ] Login to Grafana: `http://localhost:3000` (admin/admin)
- [ ] Verify Prometheus datasource is working
- [ ] View EadgeQuery Spring Boot dashboard
- [ ] Generate traffic and see metrics update
- [ ] Import additional dashboards (optional)

---

## 🚀 Next Steps

1. **Set up Alerts** - Get notified when services are down
2. **Add Log Aggregation** - Integrate with ELK/Loki
3. **Distributed Tracing** - Already have Zipkin!
4. **Custom Dashboards** - Create business-specific metrics
5. **Production Hardening** - Secure all endpoints

---

## 📞 Support

**Prometheus not scraping?**
- Check `monitoring/prometheus/prometheus.yml`
- Verify service names match docker-compose
- Check network connectivity

**Grafana dashboard empty?**
- Wait 1-2 minutes for initial data
- Generate traffic to services
- Check Prometheus has data: `http://localhost:9090`

**Need help?**
- Prometheus docs: https://prometheus.io/docs/
- Grafana docs: https://grafana.com/docs/
- Spring Boot Actuator: https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html

---

**Version:** 1.0
**Last Updated:** 2025
**Maintained by:** EadgeQuery DevOps Team
