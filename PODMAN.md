# Podman/Docker Setup Guide

This guide explains how to run all Eadgequry-AI services using Podman or Docker.

## 📋 Prerequisites

### Option 1: Podman (Recommended)
```bash
# Install Podman
# On Fedora/RHEL
sudo dnf install podman podman-compose

# On Ubuntu/Debian
sudo apt install podman python3-pip
pip install podman-compose

# On macOS
brew install podman
pip install podman-compose
```

### Option 2: Docker
```bash
# Install Docker and Docker Compose
# Follow instructions at: https://docs.docker.com/get-docker/
```

## 🏗️ Architecture

The project consists of:

### Java Microservices (7 services)
- **naming-server** (Port 8761) - Eureka Service Discovery
- **api-gateway** (Port 8765) - API Gateway
- **auth-service** (Port 8081) - Authentication Service
- **user-profile-service** (Port 8082) - User Profile Management
- **notification-service** (Port 8084) - Notification Service
- **data-source-service** (Port 8085) - Data Source Service
- **chatbot-service** (Port 8089) - Chatbot Service

### Frontend
- **frontend** (Port 3000) - Next.js TypeScript Frontend

### Infrastructure Services
- **MySQL** (Ports 3306, 3307, 3308, 3311) - Databases for different services
- **Kafka + Zookeeper** (Ports 9092, 2181) - Message Broker
- **SonarQube** (Port 9000) - Code Quality Analysis
- **Jenkins** (Port 8982) - CI/CD Server
- **phpMyAdmin** (Ports 8080, 8083, 8088, 8090) - Database Management

## 🚀 Quick Start

### 1. Build Everything
```bash
# This builds all Java services, frontend, and container images
./build-all.sh
```

### 2. Start All Services
```bash
# Start all services in detached mode
./start-all.sh
```

### 3. View Logs
```bash
# View all logs
./logs.sh

# View logs for a specific service
./logs.sh naming-server
./logs.sh frontend
./logs.sh auth-service
```

### 4. Stop All Services
```bash
# Stop all services
./stop-all.sh

# Stop and remove volumes
podman-compose down -v
# OR
docker-compose down -v
```

## 📝 Manual Commands

### Build Commands

#### Build Java Services Only
```bash
cd naming-server && ./mvnw clean package -DskipTests && cd ..
cd api-gatway && ./mvnw clean package -DskipTests && cd ..
cd auth && ./mvnw clean package -DskipTests && cd ..
cd user-profile && ./mvnw clean package -DskipTests && cd ..
cd notification && ./mvnw clean package -DskipTests && cd ..
cd data-source && ./mvnw clean package -DskipTests && cd ..
cd chat-bot-service && ./mvnw clean package -DskipTests && cd ..
```

#### Build Frontend Only
```bash
cd front-end-next-ts
npm install
npm run build
cd ..
```

#### Build Container Images
```bash
# Using Podman
podman build -t naming-server:latest -f naming-server/Containerfile naming-server/
podman build -t api-gateway:latest -f api-gatway/Containerfile api-gatway/
podman build -t auth-service:latest -f auth/Containerfile auth/
podman build -t user-profile-service:latest -f user-profile/Containerfile user-profile/
podman build -t notification-service:latest -f notification/Containerfile notification/
podman build -t data-source-service:latest -f data-source/Containerfile data-source/
podman build -t chatbot-service:latest -f chat-bot-service/Containerfile chat-bot-service/
podman build -t frontend:latest -f front-end-next-ts/Containerfile front-end-next-ts/

# Using Docker (replace 'podman' with 'docker' in the above commands)
```

### Container Management

#### Start Services
```bash
# Start all services
podman-compose -f podman-compose.yml up -d

# Start specific services
podman-compose -f podman-compose.yml up -d naming-server
podman-compose -f podman-compose.yml up -d frontend
```

#### Stop Services
```bash
# Stop all services
podman-compose -f podman-compose.yml down

# Stop specific services
podman-compose -f podman-compose.yml stop naming-server
```

#### View Status
```bash
# View running containers
podman-compose -f podman-compose.yml ps

# View all containers (including stopped)
podman ps -a
```

#### View Logs
```bash
# Follow logs for all services
podman-compose -f podman-compose.yml logs -f

# Follow logs for specific service
podman-compose -f podman-compose.yml logs -f naming-server
podman-compose -f podman-compose.yml logs -f frontend

# View last 100 lines
podman-compose -f podman-compose.yml logs --tail=100 auth-service
```

