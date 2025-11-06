/**
 * API Configuration and HTTP Client
 * Handles all API requests to the backend through API Gateway
 */

const API_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8765';
const AUTH_API = process.env.NEXT_PUBLIC_AUTH_API || '/auth';
const PROFILE_API = '/profiles';

export interface ApiError {
  message: string;
  status: number;
  error?: string;
}

export interface ApiResponse<T> {
  data?: T;
  error?: ApiError;
}

/**
 * Helper function to delete cookie
 */
function deleteCookie(name: string) {
  document.cookie = `${name}=;expires=Thu, 01 Jan 1970 00:00:00 UTC;path=/;`;
}

/**
 * Handle 401 Unauthorized - token is invalid or expired
 */
function handle401Unauthorized(endpoint: string) {
  // Don't logout for public auth endpoints
  const publicEndpoints = ['/auth/login', '/auth/register', '/auth/forgot-password'];
  const isPublicEndpoint = publicEndpoints.some(path => endpoint.includes(path));

  if (!isPublicEndpoint && typeof window !== 'undefined') {
    // Clear authentication data
    localStorage.removeItem('authToken');
    localStorage.removeItem('user');
    deleteCookie('authToken');

    // Redirect to login with message
    window.location.href = '/login?reason=session_expired';
  }
}

/**
 * Generic API request handler with error handling
 */
async function apiRequest<T>(
  endpoint: string,
  options: RequestInit = {}
): Promise<ApiResponse<T>> {
  try {
    const token = typeof window !== 'undefined' ? localStorage.getItem('authToken') : null;

    const headers: HeadersInit = {
      'Content-Type': 'application/json',
      ...options.headers,
    };

    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }

    const response = await fetch(`${API_URL}${endpoint}`, {
      ...options,
      headers,
    });

    const data = await response.json().catch(() => ({}));

    if (!response.ok) {
      // Handle 401 Unauthorized - invalid/expired token
      if (response.status === 401) {
        handle401Unauthorized(endpoint);
      }

      return {
        error: {
          message: data.message || data.error || 'An error occurred',
          status: response.status,
          error: data.error,
        },
      };
    }

    return { data };
  } catch (error) {
    return {
      error: {
        message: error instanceof Error ? error.message : 'Network error',
        status: 0,
      },
    };
  }
}

export const api = {
  get: <T>(endpoint: string) => apiRequest<T>(endpoint, { method: 'GET' }),

  post: <T>(endpoint: string, body: any) =>
    apiRequest<T>(endpoint, {
      method: 'POST',
      body: JSON.stringify(body),
    }),

  put: <T>(endpoint: string, body: any) =>
    apiRequest<T>(endpoint, {
      method: 'PUT',
      body: JSON.stringify(body),
    }),

  delete: <T>(endpoint: string) =>
    apiRequest<T>(endpoint, { method: 'DELETE' }),
};

// Auth API endpoints
export const authApi = {
  register: (data: RegisterRequest) =>
    api.post<AuthResponse>(`${AUTH_API}/register`, data),

  login: (data: LoginRequest) =>
    api.post<AuthResponse>(`${AUTH_API}/login`, data),

  logout: () =>
    api.post<MessageResponse>(`${AUTH_API}/logout`, {}),

  forgotPassword: (data: ForgotPasswordRequest) =>
    api.post<MessageResponse>(`${AUTH_API}/forgot-password`, data),

  updatePassword: (data: UpdatePasswordRequest) =>
    api.put<MessageResponse>(`${AUTH_API}/users/password`, data),

  updateEmail: (data: UpdateEmailRequest) =>
    api.put<MessageResponse>(`${AUTH_API}/users/email`, data),

  health: () =>
    api.get<HealthResponse>(`${AUTH_API}/health`),
};

// Profile API endpoints
export const profileApi = {
  getProfile: (userId: number) =>
    api.get<ProfileResponse>(`${PROFILE_API}/${userId}`),

  updateProfile: (userId: number, data: UpdateProfileRequest) =>
    api.put<ProfileResponse>(`${PROFILE_API}/${userId}`, data),

  deleteProfile: (userId: number) =>
    api.delete<MessageResponse>(`${PROFILE_API}/${userId}`),
};

// Type definitions
export interface RegisterRequest {
  name: string;
  email: string;
  password: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface ForgotPasswordRequest {
  email: string;
}

export interface UpdatePasswordRequest {
  currentPassword: string;
  newPassword: string;
}

export interface UpdateEmailRequest {
  newEmail: string;
  password: string;
}

export interface UpdateProfileRequest {
  name?: string;
  avatarUrl?: string;
  bio?: string;
  preferences?: string;
}

export interface AuthResponse {
  token: string;
  userId: number;
  email: string;
  message?: string;
}

export interface MessageResponse {
  message: string;
}

export interface HealthResponse {
  status: string;
  service: string;
}

export interface ProfileResponse {
  id: number;
  userId: number;
  name: string;
  avatarUrl: string | null;
  bio: string | null;
  preferences: string | null;
  createdAt: string;
  updatedAt: string;
}
