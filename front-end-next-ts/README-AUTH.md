# Authentication System - Eadgequry AI Frontend

This document describes the authentication system implementation for the Next.js frontend that connects to the Auth microservice through the API Gateway.

## Architecture

```
Frontend (Next.js) → API Gateway (8765) → Auth Service (8081)
```

## Features

✅ User registration with email verification
✅ User login with JWT token
✅ Protected routes
✅ Persistent authentication (localStorage)
✅ Form validation with Zod
✅ Beautiful UI with Shadcn/UI components
✅ Toast notifications with Sonner
✅ Dark/Light theme support

## Components

### 1. **API Layer** (`lib/api.ts`)
- HTTP client configuration
- Auth endpoints (register, login)
- JWT token management
- Error handling

### 2. **Auth Context** (`contexts/AuthContext.tsx`)
- Global authentication state
- Login/Register/Logout functions
- User data management
- Token storage

### 3. **Pages**
- **Login** (`app/login/page.tsx`) - User sign in
- **Register** (`app/register/page.tsx`) - New user registration

### 4. **Protected Routes**
- `components/ProtectedRoute.tsx` - Wrapper for protected pages
- `middleware.ts` - Server-side route protection

## Setup Instructions

### 1. **Environment Variables**

Create a `.env.local` file (already created):

```bash
NEXT_PUBLIC_API_URL=http://localhost:8765
NEXT_PUBLIC_AUTH_API=/auth
```

### 2. **Install Dependencies**

```bash
cd front-end-next-ts
pnpm install
```

### 3. **Start the Development Server**

```bash
pnpm dev
```

The frontend will run on `http://localhost:3000`

### 4. **Start Backend Services**

Make sure all backend services are running:

```bash
# Start Eureka Server (8761)
cd service-registry
./mvnw spring-boot:run

# Start API Gateway (8765)
cd api-gatway
./mvnw spring-boot:run

# Start Auth Service (8081)
cd auth
./mvnw spring-boot:run
```

## Usage

### Register a New User

1. Navigate to `http://localhost:3000/register`
2. Fill in:
   - Full Name
   - Email
   - Password (min 8 characters)
   - Confirm Password
3. Click "Create account"
4. You'll be redirected to login after successful registration
5. Check your email for verification link

### Login

1. Navigate to `http://localhost:3000/login`
2. Enter your email and password
3. Click "Sign in"
4. You'll be redirected to the dashboard

### Protecting Pages

Wrap any page component with `ProtectedRoute`:

```tsx
import { ProtectedRoute } from '@/components/ProtectedRoute';

export default function DashboardPage() {
  return (
    <ProtectedRoute>
      <div>Your protected content</div>
    </ProtectedRoute>
  );
}
```

### Using Auth Context

```tsx
'use client';

import { useAuth } from '@/contexts/AuthContext';

export default function MyComponent() {
  const { user, isAuthenticated, logout } = useAuth();

  if (!isAuthenticated) {
    return <div>Please login</div>;
  }

  return (
    <div>
      <p>Welcome, {user?.email}</p>
      <button onClick={logout}>Logout</button>
    </div>
  );
}
```

## API Endpoints

### Register
```http
POST http://localhost:8765/auth/register
Content-Type: application/json

{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "password123"
}
```

### Login
```http
POST http://localhost:8765/auth/login
Content-Type: application/json

{
  "email": "john@example.com",
  "password": "password123"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIs...",
  "userId": 1,
  "email": "john@example.com"
}
```

## Token Storage

JWT tokens are stored in:
- `localStorage.authToken` - The JWT token
- `localStorage.user` - User data (userId, email)

## Form Validation

Both login and register forms use Zod for validation:

**Register:**
- Name: min 2 characters
- Email: valid email format
- Password: min 8 characters
- Passwords must match

**Login:**
- Email: valid email format
- Password: required

## Error Handling

All API errors are displayed as toast notifications using Sonner:
- Success messages (green)
- Error messages (red)
- Info messages (blue)

## Security Features

1. **JWT Token** - Stored in localStorage, sent in Authorization header
2. **Protected Routes** - Automatic redirect to login if not authenticated
3. **Form Validation** - Client-side validation before API calls
4. **HTTPS Ready** - Works with HTTPS in production
5. **Token Expiry** - Server validates token expiry

## Troubleshooting

### Issue: "Network error" on login/register

**Solution:** Make sure API Gateway is running on port 8765:
```bash
cd api-gatway
./mvnw spring-boot:run
```

### Issue: "Registration successful but can't login"

**Solution:** Check your email for verification link. Email must be verified before login.

### Issue: CORS errors

**Solution:** API Gateway should have CORS configured. Check `CorsConfig.java` in api-gatway.

## Future Enhancements

- [ ] Forgot password flow
- [ ] Email verification page
- [ ] OAuth2 login (Google, GitHub)
- [ ] Remember me checkbox
- [ ] Session timeout warning
- [ ] Refresh token implementation

## File Structure

```
front-end-next-ts/
├── app/
│   ├── login/
│   │   └── page.tsx          # Login page
│   ├── register/
│   │   └── page.tsx          # Register page
│   ├── layout.tsx
│   └── ThemeProvider.tsx     # Wraps AuthProvider
├── components/
│   ├── ProtectedRoute.tsx    # Protected route wrapper
│   └── ui/                   # Shadcn/UI components
├── contexts/
│   └── AuthContext.tsx       # Auth state management
├── lib/
│   ├── api.ts               # API client
│   └── utils.ts
├── middleware.ts            # Route protection middleware
└── .env.local              # Environment variables
```

## Support

For issues or questions, please contact the development team or open an issue in the repository.
