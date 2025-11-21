/**
 * API Configuration and HTTP Client
 * Handles all API requests to the backend through API Gateway
 */

const API_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8765';
const AUTH_API = process.env.NEXT_PUBLIC_AUTH_API || '/auth';
const PROFILE_API = '/profiles';
const DATASOURCE_API = '/datasource';
const CHATBOT_API = '/chatbot';

// Flag to prevent multiple simultaneous logout redirects
let isLoggingOut = false;

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
  if (typeof document !== 'undefined') {
    document.cookie = `${name}=;expires=Thu, 01 Jan 1970 00:00:00 UTC;path=/;`;
  }
}

/**
 * Clear all authentication data from storage and cookies
 */
function clearAuthData() {
  if (typeof window === 'undefined') return;

  try {
    // Clear localStorage
    localStorage.removeItem('authToken');
    localStorage.removeItem('user');

    // Clear sessionStorage (in case anything is stored there)
    sessionStorage.removeItem('authToken');
    sessionStorage.removeItem('user');

    // Clear cookies
    deleteCookie('authToken');

    console.log('[Auth] Cleared all authentication data');
  } catch (error) {
    console.error('[Auth] Error clearing auth data:', error);
  }
}

/**
 * Export clearAuthData for use by AuthContext or other components
 */
export { clearAuthData };

/**
 * Handle 401 Unauthorized - token is invalid or expired
 * This is called automatically for ANY API response with 401 status
 */
function handle401Unauthorized(endpoint: string) {
  // Don't logout for public auth endpoints
  const publicEndpoints = ['/auth/login', '/auth/register', '/auth/forgot-password'];
  const isPublicEndpoint = publicEndpoints.some(path => endpoint.includes(path));

  if (isPublicEndpoint) {
    console.log('[Auth] 401 on public endpoint, skipping auto-logout');
    return;
  }

  // Prevent multiple simultaneous logouts
  if (isLoggingOut) {
    console.log('[Auth] Logout already in progress, skipping');
    return;
  }

  if (typeof window !== 'undefined') {
    isLoggingOut = true;

    console.warn('[Auth] 401 Unauthorized - Token invalid/expired. Auto-logout triggered.');
    console.warn('[Auth] Endpoint:', endpoint);

    // Clear all authentication data
    clearAuthData();

    // Redirect to login with session expired message
    window.location.href = '/login?reason=session_expired';
  }
}

/**
 * Generic API request handler with automatic 401 logout
 * ALL API requests go through this function, ensuring consistent 401 handling
 */
