# Chatbot Service - Testing Guide & Workflow

## System Architecture Overview

```
┌─────────────┐       ┌──────────────┐       ┌─────────────────┐       ┌──────────────────┐
│   Client    │──────▶│ API Gateway  │──────▶│ Chatbot Service │──────▶│ Datasource       │
│             │       │  (Port 8765) │       │  (Port 8087)    │       │ Service          │
└─────────────┘       └──────────────┘       └─────────────────┘       │  (Port 8083)     │
                             │                        │                 └──────────────────┘
                             │                        │                          │
                             ▼                        ▼                          ▼
                      ┌──────────────┐       ┌──────────────┐         ┌──────────────────┐
                      │ Auth Service │       │  AI API      │         │ User's Database  │
                      │  (Port 8081) │       │ (OpenRouter) │         │ (MySQL/Postgres) │
                      └──────────────┘       └──────────────┘         └──────────────────┘
```

## Complete Workflow

### **Step 1: User Authentication**
Get JWT token for API access

### **Step 2: Database Configuration**
Create/Configure database connection in datasource service

### **Step 3: Ask Questions**
Send natural language questions to chatbot

### **Step 4: Chatbot Processing**
1. Receive question
2. Get database schema from datasource
3. Generate SQL query using AI
4. Execute query on datasource (with security validation)
5. Get results and generate natural language answer
6. Return response to user

---

## API Endpoints

### Base URLs
- **API Gateway**: `http://localhost:8765`
- **Auth Service**: `http://localhost:8081` (via gateway: `/auth`)
- **Datasource Service**: `http://localhost:8083` (via gateway: `/datasource`)
- **Chatbot Service**: `http://localhost:8087` (via gateway: `/chatbot`)

---

## Testing Steps

### **STEP 1: Authentication**

#### 1.1 Register User
```bash
curl -X POST http://localhost:8765/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "testuser@example.com",
    "password": "Test@123456"
  }'
```

**Response:**
```json
{
  "id": 1,
  "username": "testuser",
  "email": "testuser@example.com"
}
```

#### 1.2 Login
```bash
curl -X POST http://localhost:8765/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "Test@123456"
  }'
```

**Response:**
```json
{
  "token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "userId": 1,
  "username": "testuser",
  "email": "testuser@example.com"
}
```

**Save this token for subsequent requests!**

---

### **STEP 2: Configure Database**

#### 2.1 Create Database Configuration
```bash
TOKEN="YOUR_JWT_TOKEN_HERE"

curl -X POST http://localhost:8765/datasource/configs/user/1 \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "My MySQL Database",
    "type": "mysql",
    "host": "localhost",
    "port": 3306,
    "databaseName": "testdb",
    "username": "root",
    "password": "password"
  }'
```

**Response:**
```json
{
  "id": 1,
  "name": "My MySQL Database",
  "type": "mysql",
  "host": "localhost",
  "port": 3306,
  "databaseName": "testdb",
  "username": "root",
  "createdAt": "2025-01-15T10:30:00",
  "updatedAt": "2025-01-15T10:30:00"
}
```

**Save the database config ID (e.g., 1) for chatbot requests!**

#### 2.2 Test Database Connection
```bash
curl -X POST http://localhost:8765/datasource/configs/1/user/1/test \
  -H "Authorization: Bearer $TOKEN"
```

**Response:**
```json
{
  "success": true,
  "message": "Connection successful"
}
```

#### 2.3 Get All Database Configs
```bash
curl -X GET http://localhost:8765/datasource/configs/user/1 \
  -H "Authorization: Bearer $TOKEN"
```

---

### **STEP 3: Ask Questions (Non-Streaming)**

#### 3.1 Simple Query
```bash
curl -X POST http://localhost:8765/chatbot/ask \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "question": "Show all users",
    "databaseConfigId": 1,
    "userId": 1
  }'
```

**Response:**
```json
{
  "success": true,
  "question": "Show all users",
  "sqlQuery": "SELECT * FROM users",
  "sqlResult": [
    {
      "name": "John Doe",
      "email": "john@example.com",
      "created_at": "2025-01-01T00:00:00"
    },
    {
      "name": "Jane Smith",
      "email": "jane@example.com",
      "created_at": "2025-01-02T00:00:00"
    }
  ],
  "answer": "I found 2 users in the database:\n\n| Name | Email | Created At |\n|------|-------|------------|\n| John Doe | john@example.com | 2025-01-01 |\n| Jane Smith | jane@example.com | 2025-01-02 |"
}
```

#### 3.2 Count Query
```bash
curl -X POST http://localhost:8765/chatbot/ask \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "question": "How many users are registered?",
    "databaseConfigId": 1,
    "userId": 1
  }'
```

**Response:**
```json
{
  "success": true,
  "question": "How many users are registered?",
  "sqlQuery": "SELECT COUNT(*) as total FROM users",
  "sqlResult": [
    {
      "total": 2
    }
  ],
  "answer": "There are 2 registered users in the database."
}
```

