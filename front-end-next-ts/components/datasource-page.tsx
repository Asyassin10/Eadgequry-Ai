"use client"

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

const DATABASE_TYPES = [
  { value: "mysql", label: "MySQL", defaultPort: "3306", available: true },
  { value: "postgresql", label: "PostgreSQL", defaultPort: "5432", available: true },
]

const MAX_DATABASES = 3

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
  const [savedDatabases, setSavedDatabases] = useState<DatabaseConfigDTO[]>([])
  const [loading, setLoading] = useState(false)
  const [userId, setUserId] = useState<number | null>(null)

  useEffect(() => {
    const userStr = localStorage.getItem("user")
    if (userStr) {
      try {
        const user = JSON.parse(userStr)
        setUserId(user.userId)
        loadConfigs(user.userId)
      } catch (error) {
        console.error("Failed to parse user data:", error)
        toast.error("Failed to load user data")
      }
    }
  }, [])

  const loadConfigs = async (uid: number) => {
    setLoading(true)
    try {
      const response = await datasourceApi.getAllConfigs(uid)
      if (response.data && response.data.length > 0) {
        setSavedDatabases(response.data)
      }
    } catch (error) {
      console.error("Failed to load configs:", error)
      toast.error("Failed to load database configurations")
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
    setFormData({
      ...formData,
      host: "localhost",
      port: selectedDb?.defaultPort || "",
      database: "",
      username: "",
      password: "",
    })
  }

  const handleSave = async () => {
    if (!userId) {
      toast.error("User not logged in")
      return
    }

    if (savedDatabases.length >= MAX_DATABASES) {
      toast.error(`You can only have up to ${MAX_DATABASES} databases`)
      return
    }

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
        toast.error(response.error.message || "Failed to save configuration")
        return
      }

      if (response.data) {
        setSavedDatabases((prev) => [...prev, response.data])
        toast.success("Database configuration saved successfully!")
        setFormData({
          name: "",
          host: "localhost",
          port: DATABASE_TYPES.find((db) => db.value === dbType)?.defaultPort || "",
          database: "",
          username: "",
          password: "",
          knowledge: "",
        })
      }
    } catch (error) {
      console.error("Failed to save config:", error)
      toast.error("Failed to save database configuration")
    } finally {
      setLoading(false)
    }
  }

  const handleDelete = async (id: number) => {
    if (!userId) return
    setLoading(true)
    try {
      const response = await datasourceApi.deleteConfig(id, userId)
      if (response.error) {
        toast.error(response.error.message || "Failed to delete configuration")
        return
      }
      setSavedDatabases((prev) => prev.filter((db) => db.id !== id))
      toast.success("Database configuration deleted successfully!")
    } catch (error) {
      console.error("Failed to delete config:", error)
      toast.error("Failed to delete database configuration")
    } finally {
      setLoading(false)
    }
  }

  const handleTestConnection = async (id: number) => {
    if (!userId) {
      toast.error("User not logged in")
      return
    }

    setLoading(true)
    try {
      const response = await datasourceApi.testConnection(id, userId)
      if (response.error) {
        toast.error(response.error.message || "Connection test failed")
        return
      }

      if (response.data?.success) {
        toast.success(response.data.message || "Connection test successful!")
      } else {
        toast.error(response.data?.message || "Connection failed", { duration: 7000 })
      }
    } catch (error) {
      console.error("Failed to test connection:", error)
      toast.error("Connection test failed")
    } finally {
      setLoading(false)
    }
  }

  const getDbTypeLabel = (type: string) => {
    return DATABASE_TYPES.find((db) => db.value === type)?.label || type
  }

  return (
    <div className="p-6 space-y-6">
      {/* Show all saved databases */}
      {savedDatabases.length > 0 && (
        <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-6">
          {savedDatabases.map((db) => (
            <Card key={db.id} className="bg-card border-border">
              <CardHeader className="flex flex-row items-start justify-between space-y-0">
                <div className="flex items-center gap-3">
                  <Database className="h-5 w-5 text-primary" />
                  <div>
                    <CardTitle className="text-lg">{db.name}</CardTitle>
                    <CardDescription>
                      {db.host}:{db.port}
                    </CardDescription>
                  </div>
                </div>
                <Button
                  variant="ghost"
                  size="sm"
                  onClick={() => handleDelete(db.id)}
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
                    <p className="font-medium">{getDbTypeLabel(db.type)}</p>
                  </div>
                  <div>
                    <span className="text-muted-foreground">Database:</span>
                    <p className="font-medium">{db.databaseName}</p>
                  </div>
                </div>
                <div className="flex gap-4 mt-4">
                  <Button
                    onClick={() => handleTestConnection(db.id)}
                    disabled={loading}
                    variant="outline"
                    className="flex-1 border-border"
                  >
                    {loading ? <Loader2 className="h-4 w-4 animate-spin mr-2" /> : null}
                    Test Connection
                  </Button>
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      )}

      {/* Add new database form */}
      {savedDatabases.length < MAX_DATABASES ? (
        <Card className="bg-card border-border">
          <CardHeader>
            <CardTitle>Add New Database</CardTitle>
            <CardDescription>
              You can add up to {MAX_DATABASES} databases. ({MAX_DATABASES - savedDatabases.length} remaining)
            </CardDescription>
          </CardHeader>
          <CardContent className="space-y-6">
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

            <div className="space-y-2">
              <Label htmlFor="name">Connection Name *</Label>
              <Input id="name" name="name" placeholder="My Database" value={formData.name} onChange={handleChange} />
            </div>

            <div className="space-y-2">
              <Label htmlFor="host">Host *</Label>
              <Input id="host" name="host" placeholder="localhost" value={formData.host} onChange={handleChange} />
            </div>

            <div className="space-y-2">
              <Label htmlFor="port">Port *</Label>
              <Input id="port" name="port" placeholder="3306" value={formData.port} onChange={handleChange} />
            </div>

            <div className="space-y-2">
              <Label htmlFor="database">Database Name *</Label>
              <Input id="database" name="database" placeholder="my_database" value={formData.database} onChange={handleChange} />
            </div>

            <div className="space-y-2">
              <Label htmlFor="username">Username *</Label>
              <Input id="username" name="username" placeholder="root" value={formData.username} onChange={handleChange} />
            </div>

            <div className="space-y-2">
              <Label htmlFor="password">Password *</Label>
              <Input id="password" name="password" type="password" placeholder="password" value={formData.password} onChange={handleChange} />
            </div>

            <div className="flex gap-4 pt-4">
              <Button onClick={handleSave} disabled={loading} className="bg-primary hover:bg-secondary text-primary-foreground">
                {loading ? <Loader2 className="h-4 w-4 animate-spin mr-2" /> : null}
                Save Configuration
              </Button>
            </div>
          </CardContent>
        </Card>
      ) : (
        <Alert className="bg-yellow-50 dark:bg-yellow-950 border-yellow-200 dark:border-yellow-800">
          <AlertCircle className="h-4 w-4 text-yellow-600 dark:text-yellow-400" />
          <AlertDescription className="text-yellow-800 dark:text-yellow-200">
            You have reached the maximum number of databases ({MAX_DATABASES}). Delete one to add another.
          </AlertDescription>
        </Alert>
      )}
    </div>
  )
}
