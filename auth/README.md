─────────┐  
│ Step 1 │ Client sends POST request to API Gateway  
└────┬────┘  
 │
│ POST /auth/login
│ {email, password}
▼
┌─────────────────────────────────────────────────────────────┐
│ API Gateway (8765) │
│ SecurityConfig.java - Check if endpoint is public │
└────┬────────────────────────────────────────────────────────┘
│
│ ✅ /auth/login is in permitAll() list
│ ✅ No JWT token required for login
│
┌────▼────┐
│ Step 2 │ Gateway routes to Auth Service
└────┬────┘
│
│ Gateway removes /auth prefix (StripPrefix=1)
│ So /auth/login → /login
│  
 │ Gateway asks Eureka: "Where is 'auth' service?"
│ Eureka responds: "localhost:8081"
│ Gateway forwards to: http://localhost:8081/login
│
▼
┌─────────────────────────────────────────────────────────────┐
│ Auth Service (8081) │
│ JwtAuthenticationResource.java │
└────┬────────────────────────────────────────────────────────┘
│
│ @PostMapping("/login")
│ public ResponseEntity<?> authenticate(...)
│
┌────▼────┐
│ Step 3 │ Extract email and password from request
└────┬────┘
│
│ LoginRequest loginRequest = {
│ email: "test@example.com",
│ password: "password123" (plain text)
│ }
│
┌────▼────┐
│ Step 4 │ Create Authentication Token
└────┬────┘
│
│ UsernamePasswordAuthenticationToken token =
│ new UsernamePasswordAuthenticationToken(
│ "test@example.com",
│ "password123"
│ )
│
┌────▼────┐
│ Step 5 │ AuthenticationManager.authenticate()
└────┬────┘
│
│ This triggers the authentication process...
│
▼
┌─────────────────────────────────────────────────────────────┐
│ CustomUserDetailsService.java │
│ loadUserByUsername("test@example.com") │
└────┬────────────────────────────────────────────────────────┘
│
┌────▼────┐
│ Step 6 │ Query Database for User
└────┬────┘
│
│ SELECT \* FROM users WHERE email = 'test@example.com'
│
▼
┌─────────────────────────────────────────────────────────────┐
│ MySQL Database │
│ Returns User: │
│ id: 1 │
│ email: test@example.com │
│ password: $2a$10$N9qo8uLOickgx2ZMRZo... (BCrypt hash) │
│ name: Test User │
└────┬────────────────────────────────────────────────────────┘
│
┌────▼────┐
│ Step 7 │ Wrap User in UserDetails object
└────┬────┘
│
│ CustomUserDetails userDetails = new CustomUserDetails(user)
│ Returns to AuthenticationManager
│
┌────▼────┐
│ Step 8 │ Verify Password
└────┬────┘
│
│ passwordEncoder.matches(
│ "password123", (what user typed)
│ "$2a$10$N9qo8uLOickgx2ZMRZo..." (from database)
│ )
│  
 │ ✅ BCrypt compares: MATCH!
│ ❌ If no match: throw BadCredentialsException → "Invalid credentials"
│
┌────▼────┐
│ Step 9 │ Authentication Successful! Create JWT Token
└────┬────┘
│
│ Back to JwtAuthenticationResource.createToken()
│
▼
┌─────────────────────────────────────────────────────────────┐
│ Create JWT Token (RSA Signing) │
│ │
│ JwtClaimsSet claims = { │
│ "iss": "self", (issuer) │
│ "sub": "test@example.com", (subject/user) │
│ "iat": 1762186937, (issued at timestamp) │
│ "exp": 1762188737, (expires in 30 min) │
│ "scope": "USER" (authorities) │
│ } │
│ │
│ Sign with RSA Private Key → Generate Token │
└────┬────────────────────────────────────────────────────────┘
│
┌────▼────┐
│ Step 10 │ Return JWT Token to Client
└────┬────┘
│
│ Response: { "token": "eyJraWQiOiI5YzUxY2FhMC04M..." }
│
▼
┌─────────────────────────────────────────────────────────────┐
│ Client receives JWT token │
│ Store this token! You'll need it for future requests │
└─────────────────────────────────────────────────────────────┘

┌────────┐ 1. GET /auth/health ┌─────────┐
│ Client │──────────────────────────────>│ Gateway │
│ │ Authorization: Bearer eyJ... │ │
└────────┘ └────┬────┘
▲ │
│ │ 2. Validate JWT
│ │ (get public key)
│ ┌────▼────┐
│ │ Auth │
│ 3. Get JWKS │ Service │
│ ┌───────────────│ │
│ │ └─────────┘
│ └──────────────>└─────────┘
│ 4. Return  
 │ public key  
 │ ┌─────────┐
│ │ Gateway │
│ 5. Verify JWT │ │
│ signature OK │ │
│ └────┬────┘
│ │
│ │ 6. Forward request
│ │
│ ┌────▼────┐
│ │ Auth │
│ 7. Process │ Service │
│ request │ │
│ └────┬────┘
│ │
│ ┌────▼────┐
│ │ Gateway │
│ 8. Return response │ │
│<───────────────────────────────────┤ │
│ {"status":"UP"} └─────────┘
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

If you want, I can also make a more visual version using Mermaid diagrams that GitHub can render directly in the README. It looks cleaner than ASCII diagrams.

Do you want me to do that?
