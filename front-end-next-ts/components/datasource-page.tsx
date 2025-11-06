"use client"

import type React from "react"

import { useState } from "react"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { AlertCircle, Database, Trash2 } from "lucide-react"
import { Alert, AlertDescription } from "@/components/ui/alert"

export function DatasourcePage() {
  const [dbType, setDbType] = useState("postgresql")
  const [formData, setFormData] = useState({
    host: "localhost",
    port: "5432",
    username: "",
    password: "",
    knowledge: "",
  })
  const [savedDatabase, setSavedDatabase] = useState<(typeof formData & { dbType: string }) | null>(null)
  const [showLimitMessage, setShowLimitMessage] = useState(false)

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    const { name, value } = e.target
    setFormData((prev) => ({ ...prev, [name]: value }))
  }

  const handleSave = () => {
    if (!formData.username || !formData.password) {
      alert("Please fill in all required fields")
      return
    }
    setSavedDatabase({ ...formData, dbType })
    setFormData({
      host: "localhost",
      port: "5432",
      username: "",
      password: "",
      knowledge: "",
    })
  }

  const handleAddMore = () => {
    setShowLimitMessage(true)
    setTimeout(() => setShowLimitMessage(false), 4000)
  }

  const handleDelete = () => {
    setSavedDatabase(null)
  }

  return (
    <div className="p-6 space-y-6">
      {showLimitMessage && (
        <Alert className="bg-blue-50 border-blue-200">
          <AlertCircle className="h-4 w-4 text-blue-600" />
          <AlertDescription className="text-blue-800">
            You can add only one database for now. Multiple databases will be available in the next version.
          </AlertDescription>
        </Alert>
      )}

      {savedDatabase ? (
        <div className="space-y-6">
          <Card className="bg-card border-border">
            <CardHeader className="flex flex-row items-start justify-between space-y-0">
              <div className="flex items-center gap-3">
                <Database className="h-5 w-5 text-primary" />
                <div>
                  <CardTitle className="text-lg">
                    {savedDatabase.dbType === "postgresql" ? "PostgreSQL" : "MySQL"}
                  </CardTitle>
                  <CardDescription>
                    {savedDatabase.host}:{savedDatabase.port}
                  </CardDescription>
                </div>
              </div>
              <Button
                variant="ghost"
                size="sm"
                onClick={handleDelete}
                className="text-destructive hover:text-destructive hover:bg-destructive/10"
              >
                <Trash2 className="h-4 w-4" />
              </Button>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="grid grid-cols-2 gap-4 text-sm">
                <div>
                  <span className="text-muted-foreground">Username:</span>
                  <p className="font-medium">{savedDatabase.username}</p>
                </div>
                <div>
                  <span className="text-muted-foreground">Port:</span>
                  <p className="font-medium">{savedDatabase.port}</p>
                </div>
              </div>
              {savedDatabase.knowledge && (
                <div>
                  <span className="text-muted-foreground text-sm">Knowledge Context:</span>
                  <p className="text-sm mt-2 p-3 bg-muted rounded-md line-clamp-3">{savedDatabase.knowledge}</p>
                </div>
              )}
              <Button
                onClick={handleAddMore}
                className="w-full bg-primary hover:bg-secondary text-primary-foreground mt-4"
              >
                Add Another Database
              </Button>
            </CardContent>
          </Card>
        </div>
      ) : (
        <Card className="bg-card border-border">
          <CardHeader>
            <CardTitle>Database Configuration</CardTitle>
            <CardDescription>Configure your datasource connection</CardDescription>
          </CardHeader>
          <CardContent className="space-y-6">
            {/* Database Type */}
            <div className="space-y-2">
              <Label htmlFor="dbtype">Database Type</Label>
              <Select value={dbType} onValueChange={setDbType}>
                <SelectTrigger className="bg-input border-border">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="postgresql">PostgreSQL</SelectItem>
                  <SelectItem value="mysql">MySQL</SelectItem>
                </SelectContent>
              </Select>
            </div>

            {/* Host */}
            <div className="space-y-2">
              <Label htmlFor="host">Host</Label>
              <Input
                id="host"
                name="host"
                placeholder="localhost"
                value={formData.host}
                onChange={handleChange}
                className="bg-input border-border"
              />
            </div>

            {/* Port */}
            <div className="space-y-2">
              <Label htmlFor="port">Port</Label>
              <Input
                id="port"
                name="port"
                placeholder="5432"
                value={formData.port}
                onChange={handleChange}
                className="bg-input border-border"
              />
            </div>

            {/* Username */}
            <div className="space-y-2">
              <Label htmlFor="username">Username</Label>
              <Input
                id="username"
                name="username"
                placeholder="Enter your username"
                value={formData.username}
                onChange={handleChange}
                className="bg-input border-border"
              />
            </div>

            {/* Password */}
            <div className="space-y-2">
              <Label htmlFor="password">Password</Label>
              <Input
                id="password"
                name="password"
                type="password"
                placeholder="Enter your password"
                value={formData.password}
                onChange={handleChange}
                className="bg-input border-border"
              />
            </div>

            {/* Knowledge Database */}
            <div className="space-y-2">
              <Label htmlFor="knowledge">Knowledge Database Context</Label>
              <textarea
                id="knowledge"
                name="knowledge"
                placeholder="Describe your database schema and tables to help AI understand your data structure..."
                value={formData.knowledge}
                onChange={handleChange}
                className="w-full px-3 py-2 rounded-md bg-input border border-border text-foreground placeholder-muted-foreground focus:outline-none focus:ring-2 focus:ring-primary min-h-32 resize-none"
              />
            </div>

            {/* Buttons */}
            <div className="flex gap-4 pt-4">
              <Button onClick={handleSave} className="bg-primary hover:bg-secondary text-primary-foreground">
                Save Configuration
              </Button>
              <Button variant="outline" className="border-border bg-transparent">
                Test Connection
              </Button>
            </div>
          </CardContent>
        </Card>
      )}
    </div>
  )
}
