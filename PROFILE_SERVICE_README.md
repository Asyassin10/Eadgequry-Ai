# Profile Service - User Profile Management Microservice

Complete REST API microservice for managing user profiles with CRUD operations.

---

## 📋 Overview

**Profile Service** is an independent microservice that manages user profile data. It has its own MySQL database and communicates with the Auth Service via REST API only (no Kafka for now).

### **Key Features:**
- ✅ Independent MySQL database (`eadgequry-user-profile`)
- ✅ Full CRUD operations with DTOs
- ✅ Profile creation via REST POST from Auth Service
- ✅ Comprehensive validation and exception handling
- ✅ Transaction management
- ✅ Health check endpoint
- ✅ Logging and monitoring
- ✅ Ready for future Kafka integration

---

## 🏗️ Architecture

### **Service Details:**
- **Port:** 8082
- **Database:** MySQL on port 3307
- **Service Name:** user-profile
- **Eureka:** Registered with service discovery

### **Database Schema:**

```sql
CREATE TABLE user_profiles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,     -- FK to auth.users.id (logical reference)
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    avatar_url VARCHAR(500) NULL,
    bio TEXT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_email (email)
);
```

### **Profile Data Fields:**
- `user_id` - Foreign key to Auth Service user (logical cross-database reference)
- `name` - User's full name
- `email` - User's email address
- `avatar_url` - URL to profile picture
- `bio` - User biography/description (up to 5000 characters)
- `created_at` - Profile creation timestamp
- `updated_at` - Last update timestamp

---

## 🚀 Setup and Running

### **1. Start MySQL Database:**

```bash
# Start the user-profile MySQL database
docker-compose up -d mysql-user-profile

# Verify it's running
docker ps | grep mysql-user-profile

# Check logs
docker logs mysql-user-profile
```

**Database Configuration:**
- Container: `mysql-user-profile`
- Port: `3307` (host) → `3306` (container)
- Database: `eadgequry-user-profile`
- User: `profileuser`
- Password: `profilepassword`

### **2. Start Eureka (Service Discovery):**

```bash
cd naming-server
./mvnw spring-boot:run
```

Wait for Eureka to be ready at: http://localhost:8761

### **3. Start Profile Service:**

```bash
cd user-profile
./mvnw spring-boot:run
```

**Profile Service will:**
- Run Flyway migrations (V1, V2, V3)
- Register with Eureka
- Listen on port 8082

### **4. Verify Service is Running:**

```bash
# Health check
curl http://localhost:8082/profiles/health

# Expected response:
{
  "status": "UP",
  "service": "user-profile"
}

# Check Eureka Dashboard
open http://localhost:8761
# You should see USER-PROFILE registered
```

---

## 📡 REST API Endpoints

### **Base URL:** `http://localhost:8082`

### **1. Create Profile**

**Called by Auth Service after user registration**

```http
POST /profiles
Content-Type: application/json

{
  "userId": 1,
  "name": "John Doe",
  "email": "john@example.com"
}
```

**Response (201 Created):**
```json
{
  "id": 1,
  "userId": 1,
  "name": "John Doe",
  "email": "john@example.com",
  "avatarUrl": null,
  "bio": null,
  "createdAt": "2025-11-04T12:00:00",
  "updatedAt": "2025-11-04T12:00:00"
}
```

**Error Responses:**
```json
// 400 Bad Request - Validation error
{
  "error": "Validation Error",
  "message": "Name is required"
}

// 400 Bad Request - Profile already exists
{
  "error": "Validation Error",
  "message": "Profile already exists for user ID: 1"
}
```

---

### **2. Get Profile by User ID**

```http
GET /profiles/{user_id}
```

**Example:**
```bash
curl http://localhost:8082/profiles/1
```

**Response (200 OK):**
```json
{
  "id": 1,
  "userId": 1,
  "name": "John Doe",
  "email": "john@example.com",
  "avatarUrl": "https://example.com/avatar.jpg",
  "bio": "Software developer and tech enthusiast",
  "createdAt": "2025-11-04T12:00:00",
  "updatedAt": "2025-11-04T13:30:00"
}
```

**Error Response (404 Not Found):**
```json
{
  "timestamp": "2025-11-04T13:45:00",
  "status": 404,
  "error": "Not Found",
  "message": "Profile not found for user ID: 999",
  "path": "/profiles/999"
}
```

---

### **3. Update Profile**

