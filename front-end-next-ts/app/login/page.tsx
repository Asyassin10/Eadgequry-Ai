'use client';

import { useState, useEffect } from 'react';
import Link from 'next/link';
import { useSearchParams } from 'next/navigation';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import * as z from 'zod';
import { useAuth } from '@/contexts/AuthContext';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Loader2, LogIn } from 'lucide-react';
import { toast } from 'sonner';

const loginSchema = z.object({
  email: z.string().email('Invalid email address'),
  password: z.string().min(1, 'Password is required'),
});

type LoginFormData = z.infer<typeof loginSchema>;

export default function LoginPage() {
  const { login, isLoading } = useAuth();
  const [isSubmitting, setIsSubmitting] = useState(false);
  const searchParams = useSearchParams();

  // Show message if redirected due to session expiration
  useEffect(() => {
    const reason = searchParams.get('reason');
    if (reason === 'session_expired') {
      toast.error('Your session has expired. Please login again.', {
        duration: 5000,
      });
    }
  }, [searchParams]);

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<LoginFormData>({
    resolver: zodResolver(loginSchema),
  });

  const onSubmit = async (data: LoginFormData) => {
    setIsSubmitting(true);
    await login(data);
    setIsSubmitting(false);
  };

  return (
    <div className="min-h-screen bg-gradient-to-b from-background to-muted">

      <nav className="border-b bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60 sticky top-0 z-50">
        <div className="container mx-auto px-4 py-4">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2">

              <a href="/">
              <span className="text-2xl font-bold bg-gradient-to-r from-primary to-blue-600 bg-clip-text text-transparent">
                EadgeQuery
              </span>
              </a>
              <span className="text-xs text-muted-foreground ml-2">v0.1</span>
                
            </div>
            <div className="hidden md:flex items-center gap-8">
              <a href="/#features" className="text-sm font-medium hover:text-primary transition-colors">
                Features
              </a>

              <a href="/#pricing" className="text-sm font-medium hover:text-primary transition-colors">
                Pricing
              </a>
              <Link href="/#faq" className="text-sm font-medium hover:text-primary transition-colors">
                FAQ
              </Link>
              <Link href="/docs" className="text-sm font-medium hover:text-primary transition-colors">
                Docs
              </Link>
              <Link href="/blog" className="text-sm font-medium hover:text-primary transition-colors">
                Blog
              </Link>
            </div>
            <div className="flex items-center gap-4">
 

            </div>
          </div>
        </div>
      </nav>
      <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-gray-50 to-gray-100 dark:from-gray-900 dark:to-gray-800 p-4">
        <Card className="w-full max-w-md">
          <CardHeader className="space-y-1">
            <div className="flex items-center justify-center mb-4">
            <div className="flex items-center justify-center mb-4">
              <div className=" w-20
                 rounded-full flex items-center justify-center">
                <img src="logo.png" className=" text-primary-foreground" />
              </div>
            </div>
            </div>
            <CardTitle className="text-2xl font-bold text-center">Welcome back</CardTitle>
            <CardDescription className="text-center">
              Sign in to your Eadgequry AI account
            </CardDescription>
          </CardHeader>
          <CardContent>
            <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
              <div className="space-y-2">
                <Label htmlFor="email">Email</Label>
                <Input
                  id="email"
                  type="email"
                  placeholder="john@example.com"
                  {...register('email')}
                  disabled={isSubmitting}
                />
                {errors.email && (
                  <p className="text-sm text-red-600 dark:text-red-400">{errors.email.message}</p>
                )}
              </div>

              <div className="space-y-2">
                <div className="flex items-center justify-between">
                  <Label htmlFor="password">Password</Label>
                  <Link
                    href="/forgot-password"
                    className="text-sm text-primary hover:underline"
                  >
                    Forgot password?
                  </Link>
                </div>
                <Input
                  id="password"
                  type="password"
                  placeholder="••••••••"
                  {...register('password')}
                  disabled={isSubmitting}
                />
                {errors.password && (
                  <p className="text-sm text-red-600 dark:text-red-400">{errors.password.message}</p>
                )}
              </div>

              <Button type="submit" className="w-full" disabled={isSubmitting || isLoading}>
                {isSubmitting || isLoading ? (
                  <>
                    <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                    Signing in...
                  </>
                ) : (
                  'Sign in'
                )}
              </Button>
            </form>

            <div className="mt-6 text-center text-sm">
              <span className="text-gray-600 dark:text-gray-400">Don't have an account? </span>
              <Link href="/register" className="text-primary hover:underline font-medium">
                Create account
              </Link>
            </div>

 
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
