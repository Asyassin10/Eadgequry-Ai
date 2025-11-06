'use client';

import { useState, useEffect } from 'react';
import { useAuth } from '@/contexts/AuthContext';
import { profileApi, authApi, type ProfileResponse, type UpdateProfileRequest, type UpdateEmailRequest, type UpdatePasswordRequest } from '@/lib/api';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import * as z from 'zod';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Textarea } from '@/components/ui/textarea';
import { Switch } from '@/components/ui/switch';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Loader2, User, Mail, Lock, Save, Settings2 } from 'lucide-react';
import { toast } from 'sonner';

// User preferences interface
interface UserPreferences {
  darkMode: boolean;
  language: 'en' | 'fr';
}

// Validation schemas
const profileSchema = z.object({
  name: z.string().min(2, 'Name must be at least 2 characters'),
  bio: z.string().max(5000, 'Bio cannot exceed 5000 characters').optional(),
  avatarUrl: z.string().url('Must be a valid URL').optional().or(z.literal('')),
});

const emailSchema = z.object({
  newEmail: z.string().email('Invalid email address'),
  password: z.string().min(1, 'Password is required'),
});

const passwordSchema = z.object({
  currentPassword: z.string().min(1, 'Current password is required'),
  newPassword: z.string().min(8, 'Password must be at least 8 characters'),
  confirmPassword: z.string(),
}).refine((data) => data.newPassword === data.confirmPassword, {
  message: "Passwords don't match",
  path: ['confirmPassword'],
});

type ProfileFormData = z.infer<typeof profileSchema>;
type EmailFormData = z.infer<typeof emailSchema>;
type PasswordFormData = z.infer<typeof passwordSchema>;

