# Datasource Service Workflow Documentation

## Overview

The Datasource Service manages database connections, tests connectivity, and automatically extracts database schemas. This document provides a comprehensive workflow and architecture guide.

---

## Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────────────┐
│                            API Gateway (Port 8765)                       │
│                         JWT Authentication Layer                         │
└────────────────────────┬───────────────────────────────────────────────┘
                         │
                         │ Forward to: data-source service
                         ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                    Datasource Service (Port 8085)                        │
│  ┌───────────────────────────────────────────────────────────────────┐  │
│  │                  DatabaseConfigController                          │  │
│  │  - REST API endpoints                                             │  │
│  │  - Request validation                                             │  │
│  │  - Response formatting                                            │  │
│  └────────────────────────┬──────────────────────────────────────────┘  │
│                           │                                              │
│                           ▼                                              │
│  ┌───────────────────────────────────────────────────────────────────┐  │
│  │              DatabaseConfigService (Business Logic)                │  │
│  │                                                                    │  │
│  │  [1] Connection Test → [2] Save Config → [3] Extract Schema      │  │
│  └────┬───────────────────┬──────────────────────┬──────────────────┘  │
│       │                   │                      │                      │
│       ▼                   ▼                      ▼                      │
│  ┌─────────┐      ┌──────────────┐      ┌──────────────────┐          │
│  │Connection│      │   Database   │      │  Schema          │          │
│  │Test      │      │   Config     │      │  Extraction      │          │
│  │Service   │      │   Repository │      │  Service         │          │
│  └─────────┘      └──────────────┘      └──────────────────┘          │
│       │                   │                      │                      │
│       ▼                   ▼                      ▼                      │
│  ┌─────────┐      ┌──────────────────────────────────────┐            │
│  │  JDBC    │      │        MySQL Database                 │            │
│  │Connection│      │   - database_config table            │            │
│  │  Test    │      │   - database_schema table            │            │
│  └─────────┘      │   (CASCADE DELETE/UPDATE)            │            │
│                   └──────────────────────────────────────┘            │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## Workflow Diagrams

### 1. Create Database Configuration (Happy Path)

```
┌──────┐                                                    ┌────────────┐
│Client│                                                    │  Datasource│
│      │                                                    │   Service  │
└───┬──┘                                                    └─────┬──────┘
    │                                                              │
    │  POST /datasource/configs/user/{userId}                     │
    │  {                                                           │
    │    "name": "Production DB",                                 │
    │    "type": "mysql",                                         │
    │    "host": "db.example.com",                               │
    │    "port": 3306,                                            │
    │    "databaseName": "prod_db",                              │
    │    "username": "dbuser",                                    │
    │    "password": "***"                                        │
    │  }                                                           │
    ├───────────────────────────────────────────────────────────▶│
    │                                                              │
    │                    [1] VALIDATE REQUEST                     │
    │                                                              ├──┐
    │                                                              │  │
    │                                                              │◀─┘
    │                                                              │
    │                    [2] TEST CONNECTION                       │
    │                       ┌──────────────────┐                  │
    │                       │ Build JDBC URL   │                  ├──┐
    │                       │ jdbc:mysql://... │                  │  │
    │                       └──────────────────┘                  │  │
    │                       ┌──────────────────┐                  │  │
    │                       │ Connect to DB    │                  │  │
    │                       │ Test isValid(5)  │                  │  │
    │                       └──────────────────┘                  │  │
    │                       ┌──────────────────┐                  │  │
    │                       │ ✓ Success!       │                  │◀─┘
    │                       └──────────────────┘                  │
    │                                                              │
    │                    [3] SAVE CONFIGURATION                    │
    │                       ┌──────────────────┐                  │
    │                       │ Create entity    │                  ├──┐
    │                       │ Set status:      │                  │  │
    │                       │   active         │                  │  │
    │                       │ Set isConnected: │                  │  │
    │                       │   true           │                  │  │
    │                       │ Save to database │                  │  │
    │                       │ ID = 1           │                  │◀─┘
    │                       └──────────────────┘                  │
    │                                                              │
    │                    [4] EXTRACT SCHEMA                        │
    │                       ┌──────────────────┐                  │
    │                       │ Connect to DB    │                  ├──┐
    │                       │ Get metadata     │                  │  │
    │                       └──────────────────┘                  │  │
    │                       ┌──────────────────┐                  │  │
    │                       │ Extract Tables:  │                  │  │
    │                       │ - users          │                  │  │
    │                       │ - orders         │                  │  │
    │                       │ - products       │                  │  │
    │                       └──────────────────┘                  │  │
    │                       ┌──────────────────┐                  │  │
    │                       │ Extract Columns: │                  │  │
    │                       │ - names, types   │                  │  │
    │                       │ - constraints    │                  │  │
    │                       └──────────────────┘                  │  │
    │                       ┌──────────────────┐                  │  │
    │                       │ Extract Relations│                  │  │
    │                       │ - FK constraints │                  │  │
    │                       │ - PK constraints │                  │  │
    │                       │ - Indexes        │                  │  │
    │                       └──────────────────┘                  │  │
    │                       ┌──────────────────┐                  │  │
    │                       │ Save as JSON     │                  │  │
    │                       │ to schema table  │                  │◀─┘
    │                       └──────────────────┘                  │
    │                                                              │
    │  ◀── 201 CREATED                                            │
    │  {                                                           │
    │    "id": 1,                                                  │
    │    "userId": 100,                                            │
    │    "name": "Production DB",                                 │
    │    "type": "mysql",                                         │
    │    "status": "active",                                      │
    │    "isConnected": true,                                     │
    │    "createdAt": "2025-11-07T12:00:00",                     │
    │    ...                                                       │
    │  }                                                           │
    │◀─────────────────────────────────────────────────────────────│
    │                                                              │
```

