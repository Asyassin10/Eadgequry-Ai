# Testing Guide - JWT Authentication

This guide shows how to test the JWT authentication with services running on **localhost** (only MySQL and Zipkin in Docker).

---

## 🚀 Quick Start

### 1. Start MySQL and Zipkin with Docker

```bash
# Start only MySQL and Zipkin
docker-compose up -d mysql phpmyadmin zipkin
```

**Verify containers are running:**
```bash
docker ps
```

You should see:
- `mysql-auth` (port 3306)
- `phpmyadmin` (port 8080)
- `zipkin` (port 9411)

---

### 2. Insert Test User in Database

**Option A: Using phpMyAdmin (http://localhost:8080)**
- Login: `authuser` / `authpassword`
- Select database: `eadgequry-auth`
- Go to SQL tab and run:

```sql
INSERT INTO users (name, email, password, provider, created_at, updated_at)
VALUES (
  'Test User',
  'test@example.com',
  '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
  'local',
  NOW(),
  NOW()
);
```

**Option B: Using MySQL CLI**
```bash
docker exec -it mysql-auth mysql -uauthuser -pauthpassword eadgequry-auth -e "
INSERT INTO users (name, email, password, provider, created_at, updated_at)
VALUES (
  'Test User',
  'test@example.com',
  '\$2a\$10\$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
  'local',
  NOW(),
  NOW()
);
"
```

**Test credentials:**
- Email: `test@example.com`
- Password: `password123`

---

### 3. Build All Services

```bash
./build-all.sh
```

Wait until all services are built successfully.

---

### 4. Start Services on Localhost

**Open 3 separate terminal windows:**

**Terminal 1 - Naming Server (Eureka):**
```bash
cd naming-server
./mvnw spring-boot:run
```

Wait for: `Tomcat started on port(s): 8761`

**Terminal 2 - Auth Service:**
```bash
cd auth
./mvnw spring-boot:run
```

Wait for:
- `Flyway successfully applied 1 migration`
- `Tomcat started on port(s): 8081`

**Terminal 3 - API Gateway:**
```bash
cd api-gatway
./mvnw spring-boot:run
```

Wait for: `Netty started on port 8765`

---

### 5. Verify Services are Running

**Check Eureka Dashboard:**
```
http://localhost:8761
```

You should see 2 registered services:
- `AUTH` (port 8081)
- `API-GATWDAY` (port 8765)

**Check Health Endpoints:**
```bash
# Naming Server
curl http://localhost:8761/actuator/health

# Auth Service
curl http://localhost:8081/actuator/health

# API Gateway
curl http://localhost:8765/actuator/health
```

---

## 🧪 Testing JWT Authentication

### Test 1: Login - Get JWT Token

**Direct to Auth Service:**
```bash
curl -X POST http://localhost:8081/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123"
  }'
```

**Through API Gateway:**
```bash
curl -X POST http://localhost:8765/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123"
  }'
```

**Expected Response:**
```json
{
  "token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzZWxmIiwiaWF0IjoxNzMwNzI..."
}
```

**Copy the token value!** You'll need it for the next steps.

---

### Test 2: Test Invalid Credentials

```bash
curl -X POST http://localhost:8765/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "wrongpassword"
  }'
```

**Expected Response:**
```
Invalid credentials
```

---

### Test 3: Access Protected Endpoint (Without Token)

```bash
curl -v http://localhost:8765/auth/actuator/health
```

**Expected:** `401 Unauthorized` or authentication required

---

### Test 4: Access Protected Endpoint (With Token)

```bash
# Replace YOUR_JWT_TOKEN with the actual token from Test 1
TOKEN="eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9..."

curl http://localhost:8765/auth/actuator/health \
  -H "Authorization: Bearer $TOKEN"
```

**Expected Response:**
```json
{
  "status": "UP",
  "groups": ["liveness", "readiness"]
}
```

---

### Test 5: Verify JWT Token Contents

**Get Public Key:**
```bash
curl http://localhost:8081/.well-known/jwks.json
```

**Decode JWT Token (using jwt.io or command line):**
```bash
# Extract payload (middle part between dots)
echo "YOUR_JWT_TOKEN" | cut -d'.' -f2 | base64 -d 2>/dev/null | jq
```

**Expected Payload:**
```json
{
  "iss": "self",
  "sub": "test@example.com",
  "exp": 1730723456,
  "iat": 1730721656,
  "scope": "USER"
}
```

---

## 🔍 Troubleshooting

### Issue: Flyway Migration Error

**Error:** `Validate failed: Detected applied migration not resolved locally`

**Solution:**
```bash
docker exec -it mysql-auth mysql -uauthuser -pauthpassword eadgequry-auth -e "
DROP TABLE IF EXISTS flyway_schema_history;
DROP TABLE IF EXISTS users;
"

# Then restart auth service
```

---

### Issue: Cannot Connect to MySQL

**Error:** `Communications link failure`

**Check:**
```bash
docker logs mysql-auth
docker exec -it mysql-auth mysql -uauthuser -pauthpassword -e "SELECT 1;"
```

**Solution:** Wait 30 seconds for MySQL to fully start, then retry.

---

### Issue: Services Not Registering with Eureka

**Check Eureka logs:**
```
http://localhost:8761
```

**Solution:** Wait 30 seconds for service registration, then refresh Eureka dashboard.

---

### Issue: JWT Token Validation Failed

**Error:** `An error occurred while attempting to decode the Jwt`

**Check:**
1. Auth service is running: `curl http://localhost:8081/.well-known/jwks.json`
2. Gateway can reach auth: Check gateway logs for errors
3. Token is not expired (valid for 30 minutes)

**Solution:** Get a fresh token from `/login` endpoint.

---

## 📊 Service Ports Reference

| Service | Port | URL |
|---------|------|-----|
| **Naming Server (Eureka)** | 8761 | http://localhost:8761 |
| **Auth Service** | 8081 | http://localhost:8081 |
| **API Gateway** | 8765 | http://localhost:8765 |
| **MySQL** | 3306 | localhost:3306 |
| **phpMyAdmin** | 8080 | http://localhost:8080 |
| **Zipkin** | 9411 | http://localhost:9411 |

---

## 🔐 API Endpoints

### Public Endpoints (No Authentication Required)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/auth/login` | Login with email/password |
| GET | `/auth/.well-known/jwks.json` | Get JWT public keys |
| GET | `/auth/actuator/health` | Health check |

### Protected Endpoints (JWT Token Required)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/auth/actuator/info` | Service information |
| GET | `/auth/actuator/metrics` | Service metrics |

**Note:** All protected endpoints require `Authorization: Bearer <token>` header.

---

## 🧑‍💻 Testing with Postman

### 1. Create a Collection

**Name:** Eadgequry Auth Testing

### 2. Add Login Request

- **Method:** POST
- **URL:** `http://localhost:8765/auth/login`
- **Headers:**
  ```
  Content-Type: application/json
  ```
- **Body (raw JSON):**
  ```json
  {
    "email": "test@example.com",
    "password": "password123"
  }
  ```
- **Tests (to save token):**
  ```javascript
  var jsonData = pm.response.json();
  pm.environment.set("jwt_token", jsonData.token);
  ```

### 3. Add Protected Request

- **Method:** GET
- **URL:** `http://localhost:8765/auth/actuator/health`
- **Headers:**
  ```
  Authorization: Bearer {{jwt_token}}
  ```

---

## 📝 Creating More Test Users

**BCrypt Password Generator:**

```bash
# Using Python
python3 -c "from passlib.hash import bcrypt; print(bcrypt.hash('your-password'))"

# Using Online Tool
# Visit: https://bcrypt-generator.com/
# Enter your password, rounds: 10
```

**Insert into Database:**
```sql
INSERT INTO users (name, email, password, provider, created_at, updated_at)
VALUES (
  'John Doe',
  'john@example.com',
  'YOUR_BCRYPT_HASH_HERE',
  'local',
  NOW(),
  NOW()
);
```

---

## 🛑 Stopping Services

**Stop Spring Boot services:**
- Press `Ctrl+C` in each terminal

**Stop Docker containers:**
```bash
docker-compose down
```

**Keep data (don't remove volumes):**
```bash
docker-compose stop
```

**Remove everything (including database data):**
```bash
docker-compose down -v
```

---

## 🎯 Next Steps

1. ✅ Add user registration endpoint
2. ✅ Add password reset functionality
3. ✅ Add refresh token support
4. ✅ Add email verification
5. ✅ Add OAuth2 social login (Google)
6. ✅ Add more microservices and protect them with JWT

---

**Happy Testing! 🚀**
