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