export function SettingsPageNew() {
  const { user } = useAuth();
  const [profile, setProfile] = useState<ProfileResponse | null>(null);
  const [isLoadingProfile, setIsLoadingProfile] = useState(true);
  const [preferences, setPreferences] = useState<UserPreferences>({
    darkMode: false,
    language: 'en',
  });
  const [isSavingPreferences, setIsSavingPreferences] = useState(false);

  // Profile form
  const {
    register: registerProfile,
    handleSubmit: handleSubmitProfile,
    formState: { errors: profileErrors, isSubmitting: isSubmittingProfile },
    reset: resetProfile,
  } = useForm<ProfileFormData>({
    resolver: zodResolver(profileSchema),
  });

  // Email form
  const {
    register: registerEmail,
    handleSubmit: handleSubmitEmail,
    formState: { errors: emailErrors, isSubmitting: isSubmittingEmail },
    reset: resetEmail,
  } = useForm<EmailFormData>({
    resolver: zodResolver(emailSchema),
  });

  // Password form
  const {
    register: registerPassword,
    handleSubmit: handleSubmitPassword,
    formState: { errors: passwordErrors, isSubmitting: isSubmittingPassword },
    reset: resetPassword,
  } = useForm<PasswordFormData>({
    resolver: zodResolver(passwordSchema),
  });

  // Load profile data and preferences
  useEffect(() => {
    const loadProfile = async () => {
      if (!user?.userId) return;

      setIsLoadingProfile(true);
      const response = await profileApi.getProfile(user.userId);

      if (response.data) {
        setProfile(response.data);
        resetProfile({
          name: response.data.name,
          bio: response.data.bio || '',
          avatarUrl: response.data.avatarUrl || '',
        });

        // Parse and load preferences from JSON
        if (response.data.preferences) {
          try {
            const prefs: UserPreferences = JSON.parse(response.data.preferences);
            setPreferences({
              darkMode: prefs.darkMode || false,
              language: prefs.language || 'en',
            });
            // Save to localStorage for consistency across the app
            localStorage.setItem('theme', prefs.darkMode ? 'dark' : 'light');
            // Apply dark mode
            if (prefs.darkMode) {
              document.documentElement.classList.add('dark');
            } else {
              document.documentElement.classList.remove('dark');
            }
          } catch (error) {
            console.error('Failed to parse preferences:', error);
          }
        }
      } else if (response.error) {
        // Don't show error toast for 401 - auto-logout will handle it
        if (response.error.status !== 401) {
          toast.error('Failed to load profile');
        }
      }

      setIsLoadingProfile(false);
    };

    loadProfile();
  }, [user, resetProfile]);

  // Handle profile update
  const onSubmitProfile = async (data: ProfileFormData) => {
    if (!user?.userId) return;

    const updateData: UpdateProfileRequest = {
      name: data.name,
      bio: data.bio || undefined,
      avatarUrl: data.avatarUrl || undefined,
    };

    const response = await profileApi.updateProfile(user.userId, updateData);

    if (response.data) {
      setProfile(response.data);
      toast.success('Profile updated successfully!');
    } else if (response.error) {
      toast.error(response.error.message);
    }
  };

  // Handle email update
  const onSubmitEmail = async (data: EmailFormData) => {
    const response = await authApi.updateEmail(data);

    if (response.data) {
      toast.success('Email update initiated! Please check your new email to verify.');
      resetEmail();
    } else if (response.error) {
      toast.error(response.error.message);
    }
  };

  // Handle password update
  const onSubmitPassword = async (data: PasswordFormData) => {
    const updateData: UpdatePasswordRequest = {
      currentPassword: data.currentPassword,
      newPassword: data.newPassword,
    };

    const response = await authApi.updatePassword(updateData);

    if (response.data) {
      toast.success('Password updated successfully!');
      resetPassword();
    } else if (response.error) {
      toast.error(response.error.message);
    }
  };

  // Handle preferences update
  const handleSavePreferences = async () => {
    if (!user?.userId) return;

    setIsSavingPreferences(true);

    try {
      // Convert preferences to JSON string
      const preferencesJson = JSON.stringify(preferences);

      const response = await profileApi.updateProfile(user.userId, {
        preferences: preferencesJson,
      });

      if (response.data) {
        toast.success('Preferences saved successfully!');
        // Save to localStorage for consistency across pages
        localStorage.setItem('theme', preferences.darkMode ? 'dark' : 'light');
        // Apply dark mode immediately
        if (preferences.darkMode) {
          document.documentElement.classList.add('dark');
        } else {
          document.documentElement.classList.remove('dark');
        }
      } else if (response.error) {
        toast.error(response.error.message);
      }
    } catch (error) {
      console.error('Failed to save preferences:', error);
      toast.error('Failed to save preferences');
    } finally {
      setIsSavingPreferences(false);
    }
  };

  // Handle dark mode toggle
  const handleDarkModeChange = (checked: boolean) => {
    setPreferences((prev) => ({ ...prev, darkMode: checked }));
  };

  // Handle language change
  const handleLanguageChange = (value: 'en' | 'fr') => {
    setPreferences((prev) => ({ ...prev, language: value }));
  };

  return (
    <div className="container mx-auto py-8 px-4 max-w-4xl">
      <div className="mb-8">
        <h1 className="text-3xl font-bold">Settings</h1>
        <p className="text-muted-foreground mt-2">Manage your account settings and preferences</p>
      </div>

      <Tabs defaultValue="profile" className="space-y-6">
        <TabsList className="grid w-full grid-cols-4">
          <TabsTrigger value="profile">
            <User className="w-4 h-4 mr-2" />
            Profile
          </TabsTrigger>
          <TabsTrigger value="email">
            <Mail className="w-4 h-4 mr-2" />
            Email
          </TabsTrigger>
          <TabsTrigger value="password">
            <Lock className="w-4 h-4 mr-2" />
            Password
          </TabsTrigger>
          <TabsTrigger value="preferences">
            <Settings2 className="w-4 h-4 mr-2" />
            Preferences
          </TabsTrigger>
        </TabsList>

        {/* Profile Tab */}
        <TabsContent value="profile">
          <Card>
            <CardHeader>
              <CardTitle>Profile Information</CardTitle>
              <CardDescription>
                Update your profile information including name, bio, and avatar
              </CardDescription>
            </CardHeader>
            <CardContent>
              {isLoadingProfile ? (
                <div className="flex items-center justify-center py-8">
                  <Loader2 className="h-8 w-8 animate-spin text-primary" />
                </div>
              ) : (
                <form onSubmit={handleSubmitProfile(onSubmitProfile)} className="space-y-4">
                  <div className="space-y-2">
                    <Label htmlFor="name">Full Name</Label>
                    <Input
                      id="name"
                      {...registerProfile('name')}
                      disabled={isSubmittingProfile}
                      className="bg-input border-border"
                    />
                    {profileErrors.name && (
                      <p className="text-sm text-red-600 dark:text-red-400">{profileErrors.name.message}</p>
                    )}
                  </div>

                  <div className="space-y-2">
                    <Label htmlFor="bio">Bio</Label>
                    <Textarea
                      id="bio"
                      placeholder="Tell us about yourself..."
                      rows={4}
                      {...registerProfile('bio')}
                      disabled={isSubmittingProfile}
                      className="bg-input border-border"
                    />
                    {profileErrors.bio && (
                      <p className="text-sm text-red-600 dark:text-red-400">{profileErrors.bio.message}</p>
                    )}
                  </div>

                  <div className="space-y-2">
                    <Label htmlFor="avatarUrl">Avatar URL</Label>
                    <Input
                      id="avatarUrl"
                      type="url"
                      placeholder="https://example.com/avatar.jpg"
                      {...registerProfile('avatarUrl')}
                      disabled={isSubmittingProfile}
                      className="bg-input border-border"
                    />
                    {profileErrors.avatarUrl && (
                      <p className="text-sm text-red-600 dark:text-red-400">{profileErrors.avatarUrl.message}</p>
                    )}
                  </div>

                  <Button type="submit" disabled={isSubmittingProfile} className="bg-primary hover:bg-secondary text-primary-foreground">
                    {isSubmittingProfile ? (
                      <>
                        <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                        Saving...
                      </>
                    ) : (
                      <>
                        <Save className="mr-2 h-4 w-4" />
                        Save Changes
                      </>
                    )}
                  </Button>
                </form>
              )}
            </CardContent>
          </Card>
        </TabsContent>

        {/* Email Tab */}
        <TabsContent value="email">
          <Card>
            <CardHeader>
              <CardTitle>Change Email</CardTitle>
              <CardDescription>
                Update your email address. You'll need to verify the new email.
              </CardDescription>
            </CardHeader>
            <CardContent>
              <form onSubmit={handleSubmitEmail(onSubmitEmail)} className="space-y-4">
                <div className="space-y-2">
                  <Label htmlFor="currentEmail">Current Email</Label>
                  <Input
                    id="currentEmail"
                    value={user?.email || ''}
                    disabled
                    className="bg-muted"
                  />
                </div>

                <div className="space-y-2">
                  <Label htmlFor="newEmail">New Email</Label>
                  <Input
                    id="newEmail"
                    type="email"
                    placeholder="newemail@example.com"
                    {...registerEmail('newEmail')}
                    disabled={isSubmittingEmail}
                    className="bg-input border-border"
                  />
                  {emailErrors.newEmail && (
                    <p className="text-sm text-red-600 dark:text-red-400">{emailErrors.newEmail.message}</p>
                  )}
                </div>

                <div className="space-y-2">
                  <Label htmlFor="emailPassword">Current Password</Label>
                  <Input
                    id="emailPassword"
                    type="password"
                    placeholder="Enter your current password"
                    {...registerEmail('password')}
                    disabled={isSubmittingEmail}
                    className="bg-input border-border"
                  />
                  {emailErrors.password && (
                    <p className="text-sm text-red-600 dark:text-red-400">{emailErrors.password.message}</p>
                  )}
                </div>

                <Button type="submit" disabled={isSubmittingEmail} className="bg-primary hover:bg-secondary text-primary-foreground">
                  {isSubmittingEmail ? (
                    <>
                      <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                      Updating...
                    </>
                  ) : (
                    <>
                      <Mail className="mr-2 h-4 w-4" />
                      Update Email
                    </>
                  )}
                </Button>
              </form>
            </CardContent>
          </Card>
        </TabsContent>

        {/* Password Tab */}
        <TabsContent value="password">
          <Card>
            <CardHeader>
              <CardTitle>Change Password</CardTitle>
              <CardDescription>
                Update your password to keep your account secure
              </CardDescription>
            </CardHeader>
            <CardContent>
              <form onSubmit={handleSubmitPassword(onSubmitPassword)} className="space-y-4">
                <div className="space-y-2">
                  <Label htmlFor="currentPassword">Current Password</Label>
                  <Input
                    id="currentPassword"
                    type="password"
                    {...registerPassword('currentPassword')}
                    disabled={isSubmittingPassword}
                    className="bg-input border-border"
                  />
                  {passwordErrors.currentPassword && (
                    <p className="text-sm text-red-600 dark:text-red-400">{passwordErrors.currentPassword.message}</p>
                  )}
                </div>

                <div className="space-y-2">
                  <Label htmlFor="newPassword">New Password</Label>
                  <Input
                    id="newPassword"
                    type="password"
                    {...registerPassword('newPassword')}
                    disabled={isSubmittingPassword}
                    className="bg-input border-border"
                  />
                  {passwordErrors.newPassword && (
                    <p className="text-sm text-red-600 dark:text-red-400">{passwordErrors.newPassword.message}</p>
                  )}
                </div>

                <div className="space-y-2">
                  <Label htmlFor="confirmPassword">Confirm New Password</Label>
                  <Input
                    id="confirmPassword"
                    type="password"
                    {...registerPassword('confirmPassword')}
                    disabled={isSubmittingPassword}
                    className="bg-input border-border"
                  />
                  {passwordErrors.confirmPassword && (
                    <p className="text-sm text-red-600 dark:text-red-400">{passwordErrors.confirmPassword.message}</p>
                  )}
                </div>

                <Button type="submit" disabled={isSubmittingPassword} className="bg-primary hover:bg-secondary text-primary-foreground">
                  {isSubmittingPassword ? (
                    <>
                      <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                      Updating...
                    </>
                  ) : (
                    <>
                      <Lock className="mr-2 h-4 w-4" />
                      Update Password
                    </>
                  )}
                </Button>
              </form>
            </CardContent>
          </Card>
        </TabsContent>

        {/* Preferences Tab */}
        <TabsContent value="preferences">
          <Card>
            <CardHeader>
              <CardTitle>Preferences</CardTitle>
              <CardDescription>
                Customize your experience with dark mode and language settings
              </CardDescription>
            </CardHeader>
            <CardContent className="space-y-6">
              {isLoadingProfile ? (
                <div className="flex items-center justify-center py-8">
                  <Loader2 className="h-8 w-8 animate-spin text-primary" />
                </div>
              ) : (
                <>
                  {/* Dark Mode Toggle */}
                  <div className="flex items-center justify-between space-x-4">
                    <div className="space-y-0.5 flex-1">
                      <Label htmlFor="dark-mode">Dark Mode</Label>
                      <p className="text-sm text-muted-foreground">
                        {preferences.darkMode ? 'Dark mode is enabled' : 'Enable dark mode for a darker theme'}
                      </p>
                    </div>
                    <Switch
                      id="dark-mode"
                      checked={preferences.darkMode}
                      onCheckedChange={handleDarkModeChange}
                      disabled={isSavingPreferences}
                    />
                  </div>

                  {/* Language Selector */}
                  <div className="space-y-2">
                    <Label htmlFor="language">Language</Label>
                    <Select
                      value={preferences.language}
                      onValueChange={handleLanguageChange}
                      disabled={isSavingPreferences}
                    >
                      <SelectTrigger id="language" className="bg-input border-border">
                        <SelectValue placeholder="Select language" />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem value="en">English</SelectItem>
                        <SelectItem value="fr">Français (French)</SelectItem>
                      </SelectContent>
                    </Select>
                    <p className="text-sm text-muted-foreground">
                      Selected: {preferences.language === 'en' ? 'English' : 'Français'}
                    </p>
                  </div>

                  {/* Save Button */}
                  <Button
                    onClick={handleSavePreferences}
                    disabled={isSavingPreferences}
                    className="w-full sm:w-auto bg-primary hover:bg-secondary text-primary-foreground"
                  >
                    {isSavingPreferences ? (
                      <>
                        <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                        Saving...
                      </>
                    ) : (
                      <>
                        <Save className="mr-2 h-4 w-4" />
                        Save Preferences
                      </>
                    )}
                  </Button>
                </>
              )}
            </CardContent>
          </Card>
        </TabsContent>
      </Tabs>
    </div>
  );
}
