# Authentication Flow

## LOGIN REQUEST

```mermaid
sequenceDiagram
    participant Client
    participant Gateway
    participant Auth
    participant MySQL

    Client->>Gateway: POST /auth/login {email, password}
    Gateway->>Auth: Route to Auth Service
    Auth->>MySQL: Check DB
    MySQL-->>Auth: User found
    Auth-->>Auth: Generate JWT
    Auth-->>Gateway: Return token {"token":"eyJ..."}
    Gateway-->>Client: Return token {"token":"eyJ..."}
```
## PROTECTED REQUEST
```mermaid
sequenceDiagram
    participant Client
    participant Gateway
    participant Auth

    Client->>Gateway: GET /auth/health\nAuthorization: Bearer eyJ...
    Gateway->>Auth: Validate JWT (get public key)
    Auth-->>Gateway: Return public key
    Gateway-->>Gateway: Verify JWT signature
    Gateway->>Auth: Forward request
    Auth-->>Gateway: Process request
    Gateway-->>Client: Return response {"status":"UP"}
```

📚 Summary
Login = Get Token

Client sends email/password to Gateway

Auth Service checks database

Password verified with BCrypt

JWT is created and signed with Private Key

Token is returned to client

Using Token = Access Protected Resources

Client sends request with Authorization: Bearer <token>

Gateway fetches Public Key from Auth Service

Gateway verifies JWT signature and expiration

If valid, request is forwarded to Auth Service

Response is returned

Why it's secure

Token is signed → cannot be forged

Token expires → cannot be reused forever

Stateless → no session storage needed

Distributed → any service can verify

✅ Your system is working perfectly! 🎉
