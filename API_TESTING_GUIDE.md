# 🧪 API Testing Guide - Complete Endpoints Reference

This guide provides all endpoints with examples to test authentication, authorization, and error handling.

---

## 📋 Table of Contents

1. [Public Endpoints (No Auth Required)](#public-endpoints)
2. [Protected Endpoints (JWT Required)](#protected-endpoints)
3. [Error Scenarios](#error-scenarios)
4. [Testing Workflow](#testing-workflow)

---

## 🌐 Base URLs

| Service | Direct URL | Via Gateway |
|---------|-----------|-------------|
| **Auth Service** | `http://localhost:8081` | `http://localhost:8765/auth` |
| **API Gateway** | `http://localhost:8765` | - |
| **Eureka Server** | `http://localhost:8761` | - |

**Recommendation:** Always test via Gateway (`http://localhost:8765/auth`) to test the full flow.

---

## 🔓 Public Endpoints (No Auth Required)

### 1. Register New User

**Description:** Create a new user account

**Endpoint:** `POST /auth/register`

**Request:**
```bash
curl -X POST http://localhost:8765/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Doe",
    "email": "john@example.com",
    "password": "mypassword123"
  }'
```

**Success Response (201 Created):**
```json
{
  "id": 1,
  "name": "John Doe",
  "email": "john@example.com",
  "provider": "local",
  "avatarUrl": null
}
```

**Error Responses:**

**Email already exists (400 Bad Request):**
```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Email already registered",
  "path": "/register",
  "timestamp": "2025-11-03T16:30:00"
}
```

**Invalid email format (400 Bad Request):**
```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid email format",
  "path": "/register",
  "timestamp": "2025-11-03T16:30:00"
}
```

**Password too short (400 Bad Request):**
```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Password must be at least 6 characters",
  "path": "/register",
  "timestamp": "2025-11-03T16:30:00"
}
```

---

### 2. Login (Get JWT Token)

**Description:** Authenticate and receive JWT token

**Endpoint:** `POST /auth/login`

**Request:**
```bash
curl -X POST http://localhost:8765/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@example.com",
    "password": "mypassword123"
  }'
```

**Success Response (200 OK):**
```json
{
  "token": "eyJraWQiOiI5YzUxY2FhMC04MGFmLTQzZjUtOTU1MS0zNDRiMzBjZTEzYmMiLCJhbGciOiJSUzI1NiJ9.eyJpc3MiOiJzZWxmIiwic3ViIjoidGVzdEBleGFtcGxlLmNvbSIsImV4cCI6MTc2MjE4ODczNywiaWF0IjoxNzYyMTg2OTM3LCJzY29wZSI6IlVTRVIifQ.ZMdPU5p76NcvsYKHh5ivBuYCyzTJhHEFI8jJCPtDmytOsmXSUD11uhrDL9JicYwJSn1wBXy6zHu2LHcNV2Zeazc3GVgvQtEmIAgbQvA0HCE8GRSuOfzxHk6fPcmb6H8Ysi4Bso8k3NVlBvdgGPW0t8Cra6AjFBg6Zv0MW6SUil4oz-37Epe3Eg3eP5slf9gPjtghvB9JA6BlfiUTf17LLHNOWxlElNDz_0r69GFIzmloxaARw5LwLi4j6pQsJOJa-rYWXGzmVSBTM1btm7RvrlvMyK0mrGrDFdzftBV2nTj6hYa4cYBSmMbO7IEFEWHvVUJst-HhsTj4fnmFiAitKg"
}
```

**⚠️ SAVE THIS TOKEN!** You'll need it for protected endpoints.

**Error Response - Invalid credentials (401 Unauthorized):**
```json
{
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid email or password. Please check your credentials and try again.",
  "path": "/login",
  "timestamp": "2025-11-03T16:30:00"
}
```

---

### 3. Health Check

**Description:** Check if auth service is running

**Endpoint:** `GET /auth/health`

**Request:**
```bash
curl http://localhost:8765/auth/health
```

**Response (200 OK):**
```json
{
  "status": "UP",
  "service": "auth"
}
```

---

### 4. Test Password Encoder

**Description:** Verify BCrypt password encoder is working correctly

**Endpoint:** `GET /auth/test-password-encoder`

**Request:**
```bash
curl http://localhost:8765/auth/test-password-encoder
```

**Response (200 OK):**
```json
{
  "passwordEncoderWorks": true,
  "message": "Password encoder is working correctly"
}
```

---

### 5. Get Public Keys (JWKS)

**Description:** Get RSA public keys for JWT verification

**Endpoint:** `GET /auth/.well-known/jwks.json`

**Request:**
```bash
curl http://localhost:8765/auth/.well-known/jwks.json
```

**Response (200 OK):**
```json
{
  "keys": [
    {
      "kty": "RSA",
      "e": "AQAB",
      "kid": "9c51caa0-80af-43f5-9551-344b30ce13bc",
      "n": "xGz8F6KqrT4vZ..."
    }
  ]
}
```

---

## 🔒 Protected Endpoints (JWT Required)

**⚠️ All protected endpoints require JWT token in Authorization header:**

```
Authorization: Bearer YOUR_JWT_TOKEN
```

### 1. Get Service Info

**Endpoint:** `GET /auth/actuator/info`

**Request:**
```bash
TOKEN="eyJraWQiOiI5YzUxY2FhMC04M..."

curl http://localhost:8765/auth/actuator/info \
  -H "Authorization: Bearer $TOKEN"
```

**Success Response (200 OK):**
```json
{
  "app": {
    "name": "auth",
    "version": "0.0.1-SNAPSHOT"
  }
}
```

**Error Response - No token (401 Unauthorized):**
```json
{
  "timestamp": "2025-11-03T16:30:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Authentication failed. Please provide a valid JWT token in the Authorization header.",
  "path": "/auth/actuator/info"
}
```

---

### 2. Get Metrics

**Endpoint:** `GET /auth/actuator/metrics`

**Request:**
```bash
curl http://localhost:8765/auth/actuator/metrics \
  -H "Authorization: Bearer $TOKEN"
```

**Success Response (200 OK):**
```json
{
  "names": [
    "jvm.memory.used",
    "jvm.threads.live",
    "http.server.requests",
    ...
  ]
}
```

---

## ❌ Error Scenarios

### Scenario 1: Invalid Endpoint (404)

**Request:**
```bash
curl http://localhost:8765/auth/nonexistent
```

**Response (404 Not Found):**
```json
{
  "timestamp": "2025-11-03T16:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "No endpoint found at this path",
  "path": "/auth/nonexistent"
}
```

---

### Scenario 2: No Authorization Header (401)

**Request:**
```bash
curl http://localhost:8765/auth/actuator/info
```

**Response (401 Unauthorized):**
```json
{
  "timestamp": "2025-11-03T16:30:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Authentication failed. Please provide a valid JWT token in the Authorization header.",
  "path": "/auth/actuator/info"
}
```

---

### Scenario 3: Invalid JWT Token (401)

**Request:**
```bash
curl http://localhost:8765/auth/actuator/info \
  -H "Authorization: Bearer invalid-token-here"
```

**Response (401 Unauthorized):**
```json
{
  "timestamp": "2025-11-03T16:30:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Authentication failed. Please provide a valid JWT token in the Authorization header.",
  "path": "/auth/actuator/info"
}
```

---

### Scenario 4: Expired JWT Token (401)

**Request:**
```bash
# Use a token that's older than 30 minutes
curl http://localhost:8765/auth/actuator/info \
  -H "Authorization: Bearer $EXPIRED_TOKEN"
```

**Response (401 Unauthorized):**
```json
{
  "timestamp": "2025-11-03T16:30:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Authentication failed. Please provide a valid JWT token in the Authorization header.",
  "path": "/auth/actuator/info"
}
```

---

### Scenario 5: Service Not Found (503)

**Request:**
```bash
# When auth service is not registered in Eureka
curl http://localhost:8765/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@test.com","password":"pass"}'
```

**Response (503 Service Unavailable):**
```json
{
  "timestamp": "2025-11-03T16:30:00",
  "status": 503,
  "error": "Service Unavailable",
  "message": "Unable to find instance for auth",
  "path": "/auth/login"
}
```

**Fix:** Make sure auth service is running and registered in Eureka.

---

### Scenario 6: Wrong Path (Example: /adsdsuth/test)

**Request:**
```bash
curl http://localhost:8765/adsdsuth/test
```

**Response (401 Unauthorized - Because it's not in permitAll):**
```json
{
  "timestamp": "2025-11-03T16:30:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Authentication failed. Please provide a valid JWT token in the Authorization header.",
  "path": "/adsdsuth/test"
}
```

**Explanation:** Gateway requires authentication for all paths except those explicitly in `permitAll()`. Since `/adsdsuth/test` is not in the list, it requires JWT token.

---

## 🧪 Testing Workflow

### Step 1: Start All Services

```bash
# Terminal 1 - Eureka
cd naming-server
./mvnw spring-boot:run

# Terminal 2 - Auth Service
cd auth
./mvnw spring-boot:run

# Terminal 3 - API Gateway
cd api-gatway
./mvnw spring-boot:run
```

Wait for all services to start and register with Eureka.

---

### Step 2: Verify Services are Running

```bash
# Check Eureka Dashboard
open http://localhost:8761

# Check Auth Health
curl http://localhost:8765/auth/health

# Should return: {"status":"UP","service":"auth"}
```

---

### Step 3: Register a User

```bash
curl -X POST http://localhost:8765/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test User",
    "email": "test@example.com",
    "password": "password123"
  }'
```

Save the user ID from response.

---

### Step 4: Login to Get Token

```bash
curl -X POST http://localhost:8765/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123"
  }'
```

**Copy the token from response!**

---

### Step 5: Save Token as Variable

```bash
TOKEN="eyJraWQiOiI5YzUxY2FhMC04MGFmLTQzZjUtOTU1MS0zNDRiMzBjZTEzYmMiLCJhbGciOiJSUzI1NiJ9..."
```

---

### Step 6: Test Protected Endpoints

```bash
# Test with valid token
curl http://localhost:8765/auth/actuator/info \
  -H "Authorization: Bearer $TOKEN"

# Should return service info
```

---

### Step 7: Test Error Scenarios

```bash
# Test without token (should get 401)
curl http://localhost:8765/auth/actuator/info

# Test with invalid token (should get 401)
curl http://localhost:8765/auth/actuator/info \
  -H "Authorization: Bearer invalid-token"

# Test wrong credentials (should get 401 with message)
curl -X POST http://localhost:8765/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "wrongpassword"
  }'

# Test invalid endpoint (should get 401)
curl http://localhost:8765/randompath
```

---

## 📊 Quick Test Commands

Copy and paste these to test quickly:

```bash
# Set base URL
BASE_URL="http://localhost:8765/auth"

# Register
curl -X POST $BASE_URL/register \
  -H "Content-Type: application/json" \
  -d '{"name":"Quick Test","email":"quick@test.com","password":"test123"}'

# Login
curl -X POST $BASE_URL/login \
  -H "Content-Type: application/json" \
  -d '{"email":"quick@test.com","password":"test123"}'

# Save token (replace with actual token)
TOKEN="paste-your-token-here"

# Test protected endpoint
curl $BASE_URL/actuator/info -H "Authorization: Bearer $TOKEN"

# Test without token (should fail)
curl $BASE_URL/actuator/info

# Test invalid endpoint (should fail with 401)
curl http://localhost:8765/invalidpath
```

---

## 🎯 Expected Behavior Summary

| Scenario | Expected Status | Expected Message |
|----------|----------------|------------------|
| **Register new user** | 201 | User object with ID |
| **Register duplicate email** | 400 | "Email already registered" |
| **Login valid credentials** | 200 | JWT token |
| **Login invalid credentials** | 401 | "Invalid email or password..." |
| **Access protected without token** | 401 | "Authentication failed..." |
| **Access protected with valid token** | 200 | Resource data |
| **Access protected with invalid token** | 401 | "Authentication failed..." |
| **Access protected with expired token** | 401 | "Authentication failed..." |
| **Access invalid endpoint** | 401 | "Authentication failed..." |
| **Service not available** | 503 | "Unable to find instance for auth" |

---

## 🔍 Debugging Tips

**Problem:** Getting 503 Service Unavailable
- **Check:** Is auth service running?
- **Check:** Is auth registered in Eureka? (http://localhost:8761)
- **Fix:** Wait 30 seconds after starting auth service

**Problem:** Getting 401 on valid token
- **Check:** Has token expired? (30 minutes validity)
- **Check:** Did you restart auth service? (new RSA keys invalidate old tokens)
- **Fix:** Login again to get a fresh token

**Problem:** Getting "Invalid credentials" on login
- **Check:** Is user registered?
- **Check:** Is password correct?
- **Fix:** Register user first or check password

**Problem:** Getting generic 401 without details
- **Check:** Is GlobalErrorWebExceptionHandler running?
- **Check:** Gateway logs for errors
- **Fix:** Restart gateway after adding exception handler

---

## 📝 Postman Collection

### Setup

1. Create environment variable: `baseUrl` = `http://localhost:8765`
2. Create environment variable: `token` = (empty initially)

### Requests

**1. Register**
- Method: POST
- URL: `{{baseUrl}}/auth/register`
- Body:
```json
{
  "name": "{{$randomFullName}}",
  "email": "{{$randomEmail}}",
  "password": "password123"
}
```

**2. Login**
- Method: POST
- URL: `{{baseUrl}}/auth/login`
- Body:
```json
{
  "email": "test@example.com",
  "password": "password123"
}
```
- Tests:
```javascript
pm.environment.set("token", pm.response.json().token);
```

**3. Protected Endpoint**
- Method: GET
- URL: `{{baseUrl}}/auth/actuator/info`
- Headers: `Authorization: Bearer {{token}}`

---

**Happy Testing! 🚀**

Need help? Check:
- TESTING.md - General testing guide
- QUICK_TEST.md - Troubleshooting guide
- API logs for detailed error messages
