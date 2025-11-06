'use client';

import { useState, useEffect } from "react";
import { AuthProvider } from "@/contexts/AuthContext";
import { Toaster } from "sonner";

export default function ThemeProvider({ children }: { children: React.ReactNode }) {
  const [isDark, setIsDark] = useState(false);
  const [mounted, setMounted] = useState(false);

  useEffect(() => {
    setMounted(true);
    const savedTheme = localStorage.getItem("theme");
    // Default to light mode (false) - only dark if explicitly saved as "dark"
    const shouldBeDark = savedTheme === "dark";
    setIsDark(shouldBeDark);
    if (shouldBeDark) {
      document.documentElement.classList.add("dark");
    } else {
      document.documentElement.classList.remove("dark");
    }
  }, []);

  const toggleTheme = () => {
    const newIsDark = !isDark;
    setIsDark(newIsDark);
    localStorage.setItem("theme", newIsDark ? "dark" : "light");
    if (newIsDark) document.documentElement.classList.add("dark");
    else document.documentElement.classList.remove("dark");
  };

  if (!mounted) return null;

  return (
    <AuthProvider>
      {children}
      <Toaster position="top-right" richColors />
    </AuthProvider>
  );
}