### 2. Create Database Configuration (Connection Fails)

```
┌──────┐                                    ┌────────────┐
│Client│                                    │  Datasource│
│      │                                    │   Service  │
└───┬──┘                                    └─────┬──────┘
    │                                              │
    │  POST /datasource/configs/user/{userId}     │
    │  { invalid credentials }                    │
    ├──────────────────────────────────────────▶ │
    │                                              │
    │           [1] VALIDATE REQUEST              │
    │                                              ├──┐
    │                                              │  │
    │                                              │◀─┘
    │                                              │
    │           [2] TEST CONNECTION                │
    │              ┌──────────────────┐            │
    │              │ Build JDBC URL   │            ├──┐
    │              └──────────────────┘            │  │
    │              ┌──────────────────┐            │  │
    │              │ Try to connect   │            │  │
    │              └──────────────────┘            │  │
    │              ┌──────────────────┐            │  │
    │              │ ✗ FAIL!          │            │  │
    │              │ Access denied    │            │◀─┘
    │              └──────────────────┘            │
    │                                              │
    │           [3] THROW EXCEPTION                │
    │              DatabaseConnectionFailedException
    │                                              │
    │  ◀── 400 BAD REQUEST                        │
    │  {                                           │
    │    "status": 400,                            │
    │    "error": "Connection Test Failed",       │
    │    "message": "Access denied. Please check  │
    │                username and password.",     │
    │    "exceptionType": "SQLException",         │
    │    "sqlState": "28000",                     │
    │    "errorCode": 1045                        │
    │  }                                           │
    │◀─────────────────────────────────────────────│
    │                                              │
    │  ⚠ NO DATABASE CONFIG SAVED                │
    │  ⚠ NO SCHEMA EXTRACTED                     │
    │                                              │
```

### 3. Update Database Configuration

```
┌──────┐                                    ┌────────────┐
│Client│                                    │  Datasource│
│      │                                    │   Service  │
└───┬──┘                                    └─────┬──────┘
    │                                              │
    │  PUT /datasource/configs/1/user/100         │
    │  { updated details }                        │
    ├──────────────────────────────────────────▶ │
    │                                              │
    │           [1] FIND EXISTING CONFIG          │
    │                                              ├──┐
    │                                              │  │ Query DB
    │                                              │◀─┘
    │                                              │
    │           [2] UPDATE CONFIG                  │
    │                                              ├──┐
    │                                              │  │ Save changes
    │                                              │◀─┘
    │                                              │
    │           [3] RE-EXTRACT SCHEMA              │
    │              ┌──────────────────┐            │
    │              │ Connect to DB    │            ├──┐
    │              │ Extract metadata │            │  │
    │              │ Update JSON      │            │  │
    │              │ Save to schema   │            │◀─┘
    │              │ table (UPDATE)   │            │
    │              └──────────────────┘            │
    │                                              │
    │  ◀── 200 OK                                 │
    │  { updated config }                         │
    │◀─────────────────────────────────────────────│
    │                                              │
```

