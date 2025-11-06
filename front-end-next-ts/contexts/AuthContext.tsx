'use client';

import React, { createContext, useContext, useState, useEffect, ReactNode } from 'react';
import { useRouter } from 'next/navigation';
import { authApi, type AuthResponse, type LoginRequest, type RegisterRequest } from '@/lib/api';
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
        localStorage.removeItem('authToken');
        localStorage.removeItem('user');
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

        // Store token and user data
        localStorage.setItem('authToken', token);
        localStorage.setItem('user', JSON.stringify({ userId, email }));

        setUser({ userId, email });

        toast.success('Login successful!');
        router.push('/'); // Redirect to dashboard
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

  const logout = () => {
    localStorage.removeItem('authToken');
    localStorage.removeItem('user');
    setUser(null);
    toast.success('Logged out successfully');
    router.push('/login');
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
