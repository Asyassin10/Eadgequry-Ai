"use client"

import type React from "react"

import { useState, useEffect } from "react"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { AlertCircle, Database, Trash2, Loader2 } from "lucide-react"
import { Alert, AlertDescription } from "@/components/ui/alert"
import { toast } from "sonner"
import { datasourceApi, type DatabaseConfigDTO, type CreateDatabaseConfigRequest } from "@/lib/api"

// Define database types
const DATABASE_TYPES = [
  { value: "mysql", label: "MySQL", defaultPort: "3306", available: true },
  { value: "postgresql", label: "PostgreSQL", defaultPort: "5432", available: true },
  { value: "sqlserver", label: "SQL Server", defaultPort: "1433", available: false },
  { value: "sqlanywhere", label: "SQL Anywhere", defaultPort: "2638", available: false },
  { value: "sqlite", label: "SQLite", defaultPort: "", available: false },
  { value: "snowflake", label: "Snowflake", defaultPort: "", available: false },
  { value: "oracle", label: "Oracle", defaultPort: "1521", available: false },
  { value: "bigquery", label: "BigQuery", defaultPort: "", available: false },
  { value: "mariadb", label: "MariaDB", defaultPort: "3306", available: false },
  { value: "redshift", label: "Redshift", defaultPort: "5439", available: false },
]

