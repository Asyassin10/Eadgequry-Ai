"use client"

import { useState, useEffect } from "react"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select"
import { Settings, Save, Key, Loader2, CheckCircle } from "lucide-react"
import { useAuth } from "@/contexts/AuthContext"
import { aiSettingsApi, type UserAiSettingsDTO, type AiProvider } from "@/lib/api"
import { toast } from "sonner"

export function AiSettingsPage() {
  const { user } = useAuth()
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [providers, setProviders] = useState<AiProvider[]>([])
  const [selectedProvider, setSelectedProvider] = useState<string>("DEMO")
  const [selectedModel, setSelectedModel] = useState<string>("")
  const [apiKey, setApiKey] = useState<string>("")
  const [hasExistingKey, setHasExistingKey] = useState(false)

  // Load user settings and available providers
  useEffect(() => {
    if (user?.userId) {
      loadSettings()
      loadProviders()
    }
  }, [user])

  const loadSettings = async () => {
    if (!user?.userId) return

    try {
      setLoading(true)
      const response = await aiSettingsApi.getUserSettings(user.userId)

      if (response.data) {
        setSelectedProvider(response.data.provider)
        setSelectedModel(response.data.model)
        setHasExistingKey(response.data.hasApiKey)
      }
    } catch (error) {
      console.error("Error loading AI settings:", error)
      toast.error("Failed to load AI settings")
    } finally {
      setLoading(false)
    }
  }

  const loadProviders = async () => {
    try {
      const response = await aiSettingsApi.getAvailableProviders()
      if (response.data) {
        setProviders(response.data.providers)
      }
    } catch (error) {
      console.error("Error loading providers:", error)
    }
  }

  const handleSave = async () => {
    if (!user?.userId) return

    // Validation
    if (!selectedProvider) {
      toast.error("Please select an AI provider")
      return
    }

    if (!selectedModel) {
      toast.error("Please select a model")
      return
    }

    if ((selectedProvider === "CLAUDE" || selectedProvider === "OPENAI") && !apiKey && !hasExistingKey) {
      toast.error("Please enter an API key for this provider")
      return
    }

    try {
      setSaving(true)
      const response = await aiSettingsApi.updateUserSettings(user.userId, {
        provider: selectedProvider,
        model: selectedModel,
        apiKey: apiKey || undefined,
      })

      if (response.data) {
        setHasExistingKey(response.data.hasApiKey)
        setApiKey("") // Clear the input after saving
        toast.success("AI settings saved successfully!")
      } else if (response.error) {
        toast.error(response.error.message || "Failed to save AI settings")
      }
    } catch (error) {
      console.error("Error saving AI settings:", error)
      toast.error("Failed to save AI settings")
    } finally {
      setSaving(false)
    }
  }

  const selectedProviderData = providers.find(p => p.code === selectedProvider)
  const needsApiKey = selectedProvider === "CLAUDE" || selectedProvider === "OPENAI"

  if (loading) {
    return (
      <div className="flex items-center justify-center h-screen">
        <Loader2 className="w-8 h-8 animate-spin text-primary" />
      </div>
    )
  }

  return (
    <div className="p-6 max-w-4xl mx-auto">
      <div className="mb-6">
        <h1 className="text-3xl font-bold flex items-center gap-2">
          <Settings className="w-8 h-8 text-primary" />
          AI Provider Settings
        </h1>
        <p className="text-muted-foreground mt-2">
          Configure which AI provider and model to use for your database queries
        </p>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>AI Configuration</CardTitle>
          <CardDescription>
            Choose between free or use your own API keys from Claude or OpenAI
          </CardDescription>
        </CardHeader>
        <CardContent className="space-y-6">
          {/* Provider Selection */}
          <div className="space-y-2">
            <Label htmlFor="provider">AI Provider</Label>
            <Select value={selectedProvider} onValueChange={setSelectedProvider}>
              <SelectTrigger id="provider">
                <SelectValue placeholder="Select AI provider" />
              </SelectTrigger>
              <SelectContent>
                {providers.map((provider) => (
                  <SelectItem key={provider.code} value={provider.code}>
                    {provider.name}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
            {selectedProvider === "DEMO" && (
              <p className="text-xs text-muted-foreground">
                ✨ Free to use! Uses the platform's OpenRouter API key. No setup required.
              </p>
            )}
            {needsApiKey && (
              <p className="text-xs text-muted-foreground">
                🔑 Requires your own API key from{" "}
                {selectedProvider === "CLAUDE" ? "Anthropic" : "OpenAI"}
              </p>
            )}
          </div>

          {/* Model Selection */}
          <div className="space-y-2">
            <Label htmlFor="model">Model</Label>
            <Select
              value={selectedModel}
              onValueChange={setSelectedModel}
              disabled={!selectedProviderData}
            >
              <SelectTrigger id="model">
                <SelectValue placeholder="Select model" />
              </SelectTrigger>
              <SelectContent>
                {selectedProviderData?.models.map((model) => (
                  <SelectItem key={model} value={model}>
                    {model}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>

          {/* API Key Input (only for Claude and OpenAI) */}
          {needsApiKey && (
            <div className="space-y-2">
              <Label htmlFor="apiKey">
                API Key
                {hasExistingKey && (
                  <span className="ml-2 text-xs text-green-600 flex items-center gap-1">
                    <CheckCircle className="w-3 h-3" />
                    Key configured
                  </span>
                )}
              </Label>
              <div className="relative">
                <Key className="absolute left-3 top-3 w-4 h-4 text-muted-foreground" />
                <Input
                  id="apiKey"
                  type="password"
                  placeholder={hasExistingKey ? "Enter new key to update" : "Enter your API key"}
                  value={apiKey}
                  onChange={(e) => setApiKey(e.target.value)}
                  className="pl-10"
                />
              </div>
              <p className="text-xs text-muted-foreground">
                {selectedProvider === "CLAUDE"
                  ? "Get your API key from https://console.anthropic.com/"
                  : "Get your API key from https://platform.openai.com/api-keys"
                }
              </p>
              <p className="text-xs text-yellow-600">
                🔒 Your API key will be encrypted and stored securely
              </p>
            </div>
          )}

          {/* Save Button */}
          <div className="flex justify-end pt-4">
            <Button
              onClick={handleSave}
              disabled={saving || !selectedProvider || !selectedModel}
              className="flex items-center gap-2"
            >
              {saving ? (
                <>
                  <Loader2 className="w-4 h-4 animate-spin" />
                  Saving...
                </>
              ) : (
                <>
                  <Save className="w-4 h-4" />
                  Save Settings
                </>
              )}
            </Button>
          </div>
        </CardContent>
      </Card>

      {/* Information Card */}
      <Card className="mt-6">
        <CardHeader>
          <CardTitle className="text-lg">Provider Comparison</CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          <div>
            <h4 className="font-semibold text-sm mb-2">🎁 DEMO Mode (Free)</h4>
            <ul className="text-sm text-muted-foreground space-y-1 ml-4 list-disc">
              <li>Uses platform's OpenRouter API key</li>
              <li>No setup or API key required</li>
              <li>Access to multiple models (Claude, GPT-4, etc.)</li>
              <li>Subject to platform usage limits</li>
            </ul>
          </div>

          <div>
            <h4 className="font-semibold text-sm mb-2">🤖 Claude AI (Your Key)</h4>
            <ul className="text-sm text-muted-foreground space-y-1 ml-4 list-disc">
              <li>Direct access to Anthropic's Claude models</li>
              <li>Your own API key and billing</li>
              <li>Higher rate limits</li>
              <li>Full control over usage</li>
            </ul>
          </div>

          <div>
            <h4 className="font-semibold text-sm mb-2">🚀 OpenAI (Your Key)</h4>
            <ul className="text-sm text-muted-foreground space-y-1 ml-4 list-disc">
              <li>Direct access to GPT-4 and other OpenAI models</li>
              <li>Your own API key and billing</li>
              <li>Higher rate limits</li>
              <li>Full control over usage</li>
            </ul>
          </div>
        </CardContent>
      </Card>
    </div>
  )
}