#### Restart Services
```bash
# Restart specific service
podman-compose -f podman-compose.yml restart naming-server

# Restart all services
podman-compose -f podman-compose.yml restart
```

## 🔍 Service Health Checks

After starting services, verify they're healthy:

```bash
# Check Eureka Dashboard
curl http://localhost:8761

# Check API Gateway
curl http://localhost:8765/actuator/health

# Check Auth Service
curl http://localhost:8081/actuator/health

# Check Frontend
curl http://localhost:3000
```

## 🌐 Access URLs

### Application Services
- Frontend: http://localhost:3000
- API Gateway: http://localhost:8765
- Eureka Dashboard: http://localhost:8761
- Auth Service: http://localhost:8081
- User Profile Service: http://localhost:8082
- Notification Service: http://localhost:8084
- Data Source Service: http://localhost:8085
- Chatbot Service: http://localhost:8089

### Management Tools
- SonarQube: http://localhost:9000
- Jenkins: http://localhost:8982/jenkins
- phpMyAdmin (Auth): http://localhost:8080
- phpMyAdmin (Profile): http://localhost:8083
- phpMyAdmin (DataSource): http://localhost:8088
- phpMyAdmin (Chatbot): http://localhost:8090

## 🐛 Troubleshooting

### Service Won't Start
```bash
# Check logs
./logs.sh [service-name]

# Rebuild the service
podman-compose -f podman-compose.yml build [service-name]
podman-compose -f podman-compose.yml up -d [service-name]
```

### Database Connection Issues
```bash
# Ensure MySQL is healthy
podman-compose -f podman-compose.yml ps mysql

# Check MySQL logs
./logs.sh mysql

# Restart MySQL
podman-compose -f podman-compose.yml restart mysql
```

### Port Already in Use
```bash
# Check what's using the port
sudo lsof -i :8761

# Stop the conflicting service or change the port in podman-compose.yml
```

### Podman-specific Issues

#### Rootless vs Rootful Podman
The `podman-compose.yml` includes notes for both rootless and rootful Podman setups. Check the Jenkins service section for socket path configuration.

#### SELinux Issues
If you encounter permission errors with volumes:
```bash
# Add :Z flag to volumes (already included in podman-compose.yml)
# This relabels the volume for container access
```

## 🔄 Development Workflow

### Make Changes to a Service

1. **Update Code**
2. **Rebuild JAR** (for Java services):
   ```bash
   cd [service-name]
   ./mvnw clean package -DskipTests
   cd ..
   ```
3. **Rebuild Container**:
   ```bash
   podman build -t [service-name]:latest -f [service-name]/Containerfile [service-name]/
   ```
4. **Restart Service**:
   ```bash
   podman-compose -f podman-compose.yml restart [service-name]
   ```

### Or Use build-all.sh
```bash
# Rebuild everything
./build-all.sh

# Restart services
podman-compose -f podman-compose.yml restart
```

## 📊 Resource Management

### View Resource Usage
```bash
# View container resource usage
podman stats

# View image sizes
podman images
```

### Clean Up

```bash
# Remove all stopped containers
podman container prune

# Remove unused images
podman image prune

# Remove all unused data (containers, images, volumes, networks)
podman system prune -a

# Remove volumes
podman-compose -f podman-compose.yml down -v
```

## 🔒 Security Notes

- Change default passwords in `podman-compose.yml` for production
- The setup uses default credentials for development
- Jenkins runs as root in the container (for Podman socket access)
- Consider using secrets management for production deployments

## 📚 Additional Resources

- [Podman Documentation](https://docs.podman.io/)
- [Podman Compose](https://github.com/containers/podman-compose)
- [Docker Documentation](https://docs.docker.com/)
- [Spring Boot with Docker](https://spring.io/guides/gs/spring-boot-docker/)
- [Next.js Docker Deployment](https://nextjs.org/docs/deployment#docker-image)

## 💡 Tips

1. **Use podman-compose** for development (rootless, daemonless, more secure)
2. **Use Docker** if you need full compatibility with Docker tooling
3. **Monitor logs** regularly with `./logs.sh` to catch issues early
4. **Health checks** are configured for critical services
5. **Rebuild only changed services** to save time during development