#### 3.3 Filtered Query
```bash
curl -X POST http://localhost:8765/chatbot/ask \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "question": "Show users with email containing gmail",
    "databaseConfigId": 1,
    "userId": 1
  }'
```

**Response:**
```json
{
  "success": true,
  "question": "Show users with email containing gmail",
  "sqlQuery": "SELECT * FROM users WHERE email LIKE '%gmail%'",
  "sqlResult": [
    {
      "name": "John Doe",
      "email": "john@gmail.com"
    }
  ],
  "answer": "I found 1 user with gmail in their email:\n\nJohn Doe (john@gmail.com)"
}
```

#### 3.4 Join Query
```bash
curl -X POST http://localhost:8765/chatbot/ask \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "question": "Show all orders with user names",
    "databaseConfigId": 1,
    "userId": 1
  }'
```

---

### **STEP 4: Ask Questions (Streaming Response)**

#### 4.1 Streaming Request
```bash
curl -X POST http://localhost:8765/chatbot/ask/stream \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "question": "Show all products with price greater than 100",
    "databaseConfigId": 1,
    "userId": 1
  }' \
  --no-buffer
```

**Response (Server-Sent Events):**
```
data: I
data:
data: found
data:
data: 5
data:
data: products
data:
data: with
data:
data: price
data:
data: greater
data:
data: than
data:
data: 100
...
```

---

### **STEP 5: Conversation History**

#### 5.1 Get User Conversation History
```bash
curl -X GET http://localhost:8765/chatbot/history/user/1 \
  -H "Authorization: Bearer $TOKEN"
```

**Response:**
```json
[
  {
    "id": 1,
    "userId": 1,
    "databaseConfigId": 1,
    "sessionId": "session-123",
    "question": "Show all users",
    "sqlQuery": "SELECT * FROM users",
    "sqlResult": [...],
    "answer": "I found 2 users...",
    "createdAt": "2025-01-15T10:35:00"
  },
  {
    "id": 2,
    "userId": 1,
    "databaseConfigId": 1,
    "sessionId": "session-123",
    "question": "How many users are registered?",
    "sqlQuery": "SELECT COUNT(*) as total FROM users",
    "sqlResult": [...],
    "answer": "There are 2 registered users...",
    "createdAt": "2025-01-15T10:36:00"
  }
]
```

#### 5.2 Get Session History
```bash
curl -X GET http://localhost:8765/chatbot/history/session/session-123 \
  -H "Authorization: Bearer $TOKEN"
```

---

### **STEP 6: Direct Query Execution (Optional)**

You can also execute SQL directly on datasource (bypasses chatbot):

```bash
curl -X POST "http://localhost:8765/datasource/query/execute?databaseConfigId=1&userId=1" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: text/plain" \
  -d "SELECT * FROM users LIMIT 10"
```

**Response:**
```json
{
  "success": true,
  "sqlQuery": "SELECT * FROM users LIMIT 10",
  "result": [
    {
      "name": "John Doe",
      "email": "john@example.com"
    }
  ],
  "rowCount": 1,
  "executionTimeMs": 45
}
```

---

## Security Validation

### What queries are ALLOWED:
✅ `SELECT * FROM users`
✅ `SELECT name, email FROM users WHERE age > 18`
✅ `SELECT COUNT(*) FROM orders`
✅ `SELECT u.name, o.total FROM users u JOIN orders o ON u.id = o.user_id`

### What queries are BLOCKED:
❌ `DELETE FROM users` → "Forbidden SQL keyword detected: DELETE"
❌ `DROP TABLE users` → "Forbidden SQL keyword detected: DROP"
❌ `UPDATE users SET name = 'hacked'` → "Forbidden SQL keyword detected: UPDATE"
❌ `INSERT INTO users VALUES (...)` → "Forbidden SQL keyword detected: INSERT"
❌ `TRUNCATE users` → "Forbidden SQL keyword detected: TRUNCATE"
❌ `CREATE TABLE test (...)` → "Forbidden SQL keyword detected: CREATE"

### Example Blocked Query:
```bash
curl -X POST http://localhost:8765/chatbot/ask \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "question": "Delete all users",
    "databaseConfigId": 1,
    "userId": 1
  }'
```

**Response:**
```json
{
  "success": false,
  "error": "Query execution failed: Security error: Forbidden SQL keyword detected: DELETE. Only SELECT queries are allowed."
}
```

---

## Health Checks

### Chatbot Service Health
```bash
curl http://localhost:8765/chatbot/health
```
Response: `Chatbot service is running`

### Datasource Service Health
```bash
curl http://localhost:8765/datasource/health
```

---

## Test Scenarios

### Scenario 1: Simple Database Query
1. Login → Get token
2. Create database config
3. Ask: "Show all users"
4. Verify response contains SQL + results + answer

