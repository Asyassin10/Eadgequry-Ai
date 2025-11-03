# Quick Testing Guide - Fix "Invalid Credentials"

## 🚨 Problem: Getting "Invalid credentials" when trying to login?

Follow these steps to diagnose and fix the issue.

---

## Step 1: Test Password Encoder

First, verify the password encoder is working correctly:

```bash
curl http://localhost:8081/test-password-encoder
```

**Expected response:**
```json
{
  "passwordEncoderWorks": true,
  "message": "Password encoder is working correctly"
}
```

**If `false`**, there's a problem with BCrypt configuration. Check auth service logs.

---

## Step 2: Check if User Exists in Database

```bash
docker exec -it mysql-auth mysql -uauthuser -pauthpassword eadgequry-auth -e "SELECT id, name, email, LEFT(password, 30) as pwd_hash, provider FROM users WHERE email='test@example.com';"
```

**Expected output:**
```
+----+-----------+-------------------+--------------------------------+----------+
| id | name      | email             | pwd_hash                       | provider |
+----+-----------+-------------------+--------------------------------+----------+
|  1 | Test User | test@example.com  | $2a$10$N9qo8uLOickgx2ZMRZoMye     | local    |
+----+-----------+-------------------+--------------------------------+----------+
```

**If empty (no rows)**, the user doesn't exist. Create one using registration endpoint (see Step 4).

**If password is NULL**, that's the problem! Update it:
```bash
docker exec -it mysql-auth mysql -uauthuser -pauthpassword eadgequry-auth -e "
UPDATE users
SET password = '\$2a\$10\$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy'
WHERE email='test@example.com';
"
```

---

## Step 3: Clean Database and Recreate User

If the password hash looks wrong or corrupted, let's start fresh:

```bash
# Delete old user
docker exec -it mysql-auth mysql -uauthuser -pauthpassword eadgequry-auth -e "DELETE FROM users WHERE email='test@example.com';"
```

Now use the registration endpoint (Step 4) to create a fresh user.

---

## Step 4: Register New User (Recommended)

Instead of manually inserting, use the registration endpoint:

**Direct to Auth Service:**
```bash
curl -X POST http://localhost:8081/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test User",
    "email": "test@example.com",
    "password": "password123"
  }'
```

**Through API Gateway:**
```bash
curl -X POST http://localhost:8765/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test User",
    "email": "test@example.com",
    "password": "password123"
  }'
```

**Expected response:**
```json
{
  "id": 1,
  "name": "Test User",
  "email": "test@example.com",
  "provider": "local",
  "avatarUrl": null
}
```

**If you get "Email already registered"**, delete the old user first (see Step 3).

---

## Step 5: Try Login Again

```bash
curl -X POST http://localhost:8765/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123"
  }'
```

**Expected response:**
```json
{
  "token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzZWxmIiwiaWF0IjoxNzMw..."
}
```

---

## Step 6: Verify Token Works

```bash
# Copy the token from Step 5
TOKEN="eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9..."

# Test with token
curl http://localhost:8765/auth/health \
  -H "Authorization: Bearer $TOKEN"
```

**Expected response:**
```json
{
  "status": "UP",
  "service": "auth"
}
```

---

## Common Issues & Solutions

### Issue 1: Password Encoder Returns `false`

**Problem:** BCrypt encoder is not working

**Solution:**
1. Stop auth service (Ctrl+C)
2. Rebuild: `cd auth && ./mvnw clean package`
3. Restart: `./mvnw spring-boot:run`

---

### Issue 2: User Password is NULL

**Cause:** User was inserted without a password hash

**Solution:** Use the `/register` endpoint instead of manual SQL INSERT. The endpoint automatically hashes the password.

---

### Issue 3: Email Already Registered

**Solution:**
```bash
# Delete existing user
docker exec -it mysql-auth mysql -uauthuser -pauthpassword eadgequry-auth -e "
DELETE FROM users WHERE email='test@example.com';
"

# Register again
curl -X POST http://localhost:8765/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test User",
    "email": "test@example.com",
    "password": "password123"
  }'
```

---

### Issue 4: Still Getting "Invalid credentials"

**Debug Steps:**

1. **Check auth service logs** for detailed error messages

2. **Verify database connection:**
```bash
docker logs mysql-auth
```

3. **Test database access from auth service:**
```bash
# In auth service logs, you should see:
# "Successfully applied 1 migration"
# "HikariPool-1 - Start completed"
```

4. **Check if Hibernate query is executing:**
Look for this in auth service logs:
```sql
select u1_0.id, u1_0.email, u1_0.password ... from users u1_0 where u1_0.email=?
```

If you don't see this query, the user doesn't exist.

5. **Generate fresh password hash:**
```bash
cd auth
./mvnw -q exec:java -Dexec.mainClass="com.eadgequry.auth.util.PasswordHashGenerator" -Dexec.args="password123"
```

This will show you if the known hash is valid.

---

## Recommended Workflow

1. ✅ **Always use `/register` endpoint** to create users (don't use SQL INSERT)
2. ✅ **Test password encoder** first with `/test-password-encoder`
3. ✅ **Check database** to verify user exists
4. ✅ **Try login** with the registered credentials
5. ✅ **Verify token** works on protected endpoints

---

## All Available Endpoints

### Public Endpoints (No Token Required)

| Method | URL | Description |
|--------|-----|-------------|
| POST | `http://localhost:8765/auth/register` | Register new user |
| POST | `http://localhost:8765/auth/login` | Login with email/password |
| GET | `http://localhost:8765/auth/health` | Health check |
| GET | `http://localhost:8765/auth/test-password-encoder` | Test BCrypt encoder |
| GET | `http://localhost:8765/auth/.well-known/jwks.json` | Public keys |

### Protected Endpoints (Token Required)

Add header: `Authorization: Bearer YOUR_TOKEN`

| Method | URL | Description |
|--------|-----|-------------|
| GET | `http://localhost:8765/auth/actuator/info` | Service info |
| GET | `http://localhost:8765/auth/actuator/metrics` | Metrics |

---

## Example Complete Flow

```bash
# 1. Test password encoder
curl http://localhost:8081/test-password-encoder

# 2. Register a new user
curl -X POST http://localhost:8765/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Doe",
    "email": "john@example.com",
    "password": "mypassword123"
  }'

# 3. Login
curl -X POST http://localhost:8765/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@example.com",
    "password": "mypassword123"
  }'

# 4. Copy the token and use it
TOKEN="<paste-token-here>"

curl http://localhost:8765/auth/health \
  -H "Authorization: Bearer $TOKEN"
```

---

## Need More Help?

Check the full testing guide: [TESTING.md](./TESTING.md)

**Common mistake:** Don't manually insert users with SQL! Use the `/register` endpoint instead. It automatically:
- Validates email format
- Checks for duplicates
- Hashes the password correctly with BCrypt
- Sets timestamps