### 4. Delete Database Configuration (Cascade Delete)

```
┌──────┐                                    ┌────────────┐
│Client│                                    │  Datasource│
│      │                                    │   Service  │
└───┬──┘                                    └─────┬──────┘
    │                                              │
    │  DELETE /datasource/configs/1/user/100      │
    ├──────────────────────────────────────────▶ │
    │                                              │
    │           [1] VERIFY EXISTS                  │
    │                                              ├──┐
    │                                              │  │
    │                                              │◀─┘
    │                                              │
    │           [2] DELETE FROM DB                 │
    │              ┌──────────────────┐            │
    │              │ DELETE FROM      │            ├──┐
    │              │ database_config  │            │  │
    │              │ WHERE id = 1     │            │  │
    │              └──────────────────┘            │  │
    │              ┌──────────────────┐            │  │
    │              │ CASCADE DELETE:  │            │  │
    │              │ database_schema  │            │  │
    │              │ records also     │            │  │
    │              │ deleted!         │            │◀─┘
    │              └──────────────────┘            │
    │                                              │
    │  ◀── 204 NO CONTENT                         │
    │◀─────────────────────────────────────────────│
    │                                              │
    │  ✓ Config deleted                            │
    │  ✓ Schema automatically deleted              │
    │                                              │
```

### 5. Test Connection (Existing Configuration)

```
┌──────┐                                    ┌────────────┐
│Client│                                    │  Datasource│
│      │                                    │   Service  │
└───┬──┘                                    └─────┬──────┘
    │                                              │
    │  POST /datasource/configs/1/user/100/test   │
    ├──────────────────────────────────────────▶ │
    │                                              │
    │           [1] FIND CONFIG                    │
    │                                              ├──┐
    │                                              │  │ Load from DB
    │                                              │◀─┘
    │                                              │
    │           [2] TEST CONNECTION                 │
    │              ┌──────────────────┐            │
    │              │ Build JDBC URL   │            ├──┐
    │              │ Connect to DB    │            │  │
    │              │ Test validity    │            │◀─┘
    │              └──────────────────┘            │
    │                                              │
    │           [3] UPDATE STATUS                  │
    │              ┌──────────────────┐            │
    │              │ Set isConnected  │            ├──┐
    │              │ Set status       │            │  │
    │              │ Set lastConnected│            │◀─┘
    │              └──────────────────┘            │
    │                                              │
    │  ◀── 200 OK                                 │
    │  {                                           │
    │    "success": true,                          │
    │    "message": "Connection successful!        │
    │                Connected to prod_db"        │
    │  }                                           │
    │◀─────────────────────────────────────────────│
    │                                              │
```

---

## Database Schema

### Table: `database_config`

```sql
CREATE TABLE database_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL,

    -- Connection details
    host VARCHAR(255),
    port INT,
    database_name VARCHAR(255),
    username VARCHAR(255),
    password VARCHAR(255),

    -- Status tracking
    status VARCHAR(20) DEFAULT 'active',
    is_connected BOOLEAN DEFAULT FALSE,
    last_connected_at TIMESTAMP,

    -- Timestamps
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,

    INDEX idx_user_id (user_id),
    INDEX idx_type (type),
    INDEX idx_status (status)
);
```

### Table: `database_schema`

```sql
CREATE TABLE database_schema (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    database_config_id BIGINT NOT NULL,
    schema_json LONGTEXT NOT NULL,
    extracted_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,

    FOREIGN KEY (database_config_id)
        REFERENCES database_config(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    INDEX idx_database_config_id (database_config_id)
);
```

**Cascade Behavior:**
- When `database_config` is **deleted** → `database_schema` is automatically **deleted**
- When `database_config` is **updated** → `database_schema` relationship is maintained

---

## API Endpoints

