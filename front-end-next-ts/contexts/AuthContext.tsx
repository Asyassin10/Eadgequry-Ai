'use client';

import React, { createContext, useContext, useState, useEffect, ReactNode } from 'react';
import { useRouter } from 'next/navigation';
import { authApi, clearAuthData, type AuthResponse, type LoginRequest, type RegisterRequest } from '@/lib/api';
import { toast } from 'sonner';

interface User {
  userId: number;
  email: string;
}

interface AuthContextType {
  user: User | null;
  isLoading: boolean;
  isAuthenticated: boolean;
  login: (credentials: LoginRequest) => Promise<boolean>;
  register: (data: RegisterRequest) => Promise<boolean>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

// Helper function to set cookie
function setCookie(name: string, value: string, days: number = 7) {
  const expires = new Date();
  expires.setTime(expires.getTime() + days * 24 * 60 * 60 * 1000);
  document.cookie = `${name}=${value};expires=${expires.toUTCString()};path=/;SameSite=Lax`;
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const router = useRouter();

  // Check if user is already logged in on mount
  useEffect(() => {
    const token = localStorage.getItem('authToken');
    const userData = localStorage.getItem('user');

    if (token && userData) {
      try {
        setUser(JSON.parse(userData));
      } catch (error) {
        console.error('Failed to parse user data:', error);
        // Use centralized auth clearing on error
        clearAuthData();
      }
    }
    setIsLoading(false);
  }, []);

  const login = async (credentials: LoginRequest): Promise<boolean> => {
    try {
      setIsLoading(true);
      const response = await authApi.login(credentials);

      if (response.error) {
        toast.error(response.error.message || 'Login failed');
        return false;
      }

      if (response.data) {
        const { token, userId, email } = response.data;

        // Store token and user data in localStorage
        localStorage.setItem('authToken', token);
        // Store token in cookie for middleware
        setCookie('authToken', token, 7);
        localStorage.setItem('user', JSON.stringify({ userId, email }));

        setUser({ userId, email });

        toast.success('Login successful!');
        router.push('/dashboard'); // Redirect to dashboard
        return true;
      }

      return false;
    } catch (error) {
      console.error('Login error:', error);
      toast.error('An unexpected error occurred');
      return false;
    } finally {
      setIsLoading(false);
    }
  };

  const register = async (data: RegisterRequest): Promise<boolean> => {
    try {
      setIsLoading(true);
      const response = await authApi.register(data);

      if (response.error) {
        toast.error(response.error.message || 'Registration failed');
        return false;
      }

      if (response.data) {
        toast.success('Registration successful! Please check your email to verify your account.');
        router.push('/login');
        return true;
      }

      return false;
    } catch (error) {
      console.error('Registration error:', error);
      toast.error('An unexpected error occurred');
      return false;
    } finally {
      setIsLoading(false);
    }
  };

  const logout = async () => {
    try {
      // Call backend logout endpoint (for logging/auditing)
      await authApi.logout();
    } catch (error) {
      // Continue with logout even if backend call fails
      console.error('Logout API call failed:', error);
    } finally {
      // Use centralized auth data clearing (clears localStorage, sessionStorage, and cookies)
      clearAuthData();
      setUser(null);
      toast.success('Logged out successfully');
      router.push('/login');
    }
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        isLoading,
        isAuthenticated: !!user,
        login,
        register,
        logout,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}