```http
PUT /profiles/{user_id}
Content-Type: application/json

{
  "name": "John Smith",
  "avatarUrl": "https://example.com/new-avatar.jpg",
  "bio": "Updated biography text"
}
```

**Notes:**
- All fields are optional
- Only provided fields will be updated
- Empty strings are not allowed for name

**Example:**
```bash
curl -X PUT http://localhost:8082/profiles/1 \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Smith",
    "bio": "Full-stack developer"
  }'
```

**Response (200 OK):**
```json
{
  "id": 1,
  "userId": 1,
  "name": "John Smith",
  "email": "john@example.com",
  "avatarUrl": "https://example.com/avatar.jpg",
  "bio": "Full-stack developer",
  "createdAt": "2025-11-04T12:00:00",
  "updatedAt": "2025-11-04T14:00:00"
}
```

**Error Responses:**
```json
// 404 Not Found
{
  "timestamp": "2025-11-04T14:00:00",
  "status": 404,
  "error": "Not Found",
  "message": "Profile not found for user ID: 999",
  "path": "/profiles/999"
}

// 400 Bad Request - Validation error
{
  "error": "Validation Error",
  "message": "Name cannot be empty"
}

// 400 Bad Request - Bio too long
{
  "error": "Validation Error",
  "message": "Bio cannot exceed 5000 characters"
}
```

---

### **4. Delete Profile**

```http
DELETE /profiles/{user_id}
```

**Example:**
```bash
curl -X DELETE http://localhost:8082/profiles/1
```

**Response (200 OK):**
```json
{
  "message": "Profile deleted successfully",
  "userId": 1
}
```

**Error Response (404 Not Found):**
```json
{
  "timestamp": "2025-11-04T14:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Profile not found for user ID: 999",
  "path": "/profiles/999"
}
```

---

### **5. Health Check**

```http
GET /profiles/health
```

**Response (200 OK):**
```json
{
  "status": "UP",
  "service": "user-profile"
}
```

---

## 🔄 Integration with Auth Service

### **Registration Flow:**

```
1. Client → Auth Service: POST /register
   Body: { "name": "John", "email": "john@example.com", "password": "secret" }

2. Auth Service:
   a) Validates request
   b) Creates user in auth database
   c) Generates user ID (e.g., ID=1)

3. Auth Service → Profile Service: POST /profiles
   Body: { "userId": 1, "name": "John", "email": "john@example.com" }

4. Profile Service:
   a) Validates request
   b) Creates profile in profile database
   c) Returns profile data

5. Auth Service → Client: Success response
```

### **Auth Service Feign Client (Example):**

```java
@FeignClient(name = "user-profile")
public interface ProfileServiceClient {

    @PostMapping("/profiles")
    ProfileResponse createProfile(@RequestBody CreateProfileRequest request);
}

// Usage in Auth Service after user creation:
CreateProfileRequest profileRequest = new CreateProfileRequest(
    savedUser.getId(),
    savedUser.getName(),
    savedUser.getEmail()
);

try {
    profileServiceClient.createProfile(profileRequest);
    logger.info("Profile created for user ID: {}", savedUser.getId());
} catch (Exception e) {
    logger.error("Failed to create profile", e);
    // Handle error (retry, compensating transaction, etc.)
}
```

---

## 🧪 Testing

### **Test 1: Create Profile**

```bash
curl -X POST http://localhost:8082/profiles \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "name": "Jane Doe",
    "email": "jane@example.com"
  }'
```

### **Test 2: Get Profile**

```bash
curl http://localhost:8082/profiles/1
```

### **Test 3: Update Profile**

```bash
curl -X PUT http://localhost:8082/profiles/1 \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Jane Smith",
    "avatarUrl": "https://example.com/jane.jpg",
    "bio": "Product designer with 5 years of experience"
  }'
```

### **Test 4: Delete Profile**

```bash
curl -X DELETE http://localhost:8082/profiles/1
```

### **Test 5: Error Handling - Profile Not Found**

```bash
curl http://localhost:8082/profiles/999
```

### **Test 6: Error Handling - Validation Error**

```bash
curl -X POST http://localhost:8082/profiles \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "name": "",
    "email": "invalid-email"
  }'
```

---

## 🗄️ Database Operations

### **View Profiles in Database:**

