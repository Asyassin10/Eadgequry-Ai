"use client"

import { useState } from "react"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import {
  ArrowLeft,
  BookOpen,
  Database,
  MessageSquare,
  Settings,
  Shield,
  Zap,
  CheckCircle,
  ChevronRight,
  Search,
  Menu,
  X
} from "lucide-react"
import Link from "next/link"

export default function DocsPage() {
  const [sidebarOpen, setSidebarOpen] = useState(false)
  const [activeSection, setActiveSection] = useState("introduction")

  const navigation = [
    {
      title: "Getting Started",
      items: [
        { id: "introduction", label: "Introduction", href: "#introduction" },
        { id: "quick-start", label: "Quick Start", href: "#quick-start" },
        { id: "installation", label: "Installation", href: "#installation" },
      ]
    },
    {
      title: "Use Cases",
      items: [
        { id: "use-cases", label: "Overview", href: "#use-cases" },
        { id: "bi-chatbot", label: "BI Chatbot", href: "#bi-chatbot" },
        { id: "dashboard-builder", label: "Dashboard Builder", href: "#dashboard-builder" },
        { id: "internal-tools", label: "Internal Tools", href: "#internal-tools" },
      ]
    },
    {
      title: "Core Concepts",
      items: [
        { id: "how-it-works", label: "How It Works", href: "#how-it-works" },
        { id: "nl2sql", label: "Natural Language to SQL", href: "#nl2sql" },
        { id: "schema-analysis", label: "Schema Analysis", href: "#schema-analysis" },
      ]
    },
    {
      title: "Configuration",
      items: [
        { id: "database-setup", label: "Database Setup", href: "#database-setup" },
        { id: "ai-providers", label: "AI Providers", href: "#ai-providers" },
        { id: "api-keys", label: "API Key Management", href: "#api-keys" },
      ]
    },
    {
      title: "Security",
      items: [
        { id: "security-overview", label: "Security Overview", href: "#security-overview" },
        { id: "encryption", label: "Encryption", href: "#encryption" },
        { id: "access-control", label: "Access Control", href: "#access-control" },
      ]
    },
    {
      title: "API Reference",
      items: [
        { id: "rest-api", label: "REST API", href: "#rest-api" },
        { id: "authentication", label: "Authentication", href: "#authentication" },
        { id: "endpoints", label: "Endpoints", href: "#endpoints" },
      ]
    }
  ]

  const onThisPage = [
    { label: "Introduction", href: "#introduction" },
    { label: "Three Main Use Cases", href: "#use-cases" },
    { label: "How It Works", href: "#how-it-works" },
    { label: "Getting Started", href: "#quick-start" },
    { label: "Database Setup", href: "#database-setup" },
    { label: "Security", href: "#security-overview" },
  ]

  return (
    <div className="min-h-screen bg-background">
      {/* Navigation */}
      <nav className="border-b bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60 sticky top-0 z-50">
        <div className="container mx-auto px-4 py-4">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-6">
              <div className="flex items-center gap-2">
                <img src="/logo.png" alt="EadgeQuery Logo" className="w-8 h-8" />
                <span className="text-2xl font-bold bg-gradient-to-r from-primary to-blue-600 bg-clip-text text-transparent">
                  EadgeQuery
                </span>
                <span className="text-xs text-muted-foreground ml-2">v0.1</span>
              </div>
              <span className="text-sm text-muted-foreground hidden md:block">/ Documentation</span>
            </div>
            <div className="flex items-center gap-4">
              <div className="hidden md:flex items-center gap-2 px-3 py-2 bg-muted rounded-lg">
                <Search className="w-4 h-4 text-muted-foreground" />
                <input
                  type="text"
                  placeholder="Search docs..."
                  className="bg-transparent border-none outline-none text-sm w-48"
                />
              </div>
              <Link href="/">
                <Button variant="ghost" size="sm">
                  <ArrowLeft className="w-4 h-4 mr-2" />
                  Home
                </Button>
              </Link>
              <Link href="/register">
                <Button size="sm">Get Started</Button>
              </Link>
              <button
                onClick={() => setSidebarOpen(!sidebarOpen)}
                className="md:hidden"
              >
                {sidebarOpen ? <X className="w-5 h-5" /> : <Menu className="w-5 h-5" />}
              </button>
            </div>
          </div>
        </div>
      </nav>

      <div className="container mx-auto px-4 py-8">
        <div className="flex gap-8">
          {/* Sidebar Navigation */}
          <aside className={`${sidebarOpen ? 'block' : 'hidden'} md:block w-64 flex-shrink-0 sticky top-24 h-[calc(100vh-6rem)] overflow-y-auto`}>
            <nav className="space-y-6">
              {navigation.map((section, idx) => (
                <div key={idx}>
                  <h3 className="font-semibold text-sm mb-2 text-foreground/70">{section.title}</h3>
                  <ul className="space-y-1">
                    {section.items.map((item) => (
                      <li key={item.id}>
                        <a
                          href={item.href}
                          onClick={() => setActiveSection(item.id)}
                          className={`block px-3 py-2 text-sm rounded-md transition-colors ${
                            activeSection === item.id
                              ? 'bg-primary text-primary-foreground'
                              : 'text-muted-foreground hover:text-foreground hover:bg-muted'
                          }`}
                        >
                          {item.label}
                        </a>
                      </li>
                    ))}
                  </ul>
                </div>
              ))}
            </nav>
          </aside>

          {/* Main Content */}
          <main className="flex-1 max-w-4xl">
            {/* Introduction Section */}
            <section id="introduction" className="mb-16">
              <h1 className="text-4xl font-bold mb-4">EadgeQuery Documentation</h1>
              <p className="text-xl text-muted-foreground mb-6">
                Transform complex SQL queries into simple conversations. This guide will help you get started with EadgeQuery and unlock the power of AI-driven database querying.
              </p>
              <Card className="border-l-4 border-l-primary">
                <CardContent className="pt-6">
                  <div className="flex items-start gap-4">
                    <BookOpen className="w-6 h-6 text-primary flex-shrink-0 mt-1" />
                    <div>
                      <h3 className="font-semibold mb-2">What You'll Learn</h3>
                      <ul className="space-y-2 text-sm text-muted-foreground">
                        <li className="flex items-center gap-2">
                          <CheckCircle className="w-4 h-4 text-green-500" />
                          How to connect your database and start querying in minutes
                        </li>
                        <li className="flex items-center gap-2">
                          <CheckCircle className="w-4 h-4 text-green-500" />
                          Best practices for asking effective questions
                        </li>
                        <li className="flex items-center gap-2">
                          <CheckCircle className="w-4 h-4 text-green-500" />
                          Understanding AI providers and API key configuration
                        </li>
                        <li className="flex items-center gap-2">
                          <CheckCircle className="w-4 h-4 text-green-500" />
                          Security best practices for protecting your data
                        </li>
                      </ul>
                    </div>
                  </div>
                </CardContent>
              </Card>
            </section>

            {/* Three Main Use Cases */}
            <section id="use-cases" className="mb-16">
              <h2 className="text-3xl font-bold mb-6">Three Main Use Cases</h2>
              <p className="text-muted-foreground mb-6">
                EadgeQuery powers a variety of data access patterns, from customer-facing analytics to internal business intelligence.
              </p>

              {/* Customer-facing BI Chatbot */}
              <div id="bi-chatbot" className="mb-8">
                <h3 className="text-2xl font-semibold mb-4 flex items-center gap-2">
                  <MessageSquare className="w-6 h-6 text-primary" />
                  1. Customer-Facing BI Chatbot
                </h3>
                <Card>
                  <CardContent className="pt-6">
                    <p className="text-muted-foreground mb-4">
                      Embed an AI-powered analytics chatbot directly into your product, allowing customers to explore their data using natural language.
                    </p>
                    <div className="bg-muted p-4 rounded-lg mb-4">
                      <p className="text-sm font-mono">
                        <span className="text-primary">Customer:</span> "What were my top-selling products last month?"
                        <br />
                        <span className="text-green-600">EadgeQuery:</span> Returns a formatted table showing products ranked by sales
                      </p>
                    </div>
                    <h4 className="font-semibold mb-2">Key Benefits:</h4>
                    <ul className="space-y-2 text-sm text-muted-foreground">
                      <li className="flex items-start gap-2">
                        <ChevronRight className="w-4 h-4 text-primary mt-1 flex-shrink-0" />
                        <span><strong>Self-service analytics:</strong> Reduce support tickets by 40%</span>
                      </li>
                      <li className="flex items-start gap-2">
                        <ChevronRight className="w-4 h-4 text-primary mt-1 flex-shrink-0" />
                        <span><strong>Increased engagement:</strong> Users spend 3x longer exploring their data</span>
                      </li>
                      <li className="flex items-start gap-2">
                        <ChevronRight className="w-4 h-4 text-primary mt-1 flex-shrink-0" />
                        <span><strong>Product differentiation:</strong> Stand out with AI-powered insights</span>
                      </li>
                    </ul>
                  </CardContent>
                </Card>
              </div>

              {/* Dashboard Builder */}
              <div id="dashboard-builder" className="mb-8">
                <h3 className="text-2xl font-semibold mb-4 flex items-center gap-2">
                  <Database className="w-6 h-6 text-primary" />
                  2. Dashboard Builder
                </h3>
                <Card>
                  <CardContent className="pt-6">
                    <p className="text-muted-foreground mb-4">
                      Generate custom dashboards and reports on-the-fly by asking questions instead of building complex queries.
                    </p>
                    <div className="bg-muted p-4 rounded-lg mb-4">
                      <p className="text-sm font-mono">
                        <span className="text-primary">User:</span> "Show me revenue by region for Q3 with year-over-year comparison"
                        <br />
                        <span className="text-green-600">EadgeQuery:</span> Generates SQL, executes query, and formats results as a comparative table
                      </p>
                    </div>
                    <h4 className="font-semibold mb-2">Use Cases:</h4>
                    <ul className="space-y-2 text-sm text-muted-foreground">
                      <li className="flex items-start gap-2">
                        <ChevronRight className="w-4 h-4 text-primary mt-1 flex-shrink-0" />
                        <span><strong>Executive reporting:</strong> C-suite access to real-time metrics</span>
                      </li>
                      <li className="flex items-start gap-2">
                        <ChevronRight className="w-4 h-4 text-primary mt-1 flex-shrink-0" />
                        <span><strong>Ad-hoc analysis:</strong> Explore data without waiting for data teams</span>
                      </li>
                      <li className="flex items-start gap-2">
                        <ChevronRight className="w-4 h-4 text-primary mt-1 flex-shrink-0" />
                        <span><strong>Dynamic dashboards:</strong> Update visualizations with simple questions</span>
                      </li>
                    </ul>
                  </CardContent>
                </Card>
              </div>

              {/* Internal Tools */}
              <div id="internal-tools" className="mb-8">
                <h3 className="text-2xl font-semibold mb-4 flex items-center gap-2">
                  <Zap className="w-6 h-6 text-primary" />
                  3. Internal Tools & Operations
                </h3>
                <Card>
                  <CardContent className="pt-6">
                    <p className="text-muted-foreground mb-4">
                      Empower your operations, support, and sales teams with instant database access through natural language.
                    </p>
                    <div className="bg-muted p-4 rounded-lg mb-4">
                      <p className="text-sm font-mono">
                        <span className="text-primary">Support Agent:</span> "Find all orders for customer ID 12345 in the last 6 months"
                        <br />
                        <span className="text-green-600">EadgeQuery:</span> Returns complete order history with status and totals
                      </p>
                    </div>
                    <h4 className="font-semibold mb-2">Team Applications:</h4>
                    <ul className="space-y-2 text-sm text-muted-foreground">
                      <li className="flex items-start gap-2">
                        <ChevronRight className="w-4 h-4 text-primary mt-1 flex-shrink-0" />
                        <span><strong>Customer Support:</strong> Quickly lookup customer data and transaction history</span>
                      </li>
                      <li className="flex items-start gap-2">
                        <ChevronRight className="w-4 h-4 text-primary mt-1 flex-shrink-0" />
                        <span><strong>Sales Operations:</strong> Track pipeline, quotas, and performance metrics</span>
                      </li>
                      <li className="flex items-start gap-2">
                        <ChevronRight className="w-4 h-4 text-primary mt-1 flex-shrink-0" />
                        <span><strong>Product Teams:</strong> Analyze feature usage and user behavior</span>
                      </li>
                    </ul>
                  </CardContent>
                </Card>
              </div>
            </section>

            {/* How It Works */}
            <section id="how-it-works" className="mb-16">
              <h2 className="text-3xl font-bold mb-6">How It Works</h2>
              <p className="text-muted-foreground mb-6">
                EadgeQuery uses advanced AI models to understand your questions and convert them into accurate SQL queries.
              </p>

              <div className="space-y-6">
                {/* Step 1 */}
                <Card>
                  <CardHeader>
                    <div className="flex items-center gap-3">
                      <div className="w-8 h-8 rounded-full bg-primary text-primary-foreground flex items-center justify-center font-bold">
                        1
                      </div>
                      <CardTitle>Schema Analysis</CardTitle>
                    </div>
                  </CardHeader>
                  <CardContent>
                    <p className="text-muted-foreground mb-3">
                      When you connect a database, EadgeQuery automatically analyzes the schema to understand:
                    </p>
                    <ul className="list-disc list-inside space-y-1 text-sm text-muted-foreground ml-4">
                      <li>Table names and their relationships</li>
                      <li>Column names, types, and constraints</li>
                      <li>Foreign key relationships</li>
                      <li>Primary keys and indexes</li>
                    </ul>
                  </CardContent>
                </Card>

                {/* Step 2 */}
                <Card>
                  <CardHeader>
                    <div className="flex items-center gap-3">
                      <div className="w-8 h-8 rounded-full bg-primary text-primary-foreground flex items-center justify-center font-bold">
                        2
                      </div>
                      <CardTitle>Natural Language Understanding</CardTitle>
                    </div>
                  </CardHeader>
                  <CardContent>
                    <p className="text-muted-foreground mb-3">
                      The AI processes your question to identify:
                    </p>
                    <ul className="list-disc list-inside space-y-1 text-sm text-muted-foreground ml-4">
                      <li>What data you're looking for (SELECT)</li>
                      <li>Which tables and columns to use (FROM, JOIN)</li>
                      <li>Filters and conditions (WHERE)</li>
                      <li>Aggregations and grouping (GROUP BY, COUNT, SUM)</li>
                      <li>Sorting and limits (ORDER BY, LIMIT)</li>
                    </ul>
                  </CardContent>
                </Card>

                {/* Step 3 */}
                <Card>
                  <CardHeader>
                    <div className="flex items-center gap-3">
                      <div className="w-8 h-8 rounded-full bg-primary text-primary-foreground flex items-center justify-center font-bold">
                        3
                      </div>
                      <CardTitle>SQL Generation</CardTitle>
                    </div>
                  </CardHeader>
                  <CardContent>
                    <p className="text-muted-foreground mb-3">
                      The AI constructs a valid SQL query using EXACT column and table names from your schema:
                    </p>
                    <div className="bg-muted p-3 rounded-lg text-sm font-mono overflow-x-auto">
                      <pre>{`SELECT
  customers.region,
  SUM(orders.amount) as total_sales
FROM orders
INNER JOIN customers
  ON orders.customer_id = customers.id
WHERE orders.order_date >= '2024-07-01'
  AND orders.order_date <= '2024-09-30'
GROUP BY customers.region
ORDER BY total_sales DESC;`}</pre>
                    </div>
                  </CardContent>
                </Card>

                {/* Step 4 */}
                <Card>
                  <CardHeader>
                    <div className="flex items-center gap-3">
                      <div className="w-8 h-8 rounded-full bg-primary text-primary-foreground flex items-center justify-center font-bold">
                        4
                      </div>
                      <CardTitle>Execution & Results</CardTitle>
                    </div>
                  </CardHeader>
                  <CardContent>
                    <p className="text-muted-foreground mb-3">
                      The query is executed with security measures:
                    </p>
                    <ul className="list-disc list-inside space-y-1 text-sm text-muted-foreground ml-4">
                      <li>Read-only access (only SELECT queries allowed)</li>
                      <li>Query timeout limits (60 seconds)</li>
                      <li>Result size limits (50 rows displayed by default)</li>
                      <li>Results formatted as markdown tables</li>
                      <li>SQL query shown for transparency</li>
                    </ul>
                  </CardContent>
                </Card>
              </div>
            </section>

            {/* Quick Start */}
            <section id="quick-start" className="mb-16">
              <h2 className="text-3xl font-bold mb-6">Quick Start</h2>
              <p className="text-muted-foreground mb-6">
                Get up and running with EadgeQuery in just a few minutes.
              </p>

              <div className="space-y-4">
                <Card>
                  <CardHeader>
                    <CardTitle className="text-lg">Step 1: Create an Account</CardTitle>
                  </CardHeader>
                  <CardContent>
                    <p className="text-sm text-muted-foreground mb-3">
                      Sign up for a free account at EadgeQuery. No credit card required.
                    </p>
                    <Link href="/register">
                      <Button>Create Free Account</Button>
                    </Link>
                  </CardContent>
                </Card>

                <Card>
                  <CardHeader>
                    <CardTitle className="text-lg">Step 2: Connect Your Database</CardTitle>
                  </CardHeader>
                  <CardContent>
                    <p className="text-sm text-muted-foreground mb-3">
                      Provide your database connection details. We support:
                    </p>
                    <div className="flex flex-wrap gap-2 mb-3">
                      <span className="px-3 py-1 bg-primary/10 text-primary rounded-full text-sm font-medium">MySQL</span>
                      <span className="px-3 py-1 bg-primary/10 text-primary rounded-full text-sm font-medium">PostgreSQL</span>
                      <span className="px-3 py-1 bg-primary/10 text-primary rounded-full text-sm font-medium">Oracle</span>
                      <span className="px-3 py-1 bg-primary/10 text-primary rounded-full text-sm font-medium">SQL Server</span>
                      <span className="px-3 py-1 bg-primary/10 text-primary rounded-full text-sm font-medium">H2</span>
                    </div>
                  </CardContent>
                </Card>

                <Card>
                  <CardHeader>
                    <CardTitle className="text-lg">Step 3: Choose Your AI Provider</CardTitle>
                  </CardHeader>
                  <CardContent>
                    <p className="text-sm text-muted-foreground mb-3">
                      Select how you want to power your queries:
                    </p>
                    <ul className="space-y-2 text-sm text-muted-foreground">
                      <li className="flex items-center gap-2">
                        <CheckCircle className="w-4 h-4 text-green-500" />
                        <strong>Demo AI:</strong> Free tier with 30 queries/day
                      </li>
                      <li className="flex items-center gap-2">
                        <CheckCircle className="w-4 h-4 text-green-500" />
                        <strong>Your API Key:</strong> Bring your own Claude or OpenAI key for 100 queries/day
                      </li>
                    </ul>
                  </CardContent>
                </Card>

                <Card>
                  <CardHeader>
                    <CardTitle className="text-lg">Step 4: Start Asking Questions</CardTitle>
                  </CardHeader>
                  <CardContent>
                    <p className="text-sm text-muted-foreground mb-3">
                      Type your question in plain English:
                    </p>
                    <div className="bg-muted p-3 rounded-lg text-sm space-y-2">
                      <p className="font-mono">"What are my top 10 customers by revenue?"</p>
                      <p className="font-mono">"Show me orders from last month"</p>
                      <p className="font-mono">"Find products with low inventory"</p>
                    </div>
                  </CardContent>
                </Card>
              </div>
            </section>

            {/* Database Setup */}
            <section id="database-setup" className="mb-16">
              <h2 className="text-3xl font-bold mb-6">Database Setup</h2>
              <p className="text-muted-foreground mb-6">
                Configure your database connection with read-only access for maximum security.
              </p>

              <Card className="mb-6">
                <CardHeader>
                  <CardTitle>Recommended: Create a Read-Only User</CardTitle>
                </CardHeader>
                <CardContent>
                  <p className="text-sm text-muted-foreground mb-3">
                    For security, create a dedicated read-only database user for EadgeQuery:
                  </p>
                  <div className="bg-muted p-3 rounded-lg text-sm font-mono overflow-x-auto">
                    <pre>{`-- MySQL Example
CREATE USER 'eadgequery_readonly'@'%' IDENTIFIED BY 'secure_password';
GRANT SELECT ON your_database.* TO 'eadgequery_readonly'@'%';
FLUSH PRIVILEGES;`}</pre>
                  </div>
                </CardContent>
              </Card>

              <Card>
                <CardHeader>
                  <CardTitle>Connection String Format</CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="space-y-3">
                    <div>
                      <h4 className="font-semibold text-sm mb-2">MySQL / MariaDB</h4>
                      <code className="block bg-muted p-2 rounded text-sm">
                        jdbc:mysql://hostname:3306/database_name
                      </code>
                    </div>
                    <div>
                      <h4 className="font-semibold text-sm mb-2">PostgreSQL</h4>
                      <code className="block bg-muted p-2 rounded text-sm">
                        jdbc:postgresql://hostname:5432/database_name
                      </code>
                    </div>
                    <div>
                      <h4 className="font-semibold text-sm mb-2">Oracle</h4>
                      <code className="block bg-muted p-2 rounded text-sm">
                        jdbc:oracle:thin:@hostname:1521:SID
                      </code>
                    </div>
                    <div>
                      <h4 className="font-semibold text-sm mb-2">SQL Server</h4>
                      <code className="block bg-muted p-2 rounded text-sm">
                        jdbc:sqlserver://hostname:1433;databaseName=database_name
                      </code>
                    </div>
                  </div>
                </CardContent>
              </Card>
            </section>

            {/* AI Providers */}
            <section id="ai-providers" className="mb-16">
              <h2 className="text-3xl font-bold mb-6">AI Providers</h2>
              <p className="text-muted-foreground mb-6">
                EadgeQuery supports multiple AI providers to power your natural language queries.
              </p>

              <div className="space-y-4">
                <Card>
                  <CardHeader>
                    <CardTitle className="text-lg">Demo AI (OpenRouter)</CardTitle>
                    <CardDescription>Free tier - 30 queries per day</CardDescription>
                  </CardHeader>
                  <CardContent>
                    <p className="text-sm text-muted-foreground mb-3">
                      Our default AI provider, perfect for testing and small-scale usage. No API key required.
                    </p>
                    <ul className="space-y-1 text-sm text-muted-foreground">
                      <li>✓ No setup required</li>
                      <li>✓ 30 queries per day limit</li>
                      <li>✓ Good for testing and demos</li>
                    </ul>
                  </CardContent>
                </Card>

                <Card>
                  <CardHeader>
                    <CardTitle className="text-lg">Claude (Anthropic)</CardTitle>
                    <CardDescription>Bring your own API key - 100 queries per day</CardDescription>
                  </CardHeader>
                  <CardContent>
                    <p className="text-sm text-muted-foreground mb-3">
                      Use Claude's powerful reasoning capabilities for complex queries and accurate SQL generation.
                    </p>
                    <ul className="space-y-1 text-sm text-muted-foreground mb-3">
                      <li>✓ Excellent at complex queries</li>
                      <li>✓ Strong reasoning capabilities</li>
                      <li>✓ 100 queries per day with your key</li>
                    </ul>
                    <Button variant="outline" size="sm">
                      <a href="https://console.anthropic.com/" target="_blank" rel="noopener noreferrer">
                        Get Claude API Key
                      </a>
                    </Button>
                  </CardContent>
                </Card>

                <Card>
                  <CardHeader>
                    <CardTitle className="text-lg">OpenAI (GPT-4)</CardTitle>
                    <CardDescription>Bring your own API key - 100 queries per day</CardDescription>
                  </CardHeader>
                  <CardContent>
                    <p className="text-sm text-muted-foreground mb-3">
                      Leverage GPT-4's natural language understanding for intuitive database querying.
                    </p>
                    <ul className="space-y-1 text-sm text-muted-foreground mb-3">
                      <li>✓ Natural language understanding</li>
                      <li>✓ Wide range of query types</li>
                      <li>✓ 100 queries per day with your key</li>
                    </ul>
                    <Button variant="outline" size="sm">
                      <a href="https://platform.openai.com/api-keys" target="_blank" rel="noopener noreferrer">
                        Get OpenAI API Key
                      </a>
                    </Button>
                  </CardContent>
                </Card>
              </div>
            </section>

            {/* Security Overview */}
            <section id="security-overview" className="mb-16">
              <h2 className="text-3xl font-bold mb-6">Security Overview</h2>
              <p className="text-muted-foreground mb-6">
                EadgeQuery implements enterprise-grade security measures to protect your data.
              </p>

              <div className="grid md:grid-cols-2 gap-4">
                <Card>
                  <CardHeader>
                    <Shield className="w-8 h-8 text-primary mb-2" />
                    <CardTitle>Read-Only Access</CardTitle>
                  </CardHeader>
                  <CardContent>
                    <p className="text-sm text-muted-foreground">
                      Only SELECT queries are allowed. No INSERT, UPDATE, DELETE, or DROP operations possible.
                    </p>
                  </CardContent>
                </Card>

                <Card>
                  <CardHeader>
                    <Shield className="w-8 h-8 text-primary mb-2" />
                    <CardTitle>AES-256 Encryption</CardTitle>
                  </CardHeader>
                  <CardContent>
                    <p className="text-sm text-muted-foreground">
                      All API keys and sensitive data encrypted at rest using industry-standard AES-256 encryption.
                    </p>
                  </CardContent>
                </Card>

                <Card>
                  <CardHeader>
                    <Shield className="w-8 h-8 text-primary mb-2" />
                    <CardTitle>TLS/SSL</CardTitle>
                  </CardHeader>
                  <CardContent>
                    <p className="text-sm text-muted-foreground">
                      All data in transit protected with TLS/SSL encryption for both API and database connections.
                    </p>
                  </CardContent>
                </Card>

                <Card>
                  <CardHeader>
                    <Shield className="w-8 h-8 text-primary mb-2" />
                    <CardTitle>Zero Data Storage</CardTitle>
                  </CardHeader>
                  <CardContent>
                    <p className="text-sm text-muted-foreground">
                      We never store your actual database data. Only query history and metadata are saved.
                    </p>
                  </CardContent>
                </Card>
              </div>
            </section>

            {/* API Reference */}
            <section id="rest-api" className="mb-16">
              <h2 className="text-3xl font-bold mb-6">REST API Reference</h2>
              <p className="text-muted-foreground mb-6">
                Integrate EadgeQuery into your applications using our REST API.
              </p>

              <Card className="border-l-4 border-l-yellow-500 mb-6">
                <CardContent className="pt-6">
                  <div className="flex items-start gap-3">
                    <span className="text-2xl">⚠️</span>
                    <div>
                      <h4 className="font-semibold mb-1">Coming in v1.0</h4>
                      <p className="text-sm text-muted-foreground">
                        The REST API is currently in development and will be available in version 1.0.
                        Check back soon for complete API documentation.
                      </p>
                    </div>
                  </div>
                </CardContent>
              </Card>

              <Card>
                <CardHeader>
                  <CardTitle>Endpoint Preview</CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="space-y-4">
                    <div>
                      <div className="flex items-center gap-2 mb-2">
                        <span className="px-2 py-1 bg-green-100 text-green-700 text-xs font-semibold rounded">POST</span>
                        <code className="text-sm">/api/v1/query</code>
                      </div>
                      <p className="text-sm text-muted-foreground">Execute a natural language query</p>
                    </div>

                    <div>
                      <div className="flex items-center gap-2 mb-2">
                        <span className="px-2 py-1 bg-blue-100 text-blue-700 text-xs font-semibold rounded">GET</span>
                        <code className="text-sm">/api/v1/history</code>
                      </div>
                      <p className="text-sm text-muted-foreground">Retrieve query history</p>
                    </div>

                    <div>
                      <div className="flex items-center gap-2 mb-2">
                        <span className="px-2 py-1 bg-blue-100 text-blue-700 text-xs font-semibold rounded">GET</span>
                        <code className="text-sm">/api/v1/databases</code>
                      </div>
                      <p className="text-sm text-muted-foreground">List connected databases</p>
                    </div>
                  </div>
                </CardContent>
              </Card>
            </section>

            {/* Footer CTA */}
            <Card className="border-2 border-primary">
              <CardContent className="pt-6 text-center">
                <h3 className="text-2xl font-bold mb-4">Ready to Get Started?</h3>
                <p className="text-muted-foreground mb-6">
                  Start querying your databases with natural language today
                </p>
                <div className="flex gap-4 justify-center">
                  <Link href="/register">
                    <Button size="lg">Create Free Account</Button>
                  </Link>
                  <Link href="/blog">
                    <Button size="lg" variant="outline">Read Our Blog</Button>
                  </Link>
                </div>
              </CardContent>
            </Card>
          </main>

          {/* Right Sidebar - On This Page */}
          <aside className="hidden xl:block w-56 flex-shrink-0 sticky top-24 h-[calc(100vh-6rem)] overflow-y-auto">
            <div className="text-sm">
              <h3 className="font-semibold mb-3 text-foreground/70">On This Page</h3>
              <nav>
                <ul className="space-y-2">
                  {onThisPage.map((item, idx) => (
                    <li key={idx}>
                      <a
                        href={item.href}
                        className="text-muted-foreground hover:text-foreground transition-colors block py-1"
                      >
                        {item.label}
                      </a>
                    </li>
                  ))}
                </ul>
              </nav>
            </div>
          </aside>
        </div>
      </div>

      {/* Footer */}
      <footer className="bg-muted/50 border-t mt-20">
        <div className="container mx-auto px-4 py-8 text-center text-sm text-muted-foreground">
          <div className="flex items-center justify-center gap-2 mb-2">
            <img src="/logo.png" alt="EadgeQuery Logo" className="w-5 h-5" />
            <span className="font-semibold">EadgeQuery</span>
            <span className="text-xs">v0.1</span>
          </div>
          <p>© 2024 EadgeQuery. All rights reserved.</p>
        </div>
      </footer>
    </div>
  )
}