### Scenario 2: Complex Query with Filters
1. Ask: "Show users registered in last 30 days"
2. Verify AI generates proper WHERE clause with date filters

### Scenario 3: Aggregation Query
1. Ask: "What is the total revenue?"
2. Verify AI generates SUM() query

### Scenario 4: Join Query
1. Ask: "Show all orders with customer names"
2. Verify AI generates JOIN query

### Scenario 5: Security Test
1. Ask: "Delete all records from users table"
2. Verify request is BLOCKED with security error

### Scenario 6: Streaming Response
1. Use `/ask/stream` endpoint
2. Verify response streams word-by-word

### Scenario 7: Conversation History
1. Ask multiple questions
2. Get conversation history
3. Verify all questions/answers are stored

---

## Error Handling

### Invalid Database Config
```bash
curl -X POST http://localhost:8765/chatbot/ask \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "question": "Show users",
    "databaseConfigId": 999,
    "userId": 1
  }'
```
**Response:**
```json
{
  "success": false,
  "error": "Error: Database configuration not found for id: 999 and user: 1"
}
```

### Invalid SQL Generated
If AI generates invalid SQL, chatbot will retry up to 3 times with error feedback.

### Empty Results
```bash
curl -X POST http://localhost:8765/chatbot/ask \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "question": "Show users with age 999",
    "databaseConfigId": 1,
    "userId": 1
  }'
```
**Response:**
```json
{
  "success": true,
  "question": "Show users with age 999",
  "sqlQuery": "SELECT * FROM users WHERE age = 999",
  "sqlResult": [],
  "answer": "No users were found matching the criteria (age = 999). The query returned no results. You may want to check if there are any users with this age in the database."
}
```

---

## Swagger UI

Access interactive API documentation:
- **All Services**: http://localhost:8765/swagger-ui.html
- **Chatbot Only**: http://localhost:8765/chatbot/swagger-ui.html
- **Datasource Only**: http://localhost:8765/datasource/swagger-ui.html

---

## Environment Variables for AI Service

Make sure to configure AI API credentials in `chat-bot-service/src/main/resources/application.properties`:

```properties
# AI API Configuration
ai.api.url=https://openrouter.ai/api/v1/chat/completions
ai.api.key=YOUR_OPENROUTER_API_KEY
ai.api.model=google/gemma-2-9b-it:free
ai.api.timeout=30000
ai.api.max-tokens=500
ai.api.temperature.query=0.1
ai.api.temperature.answer=0.3
```

---

## Complete Test Script

```bash
#!/bin/bash

# Set base URL
BASE_URL="http://localhost:8765"

echo "=== Step 1: Register User ==="
REGISTER_RESPONSE=$(curl -s -X POST $BASE_URL/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "Test@123456"
  }')
echo $REGISTER_RESPONSE

echo -e "\n=== Step 2: Login ==="
LOGIN_RESPONSE=$(curl -s -X POST $BASE_URL/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "Test@123456"
  }')
echo $LOGIN_RESPONSE

# Extract token
TOKEN=$(echo $LOGIN_RESPONSE | jq -r '.token')
USER_ID=$(echo $LOGIN_RESPONSE | jq -r '.userId')

echo -e "\n=== Step 3: Create Database Config ==="
DB_CONFIG_RESPONSE=$(curl -s -X POST $BASE_URL/datasource/configs/user/$USER_ID \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test Database",
    "type": "mysql",
    "host": "localhost",
    "port": 3306,
    "databaseName": "testdb",
    "username": "root",
    "password": "password"
  }')
echo $DB_CONFIG_RESPONSE

# Extract database config ID
DB_CONFIG_ID=$(echo $DB_CONFIG_RESPONSE | jq -r '.id')

echo -e "\n=== Step 4: Ask Question ==="
curl -s -X POST $BASE_URL/chatbot/ask \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d "{
    \"question\": \"Show all users\",
    \"databaseConfigId\": $DB_CONFIG_ID,
    \"userId\": $USER_ID
  }" | jq '.'

echo -e "\n=== Step 5: Get Conversation History ==="
curl -s -X GET $BASE_URL/chatbot/history/user/$USER_ID \
  -H "Authorization: Bearer $TOKEN" | jq '.'

echo -e "\nTest completed!"
```

---

## Summary

The chatbot service is now:
- ✅ **Global and language-agnostic** (no French, no language-specific logic)
- ✅ **Simple** (Question → SQL → Execute → Answer)
- ✅ **Secure** (SQL validation in datasource - blocks DELETE, DROP, UPDATE, etc.)
- ✅ **Streaming support** (Server-Sent Events)
- ✅ **Conversation history** (stores all Q&A)
- ✅ **Multi-database support** (MySQL, PostgreSQL, SQL Server, Oracle, H2)

Use the endpoints above to test the complete workflow!
