"use client"

import { useState } from "react"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import {
  Accordion,
  AccordionContent,
  AccordionItem,
  AccordionTrigger,
} from "@/components/ui/accordion"
import {
  Database,
  MessageSquare,
  Shield,
  Zap,
  CheckCircle,
  ChevronRight,
  Lock,
  Server,
  TrendingUp,
  Users,
  BarChart3,
  Brain,
  Github,
  Twitter,
  Linkedin,
  Mail,
} from "lucide-react"
import Link from "next/link"

export function LandingPage() {
  const [billingPeriod, setBillingPeriod] = useState<"monthly" | "yearly">("monthly")

  return (
    <div className="min-h-screen bg-gradient-to-b from-background to-muted">
      {/* Navigation */}
      <nav className="border-b bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60 sticky top-0 z-50">
        <div className="container mx-auto px-4 py-4">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2">
              <img src="/logo.png" alt="EadgeQuery Logo" className="w-8 h-8" />
              <span className="text-2xl font-bold bg-gradient-to-r from-primary to-blue-600 bg-clip-text text-transparent">
                EadgeQuery
              </span>
              <span className="text-xs text-muted-foreground ml-2">v0.1</span>
            </div>
            <div className="hidden md:flex items-center gap-8">
              <a href="#features" className="text-sm font-medium hover:text-primary transition-colors">
                Features
              </a>
              <a href="#security" className="text-sm font-medium hover:text-primary transition-colors">
                Security
              </a>
              <a href="#pricing" className="text-sm font-medium hover:text-primary transition-colors">
                Pricing
              </a>
              <a href="#faq" className="text-sm font-medium hover:text-primary transition-colors">
                FAQ
              </a>
              <Link href="/docs" className="text-sm font-medium hover:text-primary transition-colors">
                Docs
              </Link>
              <Link href="/blog" className="text-sm font-medium hover:text-primary transition-colors">
                Blog
              </Link>
            </div>
            <div className="flex items-center gap-4">
              <Link href="/login">
                <Button variant="ghost">Sign In</Button>
              </Link>
              <Link href="/register">
                <Button>Get Started</Button>
              </Link>
            </div>
          </div>
        </div>
      </nav>

      {/* Hero Section */}
      <section className="container mx-auto px-4 py-20 md:py-32">
        <div className="grid md:grid-cols-2 gap-12 items-center">
          <div className="space-y-6">
            <div className="inline-block">
              <span className="bg-primary/10 text-primary px-4 py-2 rounded-full text-sm font-semibold">
                🚀 AI-Powered Database Assistant
              </span>
            </div>
            <h1 className="text-4xl md:text-6xl font-bold leading-tight">
              Chat with Your Database in{" "}
              <span className="bg-gradient-to-r from-primary to-blue-600 bg-clip-text text-transparent">
                Plain English
              </span>
            </h1>
            <p className="text-xl text-muted-foreground">
              Transform complex SQL queries into simple conversations. Get instant insights from your data
              without writing a single line of code.
            </p>
            <div className="flex flex-col sm:flex-row gap-4">
              <Link href="/register">
                <Button size="lg" className="text-lg px-8">
                  Start Free Trial
                  <ChevronRight className="ml-2 w-5 h-5" />
                </Button>
              </Link>
              <Link href="/docs">
                <Button size="lg" variant="outline" className="text-lg px-8">
                  View Documentation
                </Button>
              </Link>
            </div>
            <div className="flex items-center gap-6 text-sm text-muted-foreground">
              <div className="flex items-center gap-2">
                <CheckCircle className="w-5 h-5 text-green-500" />
                No credit card required
              </div>
              <div className="flex items-center gap-2">
                <CheckCircle className="w-5 h-5 text-green-500" />
                Free 14-day trial
              </div>
            </div>
          </div>
          <div className="relative">
            <div className="bg-gradient-to-br from-primary/20 to-blue-600/20 rounded-2xl p-8 backdrop-blur">
            <video
              src="/Eadge.mp4"
              autoPlay
              loop
              muted 
              className="w-full h-auto rounded-xl"
            />
            </div>
          </div>
        </div>
      </section>

      {/* Features Section */}
      <section id="features" className="bg-muted/50 py-20">
        <div className="container mx-auto px-4">
          <div className="text-center mb-16">
            <h2 className="text-3xl md:text-5xl font-bold mb-4">
              Powerful Features for Everyone
            </h2>
            <p className="text-xl text-muted-foreground max-w-2xl mx-auto">
              From data analysts to business leaders, EadgeQuery makes database querying accessible to all
            </p>
          </div>
          <div className="grid md:grid-cols-3 gap-8">
            <Card className="border-2 hover:border-primary transition-colors">
              <CardHeader>
                <Brain className="w-12 h-12 text-primary mb-4" />
                <CardTitle>AI-Powered Intelligence</CardTitle>
                <CardDescription>
                  Advanced AI understands your questions, even with typos or unclear language
                </CardDescription>
              </CardHeader>
              <CardContent>
                <ul className="space-y-2 text-sm text-muted-foreground">
                  <li className="flex items-center gap-2">
                    <CheckCircle className="w-4 h-4 text-green-500" />
                    Natural language processing
                  </li>
                  <li className="flex items-center gap-2">
                    <CheckCircle className="w-4 h-4 text-green-500" />
                    Context-aware responses
                  </li>
                  <li className="flex items-center gap-2">
                    <CheckCircle className="w-4 h-4 text-green-500" />
                    Smart error correction
                  </li>
                </ul>
              </CardContent>
            </Card>

            <Card className="border-2 hover:border-primary transition-colors">
              <CardHeader>
                <Database className="w-12 h-12 text-primary mb-4" />
                <CardTitle>Multi-Database Support</CardTitle>
                <CardDescription>
                  Connect to MySQL, PostgreSQL, Oracle, SQL Server, and more
                </CardDescription>
              </CardHeader>
              <CardContent>
                <ul className="space-y-2 text-sm text-muted-foreground">
                  <li className="flex items-center gap-2">
                    <CheckCircle className="w-4 h-4 text-green-500" />
                    5+ database types supported
                  </li>
                  <li className="flex items-center gap-2">
                    <CheckCircle className="w-4 h-4 text-green-500" />
                    Automatic schema detection
                  </li>
                  <li className="flex items-center gap-2">
                    <CheckCircle className="w-4 h-4 text-green-500" />
                    Real-time data sync
                  </li>
                </ul>
              </CardContent>
            </Card>

            <Card className="border-2 hover:border-primary transition-colors">
              <CardHeader>
                <Zap className="w-12 h-12 text-primary mb-4" />
                <CardTitle>Lightning Fast</CardTitle>
                <CardDescription>
                  Get instant answers to your queries with optimized performance
                </CardDescription>
              </CardHeader>
              <CardContent>
                <ul className="space-y-2 text-sm text-muted-foreground">
                  <li className="flex items-center gap-2">
                    <CheckCircle className="w-4 h-4 text-green-500" />
                    Sub-second query generation
                  </li>
                  <li className="flex items-center gap-2">
                    <CheckCircle className="w-4 h-4 text-green-500" />
                    Streaming responses
                  </li>
                  <li className="flex items-center gap-2">
                    <CheckCircle className="w-4 h-4 text-green-500" />
                    Cached results
                  </li>
                </ul>
              </CardContent>
            </Card>

            <Card className="border-2 hover:border-primary transition-colors">
              <CardHeader>
                <BarChart3 className="w-12 h-12 text-primary mb-4" />
                <CardTitle>Beautiful Visualizations</CardTitle>
                <CardDescription>
                  Data presented in easy-to-read tables and charts
                </CardDescription>
              </CardHeader>
              <CardContent>
                <ul className="space-y-2 text-sm text-muted-foreground">
                  <li className="flex items-center gap-2">
                    <CheckCircle className="w-4 h-4 text-green-500" />
                    Markdown tables
                  </li>
                  <li className="flex items-center gap-2">
                    <CheckCircle className="w-4 h-4 text-green-500" />
                    SQL syntax highlighting
                  </li>
                  <li className="flex items-center gap-2">
                    <CheckCircle className="w-4 h-4 text-green-500" />
                    Export to CSV
                  </li>
                </ul>
              </CardContent>
            </Card>

            <Card className="border-2 hover:border-primary transition-colors">
              <CardHeader>
                <Shield className="w-12 h-12 text-primary mb-4" />
                <CardTitle>Enterprise Security</CardTitle>
                <CardDescription>
                  Bank-grade encryption and security for your data
                </CardDescription>
              </CardHeader>
              <CardContent>
                <ul className="space-y-2 text-sm text-muted-foreground">
                  <li className="flex items-center gap-2">
                    <CheckCircle className="w-4 h-4 text-green-500" />
                    AES-256 encryption
                  </li>
                  <li className="flex items-center gap-2">
                    <CheckCircle className="w-4 h-4 text-green-500" />
                    Read-only access
                  </li>
                  <li className="flex items-center gap-2">
                    <CheckCircle className="w-4 h-4 text-green-500" />
                    SOC 2 compliant
                  </li>
                </ul>
              </CardContent>
            </Card>

            <Card className="border-2 hover:border-primary transition-colors">
              <CardHeader>
                <Users className="w-12 h-12 text-primary mb-4" />
                <CardTitle>Team Collaboration</CardTitle>
                <CardDescription>
                  Share insights and queries with your team
                </CardDescription>
              </CardHeader>
              <CardContent>
                <ul className="space-y-2 text-sm text-muted-foreground">
                  <li className="flex items-center gap-2">
                    <CheckCircle className="w-4 h-4 text-green-500" />
                    Conversation history
                  </li>
                  <li className="flex items-center gap-2">
                    <CheckCircle className="w-4 h-4 text-green-500" />
                    Query templates
                  </li>
                  <li className="flex items-center gap-2">
                    <CheckCircle className="w-4 h-4 text-green-500" />
                    Role-based access
                  </li>
                </ul>
              </CardContent>
            </Card>
          </div>
        </div>
      </section>

      {/* How It Works */}
      <section className="py-20">
        <div className="container mx-auto px-4">
          <div className="text-center mb-16">
            <h2 className="text-3xl md:text-5xl font-bold mb-4">
              How It Works
            </h2>
            <p className="text-xl text-muted-foreground max-w-2xl mx-auto">
              Get started in minutes, not hours
            </p>
          </div>
          <div className="grid md:grid-cols-3 gap-8">
            <div className="text-center space-y-4">
              <div className="w-16 h-16 bg-primary/10 rounded-full flex items-center justify-center mx-auto">
                <span className="text-2xl font-bold text-primary">1</span>
              </div>
              <h3 className="text-xl font-semibold">Connect Your Database</h3>
              <p className="text-muted-foreground">
                Securely connect to MySQL, PostgreSQL, Oracle, SQL Server, or any other supported database
              </p>
            </div>
            <div className="text-center space-y-4">
              <div className="w-16 h-16 bg-primary/10 rounded-full flex items-center justify-center mx-auto">
                <span className="text-2xl font-bold text-primary">2</span>
              </div>
              <h3 className="text-xl font-semibold">Ask Questions</h3>
              <p className="text-muted-foreground">
                Type your questions in plain English. Our AI understands what you need, even with typos
              </p>
            </div>
            <div className="text-center space-y-4">
              <div className="w-16 h-16 bg-primary/10 rounded-full flex items-center justify-center mx-auto">
                <span className="text-2xl font-bold text-primary">3</span>
              </div>
              <h3 className="text-xl font-semibold">Get Instant Answers</h3>
              <p className="text-muted-foreground">
                Receive beautifully formatted results with SQL queries you can review and export
              </p>
            </div>
          </div>
        </div>
      </section>

      {/* Supported Databases Section */}
      <section className="bg-muted/50 py-20">
        <div className="container mx-auto px-4">
          <div className="text-center mb-16">
            <h2 className="text-3xl md:text-5xl font-bold mb-4">
              Works with Your Database
            </h2>
            <p className="text-xl text-muted-foreground max-w-2xl mx-auto">
              Connect to the most popular database platforms with seamless integration
            </p>
          </div>

          {/* Database Logos Grid with Animation */}
          <div className="max-w-5xl mx-auto">
            <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-5 gap-8 items-center justify-items-center">
              {/* MySQL */}
              <div className="group flex flex-col items-center gap-4 p-6 rounded-2xl bg-background/50 hover:bg-background hover:shadow-lg transition-all duration-300 hover:scale-110 w-full">
                <div className="w-20 h-20 flex items-center justify-center text-5xl bg-gradient-to-br from-blue-600 to-blue-400 rounded-xl shadow-md group-hover:shadow-xl transition-shadow">
                  🐬
                </div>
                <span className="font-semibold text-sm">MySQL</span>
              </div>

              {/* PostgreSQL */}
              <div className="group flex flex-col items-center gap-4 p-6 rounded-2xl bg-background/50 hover:bg-background hover:shadow-lg transition-all duration-300 hover:scale-110 w-full">
                <div className="w-20 h-20 flex items-center justify-center text-5xl bg-gradient-to-br from-blue-900 to-blue-700 rounded-xl shadow-md group-hover:shadow-xl transition-shadow">
                  🐘
                </div>
                <span className="font-semibold text-sm">PostgreSQL</span>
              </div>

              {/* Oracle */}
              <div className="group flex flex-col items-center gap-4 p-6 rounded-2xl bg-background/50 hover:bg-background hover:shadow-lg transition-all duration-300 hover:scale-110 w-full">
                <div className="w-20 h-20 flex items-center justify-center text-5xl bg-gradient-to-br from-red-600 to-red-400 rounded-xl shadow-md group-hover:shadow-xl transition-shadow">
                  🔴
                </div>
                <span className="font-semibold text-sm">Oracle</span>
              </div>

              {/* SQL Server */}
              <div className="group flex flex-col items-center gap-4 p-6 rounded-2xl bg-background/50 hover:bg-background hover:shadow-lg transition-all duration-300 hover:scale-110 w-full">
                <div className="w-20 h-20 flex items-center justify-center text-5xl bg-gradient-to-br from-gray-700 to-gray-500 rounded-xl shadow-md group-hover:shadow-xl transition-shadow">
                  🗄️
                </div>
                <span className="font-semibold text-sm">SQL Server</span>
              </div>

              {/* H2 Database */}
              <div className="group flex flex-col items-center gap-4 p-6 rounded-2xl bg-background/50 hover:bg-background hover:shadow-lg transition-all duration-300 hover:scale-110 w-full">
                <div className="w-20 h-20 flex items-center justify-center text-5xl bg-gradient-to-br from-green-600 to-green-400 rounded-xl shadow-md group-hover:shadow-xl transition-shadow">
                  💾
                </div>
                <span className="font-semibold text-sm">H2 Database</span>
              </div>
            </div>

            {/* Features below logos */}
            <div className="mt-16 grid md:grid-cols-3 gap-6">
              <Card className="text-center">
                <CardContent className="pt-6">
                  <div className="w-12 h-12 bg-primary/10 rounded-full flex items-center justify-center mx-auto mb-4">
                    <Database className="w-6 h-6 text-primary" />
                  </div>
                  <h4 className="font-semibold mb-2">Auto Schema Detection</h4>
                  <p className="text-sm text-muted-foreground">
                    Automatically analyzes your database structure and relationships
                  </p>
                </CardContent>
              </Card>

              <Card className="text-center">
                <CardContent className="pt-6">
                  <div className="w-12 h-12 bg-primary/10 rounded-full flex items-center justify-center mx-auto mb-4">
                    <Shield className="w-6 h-6 text-primary" />
                  </div>
                  <h4 className="font-semibold mb-2">Secure Connections</h4>
                  <p className="text-sm text-muted-foreground">
                    TLS/SSL encryption for all database connections
                  </p>
                </CardContent>
              </Card>

              <Card className="text-center">
                <CardContent className="pt-6">
                  <div className="w-12 h-12 bg-primary/10 rounded-full flex items-center justify-center mx-auto mb-4">
                    <Zap className="w-6 h-6 text-primary" />
                  </div>
                  <h4 className="font-semibold mb-2">Real-Time Queries</h4>
                  <p className="text-sm text-muted-foreground">
                    Execute queries and get results in real-time with streaming
                  </p>
                </CardContent>
              </Card>
            </div>
          </div>
        </div>
      </section>

      {/* Security Section */}
      <section id="security" className="bg-muted/50 py-20">
        <div className="container mx-auto px-4">
          <div className="text-center mb-16">
            <Shield className="w-16 h-16 text-primary mx-auto mb-6" />
            <h2 className="text-3xl md:text-5xl font-bold mb-4">
              Enterprise-Grade Security
            </h2>
            <p className="text-xl text-muted-foreground max-w-2xl mx-auto">
              Your data security is our top priority. We never store your database data
            </p>
          </div>
          <div className="grid md:grid-cols-2 lg:grid-cols-4 gap-6">
            <Card>
              <CardHeader>
                <Lock className="w-10 h-10 text-primary mb-2" />
                <CardTitle className="text-lg">AES-256 Encryption</CardTitle>
              </CardHeader>
              <CardContent>
                <p className="text-sm text-muted-foreground">
                  All API keys and sensitive data encrypted at rest and in transit
                </p>
              </CardContent>
            </Card>
            <Card>
              <CardHeader>
                <Shield className="w-10 h-10 text-primary mb-2" />
                <CardTitle className="text-lg">Read-Only Access</CardTitle>
              </CardHeader>
              <CardContent>
                <p className="text-sm text-muted-foreground">
                  Only SELECT queries allowed. No data modification possible
                </p>
              </CardContent>
            </Card>
            <Card>
              <CardHeader>
                <Server className="w-10 h-10 text-primary mb-2" />
                <CardTitle className="text-lg">Zero Data Storage</CardTitle>
              </CardHeader>
              <CardContent>
                <p className="text-sm text-muted-foreground">
                  We don't store your database data. Only query history is saved
                </p>
              </CardContent>
            </Card>
            <Card>
              <CardHeader>
                <TrendingUp className="w-10 h-10 text-primary mb-2" />
                <CardTitle className="text-lg">SOC 2 Compliant</CardTitle>
              </CardHeader>
              <CardContent>
                <p className="text-sm text-muted-foreground">
                  Enterprise-grade security standards and regular audits
                </p>
              </CardContent>
            </Card>
          </div>
        </div>
      </section>

      {/* Pricing Section */}
      <section id="pricing" className="py-20">
        <div className="container mx-auto px-4">
          <div className="text-center mb-16">
            <h2 className="text-3xl md:text-5xl font-bold mb-4">
              Simple, Transparent Pricing
            </h2>
            <p className="text-xl text-muted-foreground max-w-2xl mx-auto mb-8">
              Choose the plan that's right for you. All plans include a 14-day free trial
            </p>
            <div className="flex items-center justify-center gap-4">
              <button
                onClick={() => setBillingPeriod("monthly")}
                className={`px-4 py-2 rounded-lg ${billingPeriod === "monthly" ? "bg-primary text-primary-foreground" : "bg-muted"}`}
              >
                Monthly
              </button>
              <button
                onClick={() => setBillingPeriod("yearly")}
                className={`px-4 py-2 rounded-lg ${billingPeriod === "yearly" ? "bg-primary text-primary-foreground" : "bg-muted"}`}
              >
                Yearly
                <span className="ml-2 text-xs bg-green-500 text-white px-2 py-1 rounded">Save 20%</span>
              </button>
            </div>
          </div>
          <div className="grid md:grid-cols-3 gap-8 max-w-6xl mx-auto">
            {/* Free Plan */}
            <Card className="border-2">
              <CardHeader>
                <CardTitle className="text-2xl">Free</CardTitle>
                <div className="mt-4">
                  <span className="text-4xl font-bold">$0</span>
                  <span className="text-muted-foreground">/month</span>
                </div>
                <CardDescription>Perfect for trying out EadgeQuery</CardDescription>
              </CardHeader>
              <CardContent className="space-y-4">
                <Link href="/register" className="w-full">
                  <Button className="w-full">Get Started</Button>
                </Link>
                <ul className="space-y-3">
                  <li className="flex items-center gap-2">
                    <CheckCircle className="w-5 h-5 text-green-500" />
                    <span className="text-sm"><strong>30 queries/day</strong> with Demo AI</span>
                  </li>
                  <li className="flex items-center gap-2">
                    <CheckCircle className="w-5 h-5 text-green-500" />
                    <span className="text-sm"><strong>100 queries/day</strong> with your own API key</span>
                  </li>
                  <li className="flex items-center gap-2">
                    <CheckCircle className="w-5 h-5 text-green-500" />
                    <span className="text-sm">Conversation history</span>
                  </li>
                  <li className="flex items-center gap-2">
                    <CheckCircle className="w-5 h-5 text-green-500" />
                    <span className="text-sm">Demo AI (OpenRouter)</span>
                  </li>
                  <li className="flex items-center gap-2">
                    <CheckCircle className="w-5 h-5 text-green-500" />
                    <span className="text-sm">Bring your own API key</span>
                  </li>
                  <li className="flex items-center gap-2">
                    <CheckCircle className="w-5 h-5 text-green-500" />
                    <span className="text-sm">1 database connection</span>
                  </li>
                  <li className="flex items-center gap-2">
                    <CheckCircle className="w-5 h-5 text-green-500" />
                    <span className="text-sm">Community support</span>
                  </li>
                </ul>
              </CardContent>
            </Card>

            {/* Pro Plan */}
            <Card className="border-4 border-primary relative">
              <div className="absolute -top-4 left-1/2 -translate-x-1/2">
                <span className="bg-primary text-primary-foreground px-4 py-1 rounded-full text-sm font-semibold">
                  MOST POPULAR
                </span>
              </div>
              <CardHeader>
                <CardTitle className="text-2xl">Professional</CardTitle>
                <div className="mt-4">
                  <span className="text-4xl font-bold">$30</span>
                  <span className="text-muted-foreground">/month</span>
                </div>
                <CardDescription>For professional data analysts and teams</CardDescription>
              </CardHeader>
              <CardContent className="space-y-4">
                <Link href="/docs" className="w-full">
                  <Button className="w-full">Get Started</Button>
                </Link>
                <ul className="space-y-3">
                  <li className="flex items-center gap-2">
                    <CheckCircle className="w-5 h-5 text-green-500" />
                    <span className="text-sm"><strong>Claude-4-sonnet</strong> Unlimited</span>
                  </li>
                  <li className="flex items-center gap-2">
                    <CheckCircle className="w-5 h-5 text-green-500" />
                    <span className="text-sm"><strong>GPT-4.1-mini</strong> Unlimited</span>
                  </li>
                  <li className="flex items-center gap-2">
                    <CheckCircle className="w-5 h-5 text-green-500" />
                    <span className="text-sm"><strong>GPT-4.1</strong> Unlimited</span>
                  </li>
                  <li className="flex items-center gap-2">
                    <CheckCircle className="w-5 h-5 text-green-500" />
                    <span className="text-sm">10+ database connections</span>
                  </li>
                  <li className="flex items-center gap-2">
                    <CheckCircle className="w-5 h-5 text-green-500" />
                    <span className="text-sm">Priority support</span>
                  </li>
                  <li className="flex items-center gap-2">
                    <CheckCircle className="w-5 h-5 text-green-500" />
                    <span className="text-sm">Conversation history</span>
                  </li>
                  <li className="flex items-center gap-2">
                    <CheckCircle className="w-5 h-5 text-green-500" />
                    <span className="text-sm">Export to CSV</span>
                  </li>
                  <li className="flex items-center gap-2 text-muted-foreground">
                    <span className="text-xs italic">Coming in v1: We manage API keys for you</span>
                  </li>
                </ul>
              </CardContent>
            </Card>

            {/* Enterprise Plan */}
            <Card className="border-2">
              <CardHeader>
                <CardTitle className="text-2xl">Enterprise</CardTitle>
                <div className="mt-4">
                  <span className="text-4xl font-bold">Custom</span>
                </div>
                <CardDescription>Works great for large teams and enterprises</CardDescription>
              </CardHeader>
              <CardContent className="space-y-4">
                <Button className="w-full" variant="outline">Let's Talk</Button>
                <ul className="space-y-3">
                  <li className="flex items-center gap-2">
                    <CheckCircle className="w-5 h-5 text-green-500" />
                    <span className="text-sm"><strong>Claude-4-sonnet</strong></span>
                  </li>
                  <li className="flex items-center gap-2">
                    <CheckCircle className="w-5 h-5 text-green-500" />
                    <span className="text-sm"><strong>GPT-4</strong></span>
                  </li>
                  <li className="flex items-center gap-2">
                    <CheckCircle className="w-5 h-5 text-green-500" />
                    <span className="text-sm">Unlimited active users</span>
                  </li>
                  <li className="flex items-center gap-2">
                    <CheckCircle className="w-5 h-5 text-green-500" />
                    <span className="text-sm">Hosted on premise (custom)</span>
                  </li>
                  <li className="flex items-center gap-2">
                    <CheckCircle className="w-5 h-5 text-green-500" />
                    <span className="text-sm">Unlimited databases</span>
                  </li>
                  <li className="flex items-center gap-2">
                    <CheckCircle className="w-5 h-5 text-green-500" />
                    <span className="text-sm">24/7 premium support</span>
                  </li>
                  <li className="flex items-center gap-2">
                    <CheckCircle className="w-5 h-5 text-green-500" />
                    <span className="text-sm">Dedicated account manager</span>
                  </li>
                  <li className="flex items-center gap-2">
                    <CheckCircle className="w-5 h-5 text-green-500" />
                    <span className="text-sm">SLA guarantee</span>
                  </li>
                </ul>
              </CardContent>
            </Card>
          </div>
        </div>
      </section>

      {/* FAQ Section */}
      <section id="faq" className="bg-muted/50 py-20">
        <div className="container mx-auto px-4 max-w-4xl">
          <div className="text-center mb-16">
            <h2 className="text-3xl md:text-5xl font-bold mb-4">
              Frequently Asked Questions
            </h2>
            <p className="text-xl text-muted-foreground">
              Everything you need to know about EadgeQuery
            </p>
          </div>
          <Accordion type="single" collapsible className="space-y-4">
            <AccordionItem value="item-1" className="bg-background border rounded-lg px-6">
              <AccordionTrigger className="text-left font-semibold">
                What databases does EadgeQuery support?
              </AccordionTrigger>
              <AccordionContent className="text-muted-foreground">
                EadgeQuery supports MySQL, PostgreSQL, Oracle, SQL Server, and H2 databases. We're constantly
                adding support for more database types based on user demand.
              </AccordionContent>
            </AccordionItem>

            <AccordionItem value="item-2" className="bg-background border rounded-lg px-6">
              <AccordionTrigger className="text-left font-semibold">
                Is my data secure?
              </AccordionTrigger>
              <AccordionContent className="text-muted-foreground">
                Absolutely. We use AES-256 encryption for all sensitive data, including API keys. We only
                execute SELECT queries (read-only), and we never store your actual database data. Only query
                history and metadata are saved.
              </AccordionContent>
            </AccordionItem>

            <AccordionItem value="item-3" className="bg-background border rounded-lg px-6">
              <AccordionTrigger className="text-left font-semibold">
                Can I use my own AI API key?
              </AccordionTrigger>
              <AccordionContent className="text-muted-foreground">
                Yes! Professional and Enterprise plans allow you to bring your own API keys from Anthropic
                (Claude) or OpenAI. This gives you full control over your AI usage and costs.
              </AccordionContent>
            </AccordionItem>

            <AccordionItem value="item-4" className="bg-background border rounded-lg px-6">
              <AccordionTrigger className="text-left font-semibold">
                Do I need to know SQL?
              </AccordionTrigger>
              <AccordionContent className="text-muted-foreground">
                Not at all! That's the whole point of EadgeQuery. Just ask questions in plain English (even
                with typos!), and our AI will generate the SQL for you. You can even learn SQL by reviewing
                the generated queries.
              </AccordionContent>
            </AccordionItem>

            <AccordionItem value="item-5" className="bg-background border rounded-lg px-6">
              <AccordionTrigger className="text-left font-semibold">
                What if the AI generates the wrong query?
              </AccordionTrigger>
              <AccordionContent className="text-muted-foreground">
                We show you the generated SQL query before execution, so you can always review it. Our AI is
                highly accurate, but if something seems off, you can rephrase your question or contact
                support for help.
              </AccordionContent>
            </AccordionItem>

            <AccordionItem value="item-6" className="bg-background border rounded-lg px-6">
              <AccordionTrigger className="text-left font-semibold">
                Can I modify or delete data?
              </AccordionTrigger>
              <AccordionContent className="text-muted-foreground">
                No. For security reasons, EadgeQuery only allows SELECT queries (read-only access). This
                ensures your data remains safe and prevents accidental modifications or deletions.
              </AccordionContent>
            </AccordionItem>

            <AccordionItem value="item-7" className="bg-background border rounded-lg px-6">
              <AccordionTrigger className="text-left font-semibold">
                Is there a free trial?
              </AccordionTrigger>
              <AccordionContent className="text-muted-foreground">
                Yes! All paid plans include a 14-day free trial with full access to all features. No credit
                card required to start your trial.
              </AccordionContent>
            </AccordionItem>

            <AccordionItem value="item-8" className="bg-background border rounded-lg px-6">
              <AccordionTrigger className="text-left font-semibold">
                What kind of support do you offer?
              </AccordionTrigger>
              <AccordionContent className="text-muted-foreground">
                Free plan includes email support. Professional plan gets priority support with faster
                response times. Enterprise plan includes 24/7 premium support with dedicated account
                management and SLA guarantees.
              </AccordionContent>
            </AccordionItem>
          </Accordion>
        </div>
      </section>

      {/* CTA Section */}
      <section className="py-20">
        <div className="container mx-auto px-4">
          <div className="bg-gradient-to-r from-primary to-blue-600 rounded-3xl p-12 text-center text-white">
            <h2 className="text-3xl md:text-5xl font-bold mb-4">
              Ready to Transform Your Data Analysis?
            </h2>
            <p className="text-xl mb-8 opacity-90 max-w-2xl mx-auto">
              Join thousands of data professionals who are already using EadgeQuery to unlock insights faster
            </p>
            <div className="flex flex-col sm:flex-row gap-4 justify-center">
              <Link href="/register">
                <Button size="lg" variant="secondary" className="text-lg px-8">
                  Start Free Trial
                  <ChevronRight className="ml-2 w-5 h-5" />
                </Button>
              </Link>
              <Button size="lg" variant="outline" className="text-lg px-8 bg-transparent text-white border-white hover:bg-white/10">
                Schedule Demo
              </Button>
            </div>
          </div>
        </div>
      </section>

      {/* Footer */}
      <footer className="bg-muted/50 border-t">
        <div className="container mx-auto px-4 py-12">
          <div className="grid md:grid-cols-4 gap-8">
            <div className="space-y-4">
              <div className="flex items-center gap-2">
                <img src="/logo.png" alt="EadgeQuery Logo" className="w-6 h-6" />
                <span className="text-xl font-bold">EadgeQuery</span>
                <span className="text-xs text-muted-foreground ml-1">v0.1</span>
              </div>
              <p className="text-sm text-muted-foreground">
                Chat with your database in plain English. Powered by advanced AI.
              </p>
              <div className="flex gap-4">
                <a href="#" className="text-muted-foreground hover:text-primary">
                  <Twitter className="w-5 h-5" />
                </a>
                <a href="#" className="text-muted-foreground hover:text-primary">
                  <Github className="w-5 h-5" />
                </a>
                <a href="#" className="text-muted-foreground hover:text-primary">
                  <Linkedin className="w-5 h-5" />
                </a>
              </div>
            </div>
            <div>
              <h3 className="font-semibold mb-4">Product</h3>
              <ul className="space-y-2 text-sm text-muted-foreground">
                <li><a href="#features" className="hover:text-primary">Features</a></li>
                <li><a href="#pricing" className="hover:text-primary">Pricing</a></li>
                <li><a href="#security" className="hover:text-primary">Security</a></li>
                <li><a href="/docs" className="hover:text-primary">Documentation</a></li>
              </ul>
            </div>
            <div>
              <h3 className="font-semibold mb-4">Company</h3>
              <ul className="space-y-2 text-sm text-muted-foreground">
                <li><a href="/about" className="hover:text-primary">About Us</a></li>
                <li><a href="/blog" className="hover:text-primary">Blog</a></li>
                <li><a href="/careers" className="hover:text-primary">Careers</a></li>
                <li><a href="/contact" className="hover:text-primary">Contact</a></li>
              </ul>
            </div>
            <div>
              <h3 className="font-semibold mb-4">Legal</h3>
              <ul className="space-y-2 text-sm text-muted-foreground">
                <li><a href="/privacy" className="hover:text-primary">Privacy Policy</a></li>
                <li><a href="/terms" className="hover:text-primary">Terms of Service</a></li>
              </ul>
            </div>
          </div>
          <div className="border-t mt-12 pt-8 text-center text-sm text-muted-foreground">
            <p>© 2024 EadgeQuery. All rights reserved. v0.1</p>
          </div>
        </div>
      </footer>
    </div>
  )
}
