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
    const prefersDark = window.matchMedia("(prefers-color-scheme: dark)").matches;
    const shouldBeDark = savedTheme ? savedTheme === "dark" : prefersDark;
    setIsDark(shouldBeDark);
    if (shouldBeDark) document.documentElement.classList.add("dark");
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
