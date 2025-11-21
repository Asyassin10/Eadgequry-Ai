# EadgeQuery-AI

Microservices-based AI platform built with Spring Boot and Spring Cloud.

## 🏗️ Architecture

Scalable microservices architecture with service discovery, API gateway, and distributed tracing.

## 📋 Services

| Service                        | Port | Description              |
| ------------------------------ | ---- | ------------------------ |
| **Naming Server**              | 8761 | Eureka Service Discovery |
| **API Gateway**                | 8765 | Spring Cloud Gateway     |
| **Zipkin**                     | 9411 | Distributed Tracing      |
| _More services coming soon..._ | -    | -                        |

## 🚀 Quick Start

### Prerequisites

- **Java 21**
- **Maven 3.8+**
- **Podman & Podman Compose** (optional for production)

### Development Mode (Recommended)

```bash
# Clone repository
git clone https://github.com/Asyassin10/Eadgequry-Ai.git
cd Eadgequry-Ai

# Run each service in separate terminal
cd naming-server && ./mvnw spring-boot:run
cd api-gatway && ./mvnw spring-boot:run

# Run Zipkin with Podman
podman run -d -p 9411:9411 openzipkin/zipkin
```

### Production Mode (Podman)

**📖 First time with Podman?** See [PODMAN_SETUP_GUIDE.md](PODMAN_SETUP_GUIDE.md) for detailed configuration guide.

```bash
# Build and run all services
podman-compose up --build

# Run in background
podman-compose up -d

# Stop services
podman-compose down
```

**⚙️ Podman Configuration:**
- **Rootless Podman** (recommended): Update `docker-compose.yml` line 297 socket path
- **Rootful Podman**: Use `sudo podman-compose up -d`
- Complete setup instructions in [PODMAN_SETUP_GUIDE.md](PODMAN_SETUP_GUIDE.md)

## 🔗 Access URLs

- **Eureka Dashboard**: http://localhost:8761
- **API Gateway**: http://localhost:8765
- **Zipkin UI**: http://localhost:9411

## 🛠️ Build

```bash
# Build all services
./build-all.sh

# Or build individually
cd <service-name>
./mvnw clean package -DskipTests
```

## 📚 Tech Stack

- **Java 21**
- **Spring Boot 3.5.7**
- **Spring Cloud 2025.0.0**
  - Spring Cloud Gateway
  - Netflix Eureka Server
  - Spring Cloud Sleuth
- **Zipkin**
- **Podman**
- **Maven**

## 🤝 Contributing

1. Fork the repo
2. Create feature branch (`git checkout -b feature/name`)
3. Commit changes (`git commit -m 'Add feature'`)
4. Push to branch (`git push origin feature/name`)
5. Open Pull Request



## 👤 Author

**Yassine Ait Elhaj**  
GitHub: [@Asyassin10](https://github.com/Asyassin10)  
Repository: [EadgeQuery-AI](https://github.com/Asyassin10/Eadgequry-Ai)
