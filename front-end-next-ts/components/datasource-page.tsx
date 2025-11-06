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

// Define database types
const DATABASE_TYPES = [
  { value: "mysql", label: "MySQL", defaultPort: "3306" },
  { value: "postgresql", label: "PostgreSQL", defaultPort: "5432" },
  { value: "sqlserver", label: "SQL Server", defaultPort: "1433" },
  { value: "sqlanywhere", label: "SQL Anywhere", defaultPort: "2638" },
  { value: "sqlite", label: "SQLite", defaultPort: "" },
  { value: "snowflake", label: "Snowflake", defaultPort: "" },
  { value: "oracle", label: "Oracle", defaultPort: "1521" },
  { value: "bigquery", label: "BigQuery", defaultPort: "" },
  { value: "mariadb", label: "MariaDB", defaultPort: "3306" },
  { value: "redshift", label: "Redshift", defaultPort: "5439" },
]

export function DatasourcePage() {
  const [dbType, setDbType] = useState("mysql")
  const [formData, setFormData] = useState<Record<string, string>>({
    host: "localhost",
    port: "3306",
    database: "",
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

  const handleDbTypeChange = (value: string) => {
    setDbType(value)
    const selectedDb = DATABASE_TYPES.find((db) => db.value === value)

    // Reset form with appropriate defaults for the selected database
    const newFormData: Record<string, string> = {
      knowledge: formData.knowledge, // Keep knowledge field
    }

    // Set default fields based on database type
    if (value === "sqlite") {
      newFormData.filePath = ""
    } else if (value === "snowflake") {
      newFormData.account = ""
      newFormData.username = ""
      newFormData.password = ""
      newFormData.database = ""
      newFormData.schema = ""
      newFormData.warehouse = ""
      newFormData.role = ""
    } else if (value === "bigquery") {
      newFormData.projectId = ""
      newFormData.dataset = ""
      newFormData.serviceAccountJson = ""
    } else if (value === "oracle") {
      newFormData.host = "localhost"
      newFormData.port = selectedDb?.defaultPort || "1521"
      newFormData.serviceName = ""
      newFormData.username = ""
      newFormData.password = ""
    } else {
      // Standard fields for MySQL, PostgreSQL, SQL Server, MariaDB, Redshift, SQL Anywhere
      newFormData.host = "localhost"
      newFormData.port = selectedDb?.defaultPort || ""
      newFormData.database = ""
      newFormData.username = ""
      newFormData.password = ""

      // SQL Server specific
      if (value === "sqlserver") {
        newFormData.instance = ""
      }
    }

    setFormData(newFormData)
  }

  const handleSave = () => {
    // Basic validation
    if (dbType === "sqlite") {
      if (!formData.filePath) {
        alert("Please provide the SQLite file path")
        return
      }
    } else if (dbType === "bigquery") {
      if (!formData.projectId || !formData.dataset || !formData.serviceAccountJson) {
        alert("Please fill in all required BigQuery fields")
        return
      }
    } else if (dbType === "snowflake") {
      if (!formData.account || !formData.username || !formData.password || !formData.database) {
        alert("Please fill in all required Snowflake fields")
        return
      }
    } else {
      if (!formData.username || !formData.password || !formData.database) {
        alert("Please fill in all required fields")
        return
      }
    }

    setSavedDatabase({ ...formData, dbType })

    // Reset form
    handleDbTypeChange(dbType)
  }

  const handleAddMore = () => {
    setShowLimitMessage(true)
    setTimeout(() => setShowLimitMessage(false), 4000)
  }

  const handleDelete = () => {
    setSavedDatabase(null)
  }

  const renderConnectionFields = () => {
    switch (dbType) {
      case "sqlite":
        return (
          <div className="space-y-2">
            <Label htmlFor="filePath">Database File Path *</Label>
            <Input
              id="filePath"
              name="filePath"
              placeholder="/path/to/database.db"
              value={formData.filePath || ""}
              onChange={handleChange}
              className="bg-input border-border"
            />
            <p className="text-sm text-muted-foreground">
              Full path to your SQLite database file
            </p>
          </div>
        )

      case "bigquery":
        return (
          <>
            <div className="space-y-2">
              <Label htmlFor="projectId">Project ID *</Label>
              <Input
                id="projectId"
                name="projectId"
                placeholder="my-gcp-project"
                value={formData.projectId || ""}
                onChange={handleChange}
                className="bg-input border-border"
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="dataset">Dataset *</Label>
              <Input
                id="dataset"
                name="dataset"
                placeholder="my_dataset"
                value={formData.dataset || ""}
                onChange={handleChange}
                className="bg-input border-border"
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="serviceAccountJson">Service Account JSON *</Label>
              <textarea
                id="serviceAccountJson"
                name="serviceAccountJson"
                placeholder='{"type": "service_account", "project_id": "...", ...}'
                value={formData.serviceAccountJson || ""}
                onChange={handleChange}
                className="w-full px-3 py-2 rounded-md bg-input border border-border text-foreground placeholder-muted-foreground focus:outline-none focus:ring-2 focus:ring-primary min-h-32 resize-none font-mono text-sm"
              />
              <p className="text-sm text-muted-foreground">
                Paste your GCP service account JSON key
              </p>
            </div>
          </>
        )

      case "snowflake":
        return (
          <>
            <div className="space-y-2">
              <Label htmlFor="account">Account Identifier *</Label>
              <Input
                id="account"
                name="account"
                placeholder="xy12345.us-east-1"
                value={formData.account || ""}
                onChange={handleChange}
                className="bg-input border-border"
              />
              <p className="text-sm text-muted-foreground">
                Format: account_locator.region or account_name
              </p>
            </div>

            <div className="space-y-2">
              <Label htmlFor="username">Username *</Label>
              <Input
                id="username"
                name="username"
                placeholder="Enter your Snowflake username"
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

            <div className="space-y-2">
              <Label htmlFor="database">Database *</Label>
              <Input
                id="database"
                name="database"
                placeholder="MY_DATABASE"
                value={formData.database || ""}
                onChange={handleChange}
                className="bg-input border-border"
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="schema">Schema *</Label>
              <Input
                id="schema"
                name="schema"
                placeholder="PUBLIC"
                value={formData.schema || ""}
                onChange={handleChange}
                className="bg-input border-border"
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="warehouse">Warehouse *</Label>
              <Input
                id="warehouse"
                name="warehouse"
                placeholder="COMPUTE_WH"
                value={formData.warehouse || ""}
                onChange={handleChange}
                className="bg-input border-border"
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="role">Role (Optional)</Label>
              <Input
                id="role"
                name="role"
                placeholder="ACCOUNTADMIN"
                value={formData.role || ""}
                onChange={handleChange}
                className="bg-input border-border"
              />
            </div>
          </>
        )

      case "oracle":
        return (
          <>
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
                placeholder="1521"
                value={formData.port || ""}
                onChange={handleChange}
                className="bg-input border-border"
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="serviceName">Service Name / SID *</Label>
              <Input
                id="serviceName"
                name="serviceName"
                placeholder="ORCL"
                value={formData.serviceName || ""}
                onChange={handleChange}
                className="bg-input border-border"
              />
              <p className="text-sm text-muted-foreground">
                Oracle service name or SID
              </p>
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

      default:
        // Standard fields for MySQL, PostgreSQL, SQL Server, MariaDB, Redshift, SQL Anywhere
        return (
          <>
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

            {dbType === "sqlserver" && (
              <div className="space-y-2">
                <Label htmlFor="instance">Instance (Optional)</Label>
                <Input
                  id="instance"
                  name="instance"
                  placeholder="SQLEXPRESS"
                  value={formData.instance || ""}
                  onChange={handleChange}
                  className="bg-input border-border"
                />
                <p className="text-sm text-muted-foreground">
                  SQL Server instance name (if using named instances)
                </p>
              </div>
            )}

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
                  <CardTitle className="text-lg">{getDbTypeLabel(savedDatabase.dbType)}</CardTitle>
                  <CardDescription>
                    {savedDatabase.dbType === "sqlite"
                      ? savedDatabase.filePath
                      : savedDatabase.dbType === "bigquery"
                        ? `${savedDatabase.projectId} / ${savedDatabase.dataset}`
                        : savedDatabase.dbType === "snowflake"
                          ? savedDatabase.account
                          : `${savedDatabase.host}:${savedDatabase.port}`}
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
                  <span className="text-muted-foreground">Type:</span>
                  <p className="font-medium">{getDbTypeLabel(savedDatabase.dbType)}</p>
                </div>
                {savedDatabase.username && (
                  <div>
                    <span className="text-muted-foreground">Username:</span>
                    <p className="font-medium">{savedDatabase.username}</p>
                  </div>
                )}
                {savedDatabase.database && (
                  <div>
                    <span className="text-muted-foreground">Database:</span>
                    <p className="font-medium">{savedDatabase.database}</p>
                  </div>
                )}
                {savedDatabase.port && (
                  <div>
                    <span className="text-muted-foreground">Port:</span>
                    <p className="font-medium">{savedDatabase.port}</p>
                  </div>
                )}
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

            {/* Knowledge Database - Common for all types */}
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