```bash
# Connect to MySQL
docker exec -it mysql-user-profile mysql -uprofileuser -pprofilepassword eadgequry-user-profile

# Query profiles
SELECT * FROM user_profiles;

# Count profiles
SELECT COUNT(*) FROM user_profiles;

# Find specific profile
SELECT * FROM user_profiles WHERE user_id = 1;

# Find by email
SELECT * FROM user_profiles WHERE email = 'john@example.com';
```

### **Flyway Migrations:**

```sql
-- V1: Create user_profiles table (initial structure)
-- V2: Create login_history table
-- V3: Add bio field, remove email_verified_at, provider, google_id

-- Check migration history
SELECT * FROM flyway_schema_history;
```

---

## 📊 Validation Rules

### **CreateProfileRequest:**
- `userId`: Required, must be positive number
- `name`: Required, cannot be empty
- `email`: Required, must be valid email format

### **UpdateProfileRequest:**
- `name`: Optional, but cannot be empty if provided
- `avatarUrl`: Optional
- `bio`: Optional, max 5000 characters

---

## 🐛 Troubleshooting

### **Issue 1: Service Won't Start**

**Error:** "Communications link failure"

**Solution:**
```bash
# Check MySQL is running
docker ps | grep mysql-user-profile

# Check port
docker port mysql-user-profile

# Restart database
docker-compose restart mysql-user-profile

# Wait 10 seconds and try again
./mvnw spring-boot:run
```

---

### **Issue 2: Flyway Migration Failed**

**Error:** "Validate failed: Migrations have failed validation"

**Solution:**
```bash
# Clean database and restart
docker-compose down -v  # WARNING: Deletes all data
docker-compose up -d mysql-user-profile
./mvnw spring-boot:run
```

---

### **Issue 3: Profile Creation Fails**

**Error:** "Profile already exists for user ID: X"

**Solution:**
- Profile already exists in database
- Check with: `SELECT * FROM user_profiles WHERE user_id = X;`
- Either use UPDATE endpoint or delete existing profile first

---

### **Issue 4: Service Not Registered in Eureka**

**Solution:**
```bash
# Check Eureka is running
curl http://localhost:8761

# Check application.properties
eureka.client.enabled=true
eureka.client.service-url.defaultZone=http://localhost:8761/eureka/

# Restart service
./mvnw spring-boot:run

# Wait 30 seconds and check Eureka dashboard
open http://localhost:8761
```

---

## 🔮 Future Enhancements

### **1. Kafka Integration (Planned)**

Replace REST calls with event-driven architecture:

```
Auth Service → Kafka Topic: user.registered → Profile Service
```

**Benefits:**
- Decoupled services
- Better fault tolerance
- Async processing
- Event sourcing capabilities

### **2. Login History Tracking**

Track user login attempts:
- Success/failed login records
- IP address and user agent
- Login timestamp
- Failed login count

### **3. Email Verification**

Add email verification status:
- email_verified_at timestamp
- Verification token management
- Integration with Mail Service (future)

### **4. JWT Protection**

Secure all endpoints with JWT:
- Add OAuth2 Resource Server
- Validate JWT tokens from Auth Service
- Role-based access control

---

## 📚 Summary

### **What We Built:**
✅ Complete Profile Service with REST API
✅ MySQL database with Flyway migrations
✅ Full CRUD operations with DTOs
✅ Validation and exception handling
✅ Transaction management
✅ Comprehensive logging
✅ Health check endpoint
✅ Docker configuration
✅ Ready for Kafka integration

### **Files Created:**
- 1 Entity (UserProfile)
- 3 DTOs (CreateProfileRequest, UpdateProfileRequest, ProfileResponse)
- 1 Repository (UserProfileRepository)
- 1 Service (ProfileService)
- 1 Controller (ProfileController)
- 2 Exception classes
- 1 Migration (V3)
- Docker-compose update

### **Lines of Code:** ~670 lines

This is a **production-ready Profile Service** following microservices best practices! 🚀

---

## 🔗 Related Documentation

- [Auth Service README](./AUTH_SERVICE_README.md)
- [API Gateway Configuration](./API_GATEWAY_README.md)
- [Microservices Architecture](./MICROSERVICES_ARCHITECTURE.md)

---

## 📞 Support

For issues or questions, check:
1. Service logs: `./mvnw spring-boot:run`
2. Database: `docker logs mysql-user-profile`
3. Eureka Dashboard: http://localhost:8761
4. Health Check: http://localhost:8082/profiles/health