export function DatasourcePage() {
  const [dbType, setDbType] = useState("mysql")
  const [formData, setFormData] = useState<Record<string, string>>({
    name: "",
    host: "localhost",
    port: "3306",
    database: "",
    username: "",
    password: "",
    knowledge: "",
  })
  const [savedDatabase, setSavedDatabase] = useState<DatabaseConfigDTO | null>(null)
  const [showLimitMessage, setShowLimitMessage] = useState(false)
  const [showUnavailableAlert, setShowUnavailableAlert] = useState(false)
  const [loading, setLoading] = useState(false)
  const [userId, setUserId] = useState<number | null>(null)

  // Load user data and saved configs on mount
  useEffect(() => {
    const userStr = localStorage.getItem('user')
    if (userStr) {
      try {
        const user = JSON.parse(userStr)
        setUserId(user.userId)
        loadConfigs(user.userId)
      } catch (error) {
        console.error('Failed to parse user data:', error)
        toast.error('Failed to load user data')
      }
    }
  }, [])

  // Load database configurations
  const loadConfigs = async (uid: number) => {
    setLoading(true)
    try {
      const response = await datasourceApi.getAllConfigs(uid)
      if (response.data && response.data.length > 0) {
        // Show the first config (since we only allow one for now)
        setSavedDatabase(response.data[0])
      }
    } catch (error) {
      console.error('Failed to load configs:', error)
      toast.error('Failed to load database configurations')
    } finally {
      setLoading(false)
    }
  }

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    const { name, value } = e.target
    setFormData((prev) => ({ ...prev, [name]: value }))
  }

  const handleDbTypeChange = (value: string) => {
    setDbType(value)
    const selectedDb = DATABASE_TYPES.find((db) => db.value === value)

    // Check if database is available
    if (selectedDb && !selectedDb.available) {
      setShowUnavailableAlert(true)
      toast.info(`${selectedDb.label} is not available yet. Coming soon in v1!`, {
        duration: 4000,
      })
      // Set basic form data but disable functionality
      setFormData({
        host: "",
        port: "",
        database: "",
        username: "",
        password: "",
        knowledge: formData.knowledge,
      })
      return
    }

    setShowUnavailableAlert(false)

    // Reset form with appropriate defaults for available databases (MySQL, PostgreSQL)
    const newFormData: Record<string, string> = {
      knowledge: formData.knowledge, // Keep knowledge field
      host: "localhost",
      port: selectedDb?.defaultPort || "",
      database: "",
      username: "",
      password: "",
    }

    setFormData(newFormData)
  }

  const handleSave = async () => {
    if (!userId) {
      toast.error('User not logged in')
      return
    }

    const selectedDb = DATABASE_TYPES.find((db) => db.value === dbType)

    // Prevent saving unavailable databases
    if (selectedDb && !selectedDb.available) {
      toast.error(`Cannot save ${selectedDb.label}. This database is not available yet.`)
      return
    }

    // Basic validation for MySQL and PostgreSQL
    if (!formData.name || !formData.username || !formData.password || !formData.database) {
      toast.error("Please fill in all required fields including connection name")
      return
    }

    setLoading(true)
    try {
      const requestData: CreateDatabaseConfigRequest = {
        name: formData.name,
        type: dbType,
        host: formData.host,
        port: formData.port ? parseInt(formData.port) : undefined,
        databaseName: formData.database,
        username: formData.username,
        password: formData.password,
      }

      const response = await datasourceApi.createConfig(userId, requestData)

      if (response.error) {
        toast.error(response.error.message || 'Failed to save configuration')
        return
      }

      if (response.data) {
        setSavedDatabase(response.data)
        toast.success("Database configuration saved successfully!")

        // Reset form
        handleDbTypeChange(dbType)
      }
    } catch (error) {
      console.error('Failed to save config:', error)
      toast.error('Failed to save database configuration')
    } finally {
      setLoading(false)
    }
  }

  const handleAddMore = () => {
    setShowLimitMessage(true)
    setTimeout(() => setShowLimitMessage(false), 4000)
  }

  const handleDelete = async () => {
    if (!userId || !savedDatabase) {
      return
    }

    setLoading(true)
    try {
      const response = await datasourceApi.deleteConfig(savedDatabase.id, userId)

      if (response.error) {
        toast.error(response.error.message || 'Failed to delete configuration')
        return
      }

      setSavedDatabase(null)
      toast.success("Database configuration deleted successfully!")
    } catch (error) {
      console.error('Failed to delete config:', error)
      toast.error('Failed to delete database configuration')
    } finally {
      setLoading(false)
    }
  }

  const handleTestConnection = async () => {
    if (!savedDatabase) {
      toast.error('No database configuration to test')
      return
    }

    setLoading(true)
    try {
      const response = await datasourceApi.testConnection(savedDatabase.id)

      if (response.error) {
        toast.error(response.error.message || 'Connection test failed')
        return
      }

      toast.success(response.data?.message || 'Connection test successful!')
    } catch (error) {
      console.error('Failed to test connection:', error)
      toast.error('Connection test failed')
    } finally {
      setLoading(false)
    }
  }

  const renderConnectionFields = () => {
    const selectedDb = DATABASE_TYPES.find((db) => db.value === dbType)

    // Show message for unavailable databases
    if (selectedDb && !selectedDb.available) {
      return (
        <Alert className="bg-yellow-50 dark:bg-yellow-950 border-yellow-200 dark:border-yellow-800">
          <AlertCircle className="h-4 w-4 text-yellow-600 dark:text-yellow-400" />
          <AlertDescription className="text-yellow-800 dark:text-yellow-200">
            <strong>{selectedDb.label}</strong> is not available yet. This database type will be supported in version 1.
            <br />
            Please select MySQL or PostgreSQL for now.
          </AlertDescription>
        </Alert>
      )
    }

    // Standard fields for MySQL and PostgreSQL
    return (
      <>
        <div className="space-y-2">
          <Label htmlFor="name">Connection Name *</Label>
          <Input
            id="name"
            name="name"
            placeholder="My Database"
            value={formData.name || ""}
            onChange={handleChange}
            className="bg-input border-border"
          />
        </div>

        <div className="space-y-2">
          <Label htmlFor="host">Host *</Label>
          <Input
            id="host"
            name="host"
            placeholder="localhost"
            value={formData.host || ""}
            onChange={handleChange}
            className="bg-input border-border"
          />
        </div>

        <div className="space-y-2">
          <Label htmlFor="port">Port *</Label>
          <Input
            id="port"
            name="port"
            placeholder={DATABASE_TYPES.find((db) => db.value === dbType)?.defaultPort || ""}
            value={formData.port || ""}
            onChange={handleChange}
            className="bg-input border-border"
          />
        </div>

        <div className="space-y-2">
          <Label htmlFor="database">Database Name *</Label>
          <Input
            id="database"
            name="database"
            placeholder="my_database"
            value={formData.database || ""}
            onChange={handleChange}
            className="bg-input border-border"
          />
        </div>

        <div className="space-y-2">
          <Label htmlFor="username">Username *</Label>
          <Input
            id="username"
            name="username"
            placeholder="Enter your username"
            value={formData.username || ""}
            onChange={handleChange}
            className="bg-input border-border"
          />
        </div>

        <div className="space-y-2">
          <Label htmlFor="password">Password *</Label>
          <Input
            id="password"
            name="password"
            type="password"
            placeholder="Enter your password"
            value={formData.password || ""}
            onChange={handleChange}
            className="bg-input border-border"
          />
        </div>
      </>
    )
  }

  const getDbTypeLabel = (type: string) => {
    return DATABASE_TYPES.find((db) => db.value === type)?.label || type
  }

  return (
    <div className="p-6 space-y-6">
      {showLimitMessage && (
        <Alert className="bg-blue-50 dark:bg-blue-950 border-blue-200 dark:border-blue-800">
          <AlertCircle className="h-4 w-4 text-blue-600 dark:text-blue-400" />
          <AlertDescription className="text-blue-800 dark:text-blue-200">
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
                  <CardTitle className="text-lg">{savedDatabase.name}</CardTitle>
                  <CardDescription>
                    {savedDatabase.type === "sqlite"
                      ? savedDatabase.filePath
                      : savedDatabase.type === "bigquery"
                        ? `${savedDatabase.projectId} / ${savedDatabase.dataset}`
                        : savedDatabase.type === "snowflake"
                          ? savedDatabase.account
                          : `${savedDatabase.host}:${savedDatabase.port}`}
                  </CardDescription>
                </div>
              </div>
              <Button
                variant="ghost"
                size="sm"
                onClick={handleDelete}
                disabled={loading}
                className="text-destructive hover:text-destructive hover:bg-destructive/10"
              >
                {loading ? <Loader2 className="h-4 w-4 animate-spin" /> : <Trash2 className="h-4 w-4" />}
              </Button>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="grid grid-cols-2 gap-4 text-sm">
                <div>
                  <span className="text-muted-foreground">Type:</span>
                  <p className="font-medium">{getDbTypeLabel(savedDatabase.type)}</p>
                </div>
                {savedDatabase.username && (
                  <div>
                    <span className="text-muted-foreground">Username:</span>
                    <p className="font-medium">{savedDatabase.username}</p>
                  </div>
                )}
                {savedDatabase.databaseName && (
                  <div>
                    <span className="text-muted-foreground">Database:</span>
                    <p className="font-medium">{savedDatabase.databaseName}</p>
                  </div>
                )}
                {savedDatabase.port && (
                  <div>
                    <span className="text-muted-foreground">Port:</span>
                    <p className="font-medium">{savedDatabase.port}</p>
                  </div>
                )}
                {savedDatabase.status && (
                  <div>
                    <span className="text-muted-foreground">Status:</span>
                    <p className="font-medium">{savedDatabase.status}</p>
                  </div>
                )}
                {savedDatabase.isConnected !== undefined && (
                  <div>
                    <span className="text-muted-foreground">Connected:</span>
                    <p className="font-medium">{savedDatabase.isConnected ? "Yes" : "No"}</p>
                  </div>
                )}
              </div>
              <div className="flex gap-4 mt-4">
                <Button
                  onClick={handleTestConnection}
                  disabled={loading}
                  variant="outline"
                  className="flex-1 border-border"
                >
                  {loading ? <Loader2 className="h-4 w-4 animate-spin mr-2" /> : null}
                  Test Connection
                </Button>
                <Button
                  onClick={handleAddMore}
                  disabled={loading}
                  className="flex-1 bg-primary hover:bg-secondary text-primary-foreground"
                >
                  Add Another Database
                </Button>
              </div>
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
              <Label htmlFor="dbtype">Database Type *</Label>
              <Select value={dbType} onValueChange={handleDbTypeChange}>
                <SelectTrigger className="bg-input border-border">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  {DATABASE_TYPES.map((db) => (
                    <SelectItem key={db.value} value={db.value}>
                      {db.label}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>

            {/* Dynamic Connection Fields */}
            {renderConnectionFields()}

            {/* Knowledge Database - Only show for available databases */}
            {!showUnavailableAlert && (
              <div className="space-y-2">
                <Label htmlFor="knowledge">Knowledge Database Context</Label>
                <textarea
                  id="knowledge"
                  name="knowledge"
                  placeholder="Describe your database schema and tables to help AI understand your data structure..."
                  value={formData.knowledge || ""}
                  onChange={handleChange}
                  className="w-full px-3 py-2 rounded-md bg-input border border-border text-foreground placeholder-muted-foreground focus:outline-none focus:ring-2 focus:ring-primary min-h-32 resize-none"
                />
                <p className="text-sm text-muted-foreground">
                  Optional: Provide context about your database structure to improve AI responses
                </p>
              </div>
            )}

            {/* Buttons - Only show for available databases */}
            {!showUnavailableAlert && (
              <div className="flex gap-4 pt-4">
                <Button
                  onClick={handleSave}
                  disabled={loading}
                  className="bg-primary hover:bg-secondary text-primary-foreground"
                >
                  {loading ? <Loader2 className="h-4 w-4 animate-spin mr-2" /> : null}
                  Save Configuration
                </Button>
              </div>
            )}
          </CardContent>
        </Card>
      )}
    </div>
  )
}
