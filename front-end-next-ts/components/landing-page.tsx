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
import { Database, MessageSquare, Shield, Zap, CheckCircle, ChevronRight, Lock, Server, TrendingUp, Users, BarChart3, Brain, Github, Twitter, Linkedin, Mail } from 'lucide-react'
import Link from "next/link"
import { BackgroundVideo } from './youtube-player'
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog'
import { Demo } from "./demo"

export function LandingPage() {
  const [billingPeriod, setBillingPeriod] = useState<"monthly" | "yearly">("monthly")
  const [isOpen, setIsOpen] = useState(false)

  return (
    <div className="min-h-screen bg-gradient-to-b from-background to-muted">
      {/* Navigation */}
      <nav className="border-b bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60 sticky top-0 z-50">
        <div className="container mx-auto px-4 py-4">
          <div className="flex items-center justify-between">
            <a href="/" className="flex items-center gap-2">


              <img src="/logo.png" alt="EadgeQuery Logo" className="w-8 h-8" />
              <span className="text-2xl font-bold bg-gradient-to-r from-primary to-blue-600 bg-clip-text text-transparent">
                EadgeQuery
              </span>
              <span className="text-xs text-muted-foreground ml-2">v0.1</span>
            </a>
            <div className="hidden md:flex items-center gap-8">
              <a href="#features" className="text-sm font-medium hover:text-primary transition-colors">
                Features
              </a>

              <a href="#pricing" className="text-sm font-medium hover:text-primary transition-colors">
                Pricing
              </a>
              <Link href="#faq" className="text-sm font-medium hover:text-primary transition-colors">
                FAQ
              </Link>
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
                  Get Started
                  <ChevronRight className="ml-2 w-5 h-5" />
                </Button>
              </Link>
              <Link href="/docs">
                <Button size="lg" variant="outline" className="text-lg px-8">
                  View Documentation
                </Button>
              </Link>
            </div>

          </div>
          <div className="relative w-full  "> {/* set height as needed */}
            <BackgroundVideo
              title=""
              videoSrc="/loop.mp4"
            />
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
                Securely connect to MySQL, PostgreSQL, Oracle, or any other supported database
              </p>
            </div>
            <div className="text-center space-y-4">
              <div className="w-16 h-16 bg-primary/10 rounded-full flex items-center justify-center mx-auto">
                <span className="text-2xl font-bold text-primary">2</span>
              </div>
              <h3 className="text-xl font-semibold">Ask Questions</h3>
              <p className="text-muted-foreground">
                Type your questions in plain English. AI understands what you need, even with typos
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
          <Demo videoId="iSPLAIAIoPI" />

        </div>
      </section>

      {/* Supported Databases Section */}
      <section className="py-20">
        <div className="container mx-auto px-4">
          <div className="text-center mb-16">
            <h2 className="text-3xl md:text-5xl font-bold mb-4">
              Works with Your Database
            </h2>
            <p className="text-xl text-muted-foreground max-w-2xl mx-auto">
              Connect to the most popular database platforms with seamless integration
            </p>
          </div>

          {/* Database Logos Grid */}
          <div className="max-w-5xl mx-auto">
            <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-8 items-center justify-items-center">

              {/* MySQL */}
              <div className="flex flex-col items-center">
                <div className="w-20 h-20 flex items-center justify-center rounded-xl border shadow-sm hover:shadow-md transition-all bg-white">
                  <img src="mysql.png" alt="MySQL" className="w-14 h-14 object-contain" />
                </div>
                <span className="mt-2 text-sm font-medium">MySQL</span>
              </div>

              {/* PostgreSQL */}
              <div className="flex flex-col items-center">
                <div className="w-20 h-20 flex items-center justify-center rounded-xl border shadow-sm hover:shadow-md transition-all bg-white">
                  <img src="postgre.png" alt="PostgreSQL" className="w-14 h-14 object-contain" />
                </div>
                <span className="mt-2 text-sm font-medium">PostgreSQL</span>
              </div>

              {/* Oracle */}
              <div className="flex flex-col items-center">
                <div className="w-20 h-20 flex items-center justify-center rounded-xl border shadow-sm hover:shadow-md transition-all bg-white">
                  <img src="oracle.png" alt="Oracle" className="w-14 h-14 object-contain" />
                </div>
                <span className="mt-2 text-sm font-medium">Oracle</span>
              </div>

              {/* SQL Server */}
              <div className="flex flex-col items-center">
                <div className="w-20 h-20 flex items-center justify-center rounded-xl border shadow-sm hover:shadow-md transition-all bg-white">
                  <img src="sql-server.png" alt="SQL Server" className="w-14 h-14 object-contain" />
                </div>
                <span className="mt-2 text-sm font-medium">SQL Server</span>
              </div>

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
          <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-6">
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
              Choose the plan that's right for you
            </p>

          </div>
          <div className="grid md:grid-cols-3 gap-8 max-w-6xl mx-auto">
            {/* Free Plan */}
            <Card className="border-2 border-4 border-primary relative">
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
                <ul className="space-y-3 mt-5">
                  <li className="flex items-center gap-2">
                    <CheckCircle className="w-5 h-5 text-green-500" />
                    <span className="text-sm"><strong>30 queries/day</strong> with Free Mode</span>
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
                    <span className="text-sm">Free Mode</span>
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
            <Card className="">

              <CardHeader>
                <CardTitle className="text-2xl">Professional</CardTitle>
                <div className="mt-4">
                  <span className="text-4xl font-bold">$30</span>
                  <span className="text-muted-foreground">/month</span>
                </div>
                <CardDescription>For professional data analysts and teams</CardDescription>
              </CardHeader>
              <CardContent className="space-y-4">
                <Link target="__blank" href="/docs#v1-features" className="w-full">
                  <Button className="w-full">View Documentation</Button>
                </Link>
                <ul className="space-y-3 mt-5">
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

            {/* Card */}
            <Card className="border-2">
              <CardHeader>
                <CardTitle className="text-2xl">Enterprise</CardTitle>
                <div className="mt-4">
                  <span className="text-4xl font-bold">Custom</span>
                </div>
                <CardDescription>Works great for large teams and enterprises</CardDescription>
              </CardHeader>

              <CardContent className="space-y-4">
                <Button className="w-full" variant="outline" onClick={() => setIsOpen(true)}>
                  Let's Talk
                </Button>

                <ul className="space-y-3">
                  <li className="flex items-center gap-2">
                    <CheckCircle className="w-5 h-5 text-green-500" />
                    <span className="text-sm">
                      <strong>Claude-4-sonnet</strong>
                    </span>
                  </li>
                  <li className="flex items-center gap-2">
                    <CheckCircle className="w-5 h-5 text-green-500" />
                    <span className="text-sm">
                      <strong>GPT-4</strong>
                    </span>
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
                </ul>

              </CardContent>
            </Card>

            {/* Modal (Popup) */}
            <Dialog open={isOpen} onOpenChange={setIsOpen}>
              <DialogContent className="flex flex-col items-center justify-center space-y-3 border-b border-gray-200 bg-white px-4 py-6 pt-8 text-center md:px-16">


                <div className="space-y-6">
                  <div>
                    <h3 className="font-semibold text-slate-900 mb-2">Join Our Discord</h3>
                    <p className="text-sm text-slate-600 mb-4">
                      We recommend joining our Discord server and sending a direct message to us.
                    </p>
                    <a
                      href="https://discord.gg/g8zyEDKTAj"
                      target="_blank"
                      rel="noopener noreferrer"
                      className="flex animate-fade-up items-center justify-center space-x-2 overflow-hidden rounded-full bg-blue-100 px-7 py-2 transition-colors hover:bg-blue-200 "
                    >
                      <svg className="mr-2" width="24" height="24" fill="currentColor" xmlns="http://www.w3.org/2000/svg" viewBox="0 5 30.67 23.25"><title>Discord</title><path d="M26.0015 6.9529C24.0021 6.03845 21.8787 5.37198 19.6623 5C19.3833 5.48048 19.0733 6.13144 18.8563 6.64292C16.4989 6.30193 14.1585 6.30193 11.8336 6.64292C11.6166 6.13144 11.2911 5.48048 11.0276 5C8.79575 5.37198 6.67235 6.03845 4.6869 6.9529C0.672601 12.8736 -0.41235 18.6548 0.130124 24.3585C2.79599 26.2959 5.36889 27.4739 7.89682 28.2489C8.51679 27.4119 9.07477 26.5129 9.55525 25.5675C8.64079 25.2265 7.77283 24.808 6.93587 24.312C7.15286 24.1571 7.36986 23.9866 7.57135 23.8161C12.6241 26.1255 18.0969 26.1255 23.0876 23.8161C23.3046 23.9866 23.5061 24.1571 23.7231 24.312C22.8861 24.808 22.0182 25.2265 21.1037 25.5675C21.5842 26.5129 22.1422 27.4119 22.7621 28.2489C25.2885 27.4739 27.8769 26.2959 30.5288 24.3585C31.1952 17.7559 29.4733 12.0212 26.0015 6.9529ZM10.2527 20.8402C8.73376 20.8402 7.49382 19.4608 7.49382 17.7714C7.49382 16.082 8.70276 14.7025 10.2527 14.7025C11.7871 14.7025 13.0425 16.082 13.0115 17.7714C13.0115 19.4608 11.7871 20.8402 10.2527 20.8402ZM20.4373 20.8402C18.9183 20.8402 17.6768 19.4608 17.6768 17.7714C17.6768 16.082 18.8873 14.7025 20.4373 14.7025C21.9717 14.7025 23.2271 16.082 23.1961 17.7714C23.1961 19.4608 21.9872 20.8402 20.4373 20.8402Z"></path></svg>
                      Join Discord
                    </a>
                  </div>

                  <div className="relative">
                    <div className="absolute inset-0 flex items-center">
                      <div className="w-full border-t border-slate-200"></div>
                    </div>
                    <div className="relative flex justify-center text-sm">
                      <span className="px-2 bg-white text-slate-500">Or</span>
                    </div>
                  </div>

                  <div>
                    <h3 className="font-semibold text-slate-900 mb-2">Email Us</h3>
                    <p className="text-sm text-slate-600 mb-4">
                      Send us an email and we'll get back to you as soon as possible.
                    </p>
                    <a
                      href="mailto:yassineaitsidibrahim@gmail.com"
                      className="text-blue-500"
                    >
                      yassineaitsidibrahim@gmail.com
                    </a>
                  </div>
                </div>
              </DialogContent>
            </Dialog>
          </div>
        </div>
      </section >

      {/* FAQ Section */}
      < section id="faq" className="bg-muted/50 py-20" >
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
      </section >

 

      {/* Footer */}
      < footer className="bg-muted/50 border-t" >
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
                <li><a href="/blog" className="hover:text-primary">Blog</a></li>

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
            <p>© 2025 EadgeQuery. All rights reserved. v0.1</p>
          </div>
        </div>
      </footer >
    </div >
  )
}
