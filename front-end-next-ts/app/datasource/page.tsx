"use client"

import { useState, useEffect } from "react"
import { Layout } from "@/components/layout"
import { DatasourcePage } from "@/components/datasource-page"

export default function Datasource() {
  const [isDark, setIsDark] = useState(false)
  const [mounted, setMounted] = useState(false)

  useEffect(() => {
    setMounted(true)
    const savedTheme = localStorage.getItem("theme")
    // Default to light mode - only dark if explicitly saved
    const shouldBeDark = savedTheme === "dark"
    setIsDark(shouldBeDark)
  }, [])

  const toggleTheme = () => {
    const newIsDark = !isDark
    setIsDark(newIsDark)
    localStorage.setItem("theme", newIsDark ? "dark" : "light")
    if (newIsDark) {
      document.documentElement.classList.add("dark")
    } else {
      document.documentElement.classList.remove("dark")
    }
  }

  if (!mounted) return null

  return (
    <Layout isDark={isDark} onToggleTheme={toggleTheme}>
      <DatasourcePage />
    </Layout>
  )
}
