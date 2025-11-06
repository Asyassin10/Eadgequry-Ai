"use client"

import React from "react"

import type { ReactElement } from "react"

import { useState } from "react"
import { useRouter } from "next/navigation"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Avatar, AvatarImage, AvatarFallback } from "@/components/ui/avatar"
import { ArrowLeft, Moon, Sun, Globe } from "lucide-react"

export function SettingsPage(): ReactElement {
  const router = useRouter()
  const [userName] = useState("John Doe")
  const [showProfile, setShowProfile] = useState(false)
  const [isDark, setIsDark] = useState(false)
  const [language, setLanguage] = useState("en")
  const [selectedModel, setSelectedModel] = useState("gpt-4")
  const [email, setEmail] = useState("john@example.com")
  const [apiKey, setApiKey] = useState("")

  React.useEffect(() => {
    const savedTheme = localStorage.getItem("theme")
    const savedLanguage = localStorage.getItem("language")
    setIsDark(savedTheme === "dark")
    if (savedLanguage) setLanguage(savedLanguage)
  }, [])

  const handleDarkModeToggle = () => {
    const newIsDark = !isDark
    setIsDark(newIsDark)
    localStorage.setItem("theme", newIsDark ? "dark" : "light")
    if (newIsDark) {
      document.documentElement.classList.add("dark")
    } else {
      document.documentElement.classList.remove("dark")
    }
  }

  const handleLanguageChange = (value: string) => {
    setLanguage(value)
    localStorage.setItem("language", value)
  }

  return (
    <div className="p-6 space-y-6">
      {!showProfile && (
        <Card
          className="bg-card border-border cursor-pointer hover:bg-muted/50 transition-colors"
          onClick={() => setShowProfile(true)}
        >
          <CardContent className="pt-6 flex items-center gap-4">
            <Avatar className="w-12 h-12">
              <AvatarImage src="https://avatar.vercel.sh/user?size=48" />
              <AvatarFallback>JD</AvatarFallback>
            </Avatar>
            <div>
              <p className="text-lg font-semibold text-foreground">{userName}</p>
              <p className="text-sm text-muted-foreground">Click to view settings</p>
            </div>
          </CardContent>
        </Card>
      )}

      {showProfile && (
        <>
          <div className="flex items-center gap-2 mb-4">
            <Button variant="ghost" size="sm" onClick={() => setShowProfile(false)} className="flex items-center gap-2">
              <ArrowLeft className="w-4 h-4" />
              Back
            </Button>
          </div>

          <Card className="bg-card border-border">
            <CardHeader>
              <CardTitle>Profile Settings</CardTitle>
              <CardDescription>Manage your account information</CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="space-y-2">
                <Label htmlFor="name">Name</Label>
                <Input id="name" value={userName} disabled className="bg-input border-border" />
              </div>

              <div className="space-y-2">
                <Label htmlFor="email">Email</Label>
                <div className="flex gap-2">
                  <Input
                    id="email"
                    type="email"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    className="flex-1 bg-input border-border"
                  />
                  <Button className="bg-primary hover:bg-secondary text-primary-foreground">Update</Button>
                </div>
              </div>

              <div className="space-y-2">
                <Label htmlFor="password">Password</Label>
                <Button variant="outline" className="border-border bg-transparent">
                  Reset Password
                </Button>
              </div>
            </CardContent>
          </Card>
        </>
      )}

      <Card className="bg-card border-border">
        <CardHeader>
          <CardTitle>Preferences</CardTitle>
          <CardDescription>Customize your experience</CardDescription>
        </CardHeader>
        <CardContent className="space-y-6">
          {/* Dark Mode */}
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-3">
              {isDark ? <Moon className="w-5 h-5" /> : <Sun className="w-5 h-5" />}
              <div>
                <p className="font-medium text-foreground">Dark Mode</p>
                <p className="text-sm text-muted-foreground">Toggle dark theme</p>
              </div>
            </div>
            <Button variant="outline" size="sm" onClick={handleDarkModeToggle} className="border-border bg-transparent">
              {isDark ? "Disable" : "Enable"}
            </Button>
          </div>

          {/* Language */}
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-3">
              <Globe className="w-5 h-5" />
              <div>
                <p className="font-medium text-foreground">Language</p>
                <p className="text-sm text-muted-foreground">Select your language</p>
              </div>
            </div>
            <Select value={language} onValueChange={handleLanguageChange}>
              <SelectTrigger className="w-40 bg-input border-border">
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="en">English</SelectItem>
                <SelectItem value="es">Español</SelectItem>
                <SelectItem value="fr">Français</SelectItem>
                <SelectItem value="de">Deutsch</SelectItem>
              </SelectContent>
            </Select>
          </div>
        </CardContent>
      </Card>

      {/* API Settings */}
      <Card className="bg-card border-border">
        <CardHeader>
          <CardTitle>API Settings</CardTitle>
          <CardDescription>Configure your API keys and model selection</CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="space-y-2">
            <Label htmlFor="apikey">API Key</Label>
            <Input
              id="apikey"
              type="password"
              value={apiKey}
              onChange={(e) => setApiKey(e.target.value)}
              placeholder="sk-..."
              className="bg-input border-border"
            />
          </div>

          <div className="space-y-2">
            <Label htmlFor="model">AI Model</Label>
            <Select value={selectedModel} onValueChange={setSelectedModel}>
              <SelectTrigger className="bg-input border-border">
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="gpt-4">OpenAI - GPT-4</SelectItem>
                <SelectItem value="gpt-3.5-turbo">OpenAI - GPT-3.5 Turbo</SelectItem>
                <SelectItem value="claude-3-opus">Claude - Claude 3 Opus</SelectItem>
                <SelectItem value="claude-3-sonnet">Claude - Claude 3 Sonnet</SelectItem>
              </SelectContent>
            </Select>
          </div>

          <Button
            onClick={() => console.log("Saved")}
            className="bg-primary hover:bg-secondary text-primary-foreground"
          >
            Save API Settings
          </Button>
        </CardContent>
      </Card>
    </div>
  )
}