### 1. Get All Configurations
```
GET /datasource/configs/user/{userId}

Response: 200 OK
[
  {
    "id": 1,
    "userId": 100,
    "name": "Production DB",
    "type": "mysql",
    "host": "db.example.com",
    "port": 3306,
    "databaseName": "prod_db",
    "username": "dbuser",
    "status": "active",
    "isConnected": true,
    "lastConnectedAt": "2025-11-07T12:00:00",
    "createdAt": "2025-11-07T10:00:00",
    "updatedAt": "2025-11-07T12:00:00"
  }
]
```

### 2. Get Configuration by ID
```
GET /datasource/configs/{id}/user/{userId}

Response: 200 OK
{
  "id": 1,
  "userId": 100,
  ...
}
```

### 3. Create Configuration
```
POST /datasource/configs/user/{userId}

Request Body:
{
  "name": "Production DB",
  "type": "mysql",
  "host": "db.example.com",
  "port": 3306,
  "databaseName": "prod_db",
  "username": "dbuser",
  "password": "securepass"
}

Response: 201 CREATED
{
  "id": 1,
  "userId": 100,
  "name": "Production DB",
  ...
  "status": "active",
  "isConnected": true
}
```

### 4. Update Configuration
```
PUT /datasource/configs/{id}/user/{userId}

Request Body:
{
  "name": "Production DB Updated",
  "type": "mysql",
  ...
}

Response: 200 OK
{
  "id": 1,
  ...
}
```

### 5. Delete Configuration
```
DELETE /datasource/configs/{id}/user/{userId}

Response: 204 NO CONTENT
```

### 6. Test Connection
```
POST /datasource/configs/{id}/user/{userId}/test

Response: 200 OK
{
  "success": true,
  "message": "Connection successful! Connected to prod_db"
}

OR (on failure):

{
  "success": false,
  "message": "Access denied. Please check username and password.",
  "exceptionType": "SQLException",
  "sqlState": "28000",
  "errorCode": 1045
}
```

---

## Supported Database Types

| Database Type | Port  | Status       |
|---------------|-------|--------------|
| MySQL         | 3306  | ✅ Supported |
| PostgreSQL    | 5432  | ✅ Supported |
| SQL Server    | 1433  | ✅ Supported |
| Oracle        | 1521  | ✅ Supported |
| H2            | 9092  | ✅ Supported |

---

## Schema Extraction Details

### What is Extracted:

1. **Tables**
   - Table name
   - Table type (TABLE, VIEW, etc.)
   - Table comments/remarks

2. **Columns** (for each table)
   - Column name
   - Data type (VARCHAR, INT, BIGINT, etc.)
   - Column size
   - Nullable constraint
   - Default value
   - Ordinal position
   - Column comments

3. **Primary Keys**
   - List of primary key columns per table

4. **Foreign Keys (Relationships)**
   - Foreign key name
   - Source column
   - Referenced table
   - Referenced column
   - Update rule (CASCADE, SET NULL, RESTRICT, etc.)
   - Delete rule (CASCADE, SET NULL, RESTRICT, etc.)

5. **Indexes**
   - Index name
   - Indexed columns
   - Unique/non-unique flag
   - Ordinal position

### Example Extracted Schema JSON:

```json
{
  "databaseName": "prod_db",
  "databaseType": "mysql",
  "extractedAt": "2025-11-07T12:00:00.000",
  "tables": [
    {
      "name": "users",
      "type": "TABLE",
      "remarks": "User accounts",
      "columns": [
        {
          "name": "id",
          "type": "BIGINT",
          "size": 19,
          "nullable": false,
          "ordinalPosition": 1
        },
        {
          "name": "email",
          "type": "VARCHAR",
          "size": 255,
          "nullable": false,
          "ordinalPosition": 2
        },
        {
          "name": "role_id",
          "type": "BIGINT",
          "size": 19,
          "nullable": true,
          "ordinalPosition": 3
        }
      ],
      "primaryKeys": ["id"],
      "foreignKeys": [
        {
          "name": "fk_user_role",
          "column": "role_id",
          "referencedTable": "roles",
          "referencedColumn": "id",
          "updateRule": "CASCADE",
          "deleteRule": "SET NULL"
        }
      ],
      "indexes": [
        {
          "name": "idx_email",
          "column": "email",
          "unique": true,
          "ordinalPosition": 1
        }
      ]
    }
  ]
}
```