async function apiRequest<T>(
  endpoint: string,
  options: RequestInit = {}
): Promise<ApiResponse<T>> {
  try {
    const token = typeof window !== 'undefined' ? localStorage.getItem('authToken') : null;

    const headers: Record<string, string> = {
      'Content-Type': 'application/json',
    };

    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }

    // Merge with any additional headers from options
    if (options.headers) {
      Object.assign(headers, options.headers);
    }

    const response = await fetch(`${API_URL}${endpoint}`, {
      ...options,
      headers,
    });

    // Try to parse JSON response
    const data = await response.json().catch(() => ({}));

    if (!response.ok) {
      // CRITICAL: Handle 401 Unauthorized - AUTOMATIC LOGOUT
      if (response.status === 401) {
        handle401Unauthorized(endpoint);

        // Still return error so caller can handle if needed
        return {
          error: {
            message: 'Session expired. Please login again.',
            status: 401,
            error: 'Unauthorized',
          },
        };
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
    console.error('[API] Request failed:', endpoint, error);
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

// Datasource API endpoints
export const datasourceApi = {
  getAllConfigs: (userId: number) =>
    api.get<DatabaseConfigDTO[]>(`${DATASOURCE_API}/configs/user/${userId}`),

  getConfig: (id: number, userId: number) =>
    api.get<DatabaseConfigDTO>(`${DATASOURCE_API}/configs/${id}/user/${userId}`),

  createConfig: (userId: number, data: CreateDatabaseConfigRequest) =>
    api.post<DatabaseConfigDTO>(`${DATASOURCE_API}/configs/user/${userId}`, data),

  updateConfig: (id: number, userId: number, data: CreateDatabaseConfigRequest) =>
    api.put<DatabaseConfigDTO>(`${DATASOURCE_API}/configs/${id}/user/${userId}`, data),

  deleteConfig: (id: number, userId: number) =>
    api.delete<void>(`${DATASOURCE_API}/configs/${id}/user/${userId}`),

  testConnection: (id: number, userId: number) =>
    api.post<ConnectionTestResponse>(`${DATASOURCE_API}/configs/${id}/user/${userId}/test`, {}),

  getSchema: (configId: number, userId: number) =>
    api.get<DatabaseSchemaDTO>(`${DATASOURCE_API}/schemas/config/${configId}/user/${userId}`),
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

// Datasource types
export interface CreateDatabaseConfigRequest {
  name: string;
  type: string; // mysql, postgresql, oracle, sqlite, etc.

  // Common fields
  host?: string;
  port?: number;
  databaseName?: string;
  username?: string;
  password?: string;

  // Type-specific fields
  filePath?: string; // SQLite
  serviceName?: string; // Oracle
  sid?: string; // Oracle
  instanceName?: string; // SQL Server
  account?: string; // Snowflake
  warehouse?: string; // Snowflake
  schemaName?: string; // Snowflake
  role?: string; // Snowflake
  projectId?: string; // BigQuery
  dataset?: string; // BigQuery
  serviceAccountJson?: string; // BigQuery

  connectionProperties?: Record<string, any>;
}

export interface DatabaseConfigDTO {
  id: number;
  userId: number;
  name: string;
  type: string;

  // Common fields
  host?: string;
  port?: number;
  databaseName?: string;
  username?: string;
  // password not included for security

  // Type-specific fields
  filePath?: string;
  serviceName?: string;
  sid?: string;
  instanceName?: string;
  account?: string;
  warehouse?: string;
  schemaName?: string;
  role?: string;
  projectId?: string;
  dataset?: string;

  connectionProperties?: Record<string, any>;
  status?: string;
  isConnected?: boolean;
  lastConnectedAt?: string;
  createdAt: string;
  updatedAt: string;
}

export interface ConnectionTestResponse {
  success: boolean;
  message: string;
  exceptionType?: string;
  sqlState?: string;
  errorCode?: number;
}

export interface DatabaseSchemaDTO {
  id: number;
  databaseConfigId: number;
  databaseName: string;
  databaseType: string;
  tables: TableInfo[];
  extractedAt: string;
  createdAt: string;
  updatedAt: string;
}

export interface TableInfo {
  name: string;
  columns: ColumnInfo[];
  primaryKeys: string[];
  foreignKeys: ForeignKeyInfo[];
  indexes: string[];
  rowCount?: number;
}

export interface ColumnInfo {
  name: string;
  type: string;
  nullable: boolean;
  defaultValue?: string;
  maxLength?: number;
  precision?: number;
  scale?: number;
}

export interface ForeignKeyInfo {
  name: string;
  column: string;
  referencedTable: string;
  referencedColumn: string;
}

// Chatbot API endpoints
export const chatbotApi = {
  ask: (data: ChatRequest) =>
    api.post<ChatResponse>(`${CHATBOT_API}/ask`, data),

  getUserHistory: (userId: number) =>
    api.get<ConversationHistory[]>(`${CHATBOT_API}/history/user/${userId}`),

  getSessionHistory: (sessionId: string) =>
    api.get<ConversationHistory[]>(`${CHATBOT_API}/history/session/${sessionId}`),

  health: () =>
    api.get<{ message: string }>(`${CHATBOT_API}/health`),
};

// AI Settings API endpoints
export const aiSettingsApi = {
  getUserSettings: (userId: number) =>
    api.get<UserAiSettingsDTO>(`${CHATBOT_API}/ai-settings/user/${userId}`),

  updateUserSettings: (userId: number, data: UpdateAiSettingsRequest) =>
    api.put<UserAiSettingsDTO>(`${CHATBOT_API}/ai-settings/user/${userId}`, data),

  deleteUserSettings: (userId: number) =>
    api.delete<void>(`${CHATBOT_API}/ai-settings/user/${userId}`),

  getAvailableProviders: () =>
    api.get<AvailableProvidersResponse>(`${CHATBOT_API}/ai-settings/providers`),
};

// Chatbot types
export interface ChatRequest {
  question: string;
  databaseConfigId: number;
  userId: number;
}

export interface ChatResponse {
  success: boolean;
  question: string;
  sqlQuery?: string;
  sqlResult?: Array<Record<string, any>>;
  answer: string;
  error?: string;
}

export interface ConversationHistory {
  id: number;
  userId: number;
  databaseConfigId: number;
  sessionId: string;
  question: string;
  sqlQuery: string;
  sqlResult: Array<Record<string, any>>;
  answer: string;
  errorMessage?: string;
  createdAt: string;
}

// Helper function for streaming chatbot responses
export async function* streamChatbot(data: ChatRequest): AsyncGenerator<string> {
  const token = typeof window !== 'undefined' ? localStorage.getItem('authToken') : null;

  const response = await fetch(`${API_URL}${CHATBOT_API}/ask/stream`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      ...(token && { Authorization: `Bearer ${token}` }),
    },
    body: JSON.stringify(data),
  });

  if (!response.ok) {
    throw new Error(`HTTP error! status: ${response.status}`);
  }

  const reader = response.body?.getReader();
  const decoder = new TextDecoder();

  if (!reader) {
    throw new Error('Response body is not readable');
  }

  while (true) {
    const { done, value } = await reader.read();
    if (done) break;

    const chunk = decoder.decode(value, { stream: true });
    const lines = chunk.split('\n');

    for (const line of lines) {
      if (line.startsWith('data: ')) {
        const data = line.slice(6);
        if (data && data !== '[DONE]') {
          yield data;
        }
      }
    }
  }
}

// AI Settings types
export interface UserAiSettingsDTO {
  id?: number;
  userId: number;
  provider: string; // 'DEMO' | 'CLAUDE' | 'OPENAI'
  model: string;
  apiKey?: string;  // Never returned by API
  hasApiKey: boolean;
}

export interface UpdateAiSettingsRequest {
  provider: string;
  model: string;
  apiKey?: string;
}

export interface AvailableProvidersResponse {
  providers: AiProvider[];
}

export interface AiProvider {
  code: string;
  name: string;
  models: string[];
}
