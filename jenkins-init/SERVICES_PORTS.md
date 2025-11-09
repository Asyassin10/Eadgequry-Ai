# EadgeQuery Services & Ports Reference

## 📊 All Services with Ports

### Core Services

| Service | Port | URL | Description |
|---------|------|-----|-------------|
| **naming-server** | 8761 | http://localhost:8761 | Eureka Discovery Server |
| **auth** | 8081 | http://localhost:8081 | Authentication Service |
| **api-gatway** | 8765 | http://localhost:8765 | API Gateway |
| **chat-bot-service** | 8086 | http://localhost:8086 | Chatbot Service |
| **data-source** | 8087 | http://localhost:8087 | Data Source Service |
| **user-profile** | 8088 | http://localhost:8088 | User Profile Service |
| **notification** | 8089 | http://localhost:8089 | Notification Service |

### Infrastructure Services

| Service | Port | URL | Description |
|---------|------|-----|-------------|
| **Jenkins** | 8082 | http://localhost:8082/jenkins | CI/CD Server |
| **SonarQube** | 9000 | http://localhost:9000 | Code Quality Platform |
| **Zipkin** | 9411 | http://localhost:9411 | Distributed Tracing |
| **Kafka** | 9092 | localhost:9092 | Message Broker |
| **Zookeeper** | 2181 | localhost:2181 | Kafka Coordinator |

### Database Services

| Service | Port | Type | Database Name |
|---------|------|------|---------------|
| **mysql** | 3306 | MySQL 8.0 | eadgequry-auth |
| **mysql-user-profile** | 3307 | MySQL 8.0 | eadgequry-user-profile |
| **mysql-datasource** | 3308 | MySQL 8.0 | datasource_db |
| **mysql-chatbot** | 3311 | MySQL 8.0 | chatbot_db |
| **sonarqube-db** | 5432 | PostgreSQL | sonarqube |

### Database Admin Tools

| Service | Port | URL | Manages |
|---------|------|-----|---------|
| **phpmyadmin** | 8080 | http://localhost:8080 | mysql (auth) |
| **phpmyadmin-user-profile** | 8083 | http://localhost:8083 | mysql-user-profile |
| **phpmyadmin-datasource** | 8084 | http://localhost:8084 | mysql-datasource |
| **phpmyadmin-chatbot** | 8085 | http://localhost:8085 | mysql-chatbot |

## 🔧 Port Ranges Summary

- **8000-8099**: Application services & admin tools
- **9000-9999**: Infrastructure services (SonarQube, Zipkin, Kafka)
- **3306-3399**: MySQL databases
- **5432**: PostgreSQL (SonarQube)
- **2181**: Zookeeper
- **50000**: Jenkins agent port

## 🎯 Quick Access URLs

### Development
- Eureka Dashboard: http://localhost:8761
- API Gateway: http://localhost:8765
- Auth Service: http://localhost:8081

### CI/CD & Quality
- **Jenkins**: http://localhost:8082/jenkins
- **SonarQube**: http://localhost:9000 (admin/admin)
- **Zipkin**: http://localhost:9411

### Database Management
- Auth DB: http://localhost:8080 (authuser/authpassword)
- User Profile DB: http://localhost:8083 (profileuser/profilepassword)
- DataSource DB: http://localhost:8084 (datasourceuser/datasourcepassword)
- Chatbot DB: http://localhost:8085 (chatbot_user/chatbot_password)

## 📝 Port Availability Check

Run the port scanner to check current port usage:

```bash
./jenkins-init/scan-ports.sh
```

## 🚨 Port Conflicts Resolution

If you encounter port conflicts:

1. **Check what's using the port:**
   ```bash
   lsof -i :PORT_NUMBER
   # or
   netstat -tuln | grep PORT_NUMBER
   ```

2. **Stop the conflicting service:**
   ```bash
   # If it's a Docker container
   docker stop CONTAINER_NAME

   # If it's a system service
   sudo systemctl stop SERVICE_NAME
   ```

3. **Change port in docker-compose.yml:**
   ```yaml
   ports:
     - "NEW_PORT:INTERNAL_PORT"
   ```

4. **Update your service configuration** if needed

## 🔐 Default Credentials

### Jenkins
- Initial: See output from `docker exec jenkins-cicd cat /var/jenkins_home/secrets/initialAdminPassword`
- After setup: admin/admin (change after first login)

### SonarQube
- Username: admin
- Password: admin
- Change after first login: http://localhost:9000

### MySQL Databases
- Root password: rootpassword
- Auth: authuser/authpassword
- User Profile: profileuser/profilepassword
- DataSource: datasourceuser/datasourcepassword
- Chatbot: chatbot_user/chatbot_password

### phpMyAdmin
- Use the respective MySQL credentials above

## 📊 Service Dependencies

```
┌─────────────────┐
│  Naming Server  │ (8761)
│    (Eureka)     │
└────────┬────────┘
         │
    ┌────┴────┬────────────┐
    │         │            │
┌───┴──┐  ┌──┴───┐    ┌──┴────┐
│ Auth │  │ API  │    │ Other │
│      │  │  GW  │    │  SVCs │
└──┬───┘  └──┬───┘    └───────┘
   │         │
┌──┴─────────┴──┐
│    Jenkins    │ (8082)
│  CI/CD Server │
└───────────────┘
```

## 🔄 Starting Services in Order

### Recommended Startup Order:

1. **Infrastructure** (databases, Kafka)
   ```bash
   docker-compose up -d mysql mysql-user-profile mysql-datasource mysql-chatbot
   docker-compose up -d zookeeper kafka
   docker-compose up -d sonarqube-db sonarqube
   ```

2. **Discovery & Gateway**
   ```bash
   docker-compose up -d naming-server
   docker-compose up -d auth
   docker-compose up -d api-gatway
   ```

3. **Business Services**
   ```bash
   docker-compose up -d chat-bot-service data-source user-profile notification
   ```

4. **Admin Tools**
   ```bash
   docker-compose up -d phpmyadmin phpmyadmin-user-profile phpmyadmin-datasource phpmyadmin-chatbot
   docker-compose up -d zipkin
   docker-compose up -d jenkins
   ```

### Or Start Everything:
```bash
docker-compose up -d
```

## 🛑 Stopping Services

```bash
# Stop all
docker-compose down

# Stop specific service
docker-compose stop SERVICE_NAME

# Stop and remove volumes (CAUTION: Data loss!)
docker-compose down -v
```

## 📈 Monitoring

Check status of all services:
```bash
docker-compose ps
```

View logs:
```bash
# All services
docker-compose logs

# Specific service
docker-compose logs -f SERVICE_NAME

# Last 100 lines
docker-compose logs --tail=100
```

## 🆘 Troubleshooting

### Service won't start
```bash
# Check logs
docker-compose logs SERVICE_NAME

# Check if port is in use
lsof -i :PORT_NUMBER

# Restart service
docker-compose restart SERVICE_NAME
```

### Database connection issues
```bash
# Check database is running
docker-compose ps | grep mysql

# Check database logs
docker-compose logs mysql

# Test connection
docker exec -it mysql-auth mysql -u authuser -p
```

### Network issues
```bash
# List networks
docker network ls

# Inspect network
docker network inspect eadgequry-net

# Recreate network
docker-compose down
docker-compose up -d
```

---

**Last Updated**: 2024
**Network**: eadgequry-net (bridge)