---

## Error Handling

### Connection Test Failures

The service provides user-friendly error messages:

| Error Scenario | User Message |
|----------------|--------------|
| Wrong credentials | "Access denied. Please check username and password." |
| Database doesn't exist | "Database 'dbname' does not exist." |
| Wrong host/port | "Cannot connect to database server. Please check host and port." |
| Connection timeout | "Connection timeout. Database server might be unreachable." |
| Missing driver | "Database driver not found. Please contact administrator." |

### HTTP Status Codes

| Status Code | Meaning |
|-------------|---------|
| 200 OK | Request successful |
| 201 CREATED | Configuration created successfully |
| 204 NO CONTENT | Configuration deleted successfully |
| 400 BAD REQUEST | Invalid request or connection test failed |
| 404 NOT FOUND | Configuration not found |
| 500 INTERNAL SERVER ERROR | Unexpected server error |

---

## Testing

### Unit Tests

The service includes comprehensive unit tests:

1. **DatabaseConfigServiceTest** (12 tests)
   - Get all configs by user
   - Get config by ID (exists/not exists)
   - Create config (success/connection fail/schema fail)
   - Update config (success/not exists)
   - Delete config (success/not exists)
   - Update connection status
   - Test existing connection

2. **DatabaseConnectionTestServiceTest** (8 tests)
   - Connection test with invalid host
   - Unsupported database type
   - Connection test from config
   - Result object properties
   - JDBC URL building for different databases

3. **DatabaseConfigControllerTest** (7 tests)
   - Get all configs
   - Get config by ID
   - Create config
   - Update config
   - Delete config
   - Test connection (success/failure)

### Code Coverage Target

- Minimum: 80% line coverage
- Target: 90%+ line coverage
- Excludes: DTOs, models, exceptions, config classes

---

## Security

### JWT Authentication

All endpoints require JWT authentication through the API Gateway:

```
Authorization: Bearer <JWT_TOKEN>
```

### Password Handling

- Passwords are **stored in plaintext** in database_config (for connecting to target databases)
- Passwords are **NOT returned** in API responses (DatabaseConfigDTO)
- ⚠️ **Important**: In production, consider encrypting passwords at rest

---

## Swagger/OpenAPI Documentation

Access the interactive API documentation:

```
http://localhost:8085/swagger-ui.html
```

Or through API Gateway:

```
http://localhost:8765/datasource/swagger-ui.html
```

---

## Quick Start Guide

### 1. Start the Service

```bash
cd data-source
mvn spring-boot:run
```

### 2. Register with Eureka

The service automatically registers at: `http://localhost:8761`

### 3. Create a Database Configuration

```bash
curl -X POST http://localhost:8765/datasource/configs/user/1 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "My Database",
    "type": "mysql",
    "host": "localhost",
    "port": 3306,
    "databaseName": "mydb",
    "username": "root",
    "password": "password"
  }'
```

### 4. View Extracted Schema

Check the `database_schema` table in the datasource service database.

---

## Maintenance

### Database Migrations

Managed by Flyway:
- Migration files: `src/main/resources/db/migration/`
- Run on application startup
- Version controlled

### Logs

Key log messages:
- `Extracting schema for database config ID: {id}` - Schema extraction started
- `Schema extraction successful for database config ID: {id}` - Schema extracted
- `Schema extraction failed for database config ID: {id}` - Schema extraction error

---

## Future Enhancements

1. **Password Encryption**: Encrypt stored database passwords
2. **Connection Pooling**: Reuse connections for repeated operations
3. **Async Schema Extraction**: Extract schema in background thread
4. **Schema Diff**: Compare schema changes over time
5. **Query Execution**: Execute queries against saved configurations
6. **Multi-Database Support**: Expand support for more database types

---

## Support

For issues or questions:
- Check logs: `data-source/logs/`
- Review Swagger docs: `http://localhost:8085/swagger-ui.html`
- Check Eureka dashboard: `http://localhost:8761`

---

**Document Version**: 1.0
**Last Updated**: 2025-11-07
**Service Version**: 0.0.1-SNAPSHOT
