"use client"

import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import {
  Database,
  Server,
  Boxes,
  Shield,
  MessageSquare,
  Users,
  Bell,
  Search,
  Network,
  GitBranch,
  Container,
  Activity,
  Code,
  ArrowRight,
  Globe,
  Lock,
  Zap,
  ArrowLeft
} from "lucide-react"
import Link from "next/link"

export default function ArchitecturePage() {
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
            <div className="flex items-center gap-4">
              <Link href="/">
                <Button variant="ghost">
                  <ArrowLeft className="w-4 h-4 mr-2" />
                  Back to Home
                </Button>
              </Link>
            </div>
          </div>
        </div>
      </nav>

      {/* Hero Section */}
      <section className="container mx-auto px-4 py-12">
        <div className="text-center mb-12">
          <div className="inline-block mb-4">
            <span className="bg-primary/10 text-primary px-4 py-2 rounded-full text-sm font-semibold">
              🏗️ System Architecture
            </span>
          </div>
          <h1 className="text-4xl md:text-6xl font-bold mb-4">
            <span className="bg-gradient-to-r from-primary to-blue-600 bg-clip-text text-transparent">
              Microservices Architecture
            </span>
          </h1>
          <p className="text-xl text-muted-foreground max-w-3xl mx-auto">
            Scalable, resilient, and cloud-native architecture powering EadgeQuery AI platform
          </p>
        </div>

        {/* Architecture Layers */}
        <div className="space-y-8 mb-12">

          {/* Layer 1: Client Layer */}
          <div className="relative">
            <div className="absolute left-0 top-0 -translate-y-6">
              <span className="text-xs font-semibold text-primary bg-primary/10 px-3 py-1 rounded-full">
                CLIENT LAYER
              </span>
            </div>
            <Card className="border-2 border-primary/20 bg-gradient-to-r from-primary/5 to-blue-500/5">
              <CardContent className="pt-6">
                <div className="flex justify-center">
                  <div className="relative group">
                    <div className="absolute -inset-1 bg-gradient-to-r from-primary to-blue-600 rounded-lg blur opacity-25 group-hover:opacity-50 transition duration-200"></div>
                    <Card className="relative border-2 border-primary/30 w-64 hover:border-primary transition-all">
                      <CardHeader className="text-center">
                        <Globe className="w-12 h-12 mx-auto mb-2 text-primary" />
                        <CardTitle>Next.js Frontend</CardTitle>
                      </CardHeader>
                      <CardContent className="text-center">
                        <div className="space-y-1 text-sm text-muted-foreground">
                          <p>React 18 + TypeScript</p>
                          <p>Tailwind CSS</p>
                          <p>shadcn/ui Components</p>
                          <p className="text-xs text-primary font-semibold mt-2">Port: 3000</p>
                        </div>
                      </CardContent>
                    </Card>
                  </div>
                </div>
              </CardContent>
            </Card>
          </div>

          {/* Connection Arrow */}
          <div className="flex justify-center">
            <ArrowRight className="w-8 h-8 text-primary rotate-90" />
          </div>

          {/* Layer 2: API Gateway Layer */}
          <div className="relative">
            <div className="absolute left-0 top-0 -translate-y-6">
              <span className="text-xs font-semibold text-primary bg-primary/10 px-3 py-1 rounded-full">
                GATEWAY LAYER
              </span>
            </div>
            <Card className="border-2 border-blue-500/20 bg-gradient-to-r from-blue-500/5 to-purple-500/5">
              <CardContent className="pt-6">
                <div className="grid md:grid-cols-3 gap-4">
                  <div className="md:col-span-1"></div>
                  <Card className="border-2 border-blue-500/30 hover:border-blue-500 transition-all">
                    <CardHeader className="text-center">
                      <Network className="w-10 h-10 mx-auto mb-2 text-blue-500" />
                      <CardTitle className="text-lg">API Gateway</CardTitle>
                    </CardHeader>
                    <CardContent className="text-center text-sm text-muted-foreground">
                      <p>Spring Cloud Gateway</p>
                      <p>Routing & Load Balancing</p>
                      <p>Rate Limiting</p>
                      <p className="text-xs text-blue-500 font-semibold mt-2">Port: 8765</p>
                    </CardContent>
                  </Card>
                  <div className="md:col-span-1"></div>
                </div>
              </CardContent>
            </Card>
          </div>

          {/* Connection Arrow */}
          <div className="flex justify-center">
            <ArrowRight className="w-8 h-8 text-primary rotate-90" />
          </div>

          {/* Layer 3: Service Discovery */}
          <div className="relative">
            <div className="absolute left-0 top-0 -translate-y-6">
              <span className="text-xs font-semibold text-primary bg-primary/10 px-3 py-1 rounded-full">
                DISCOVERY LAYER
              </span>
            </div>
            <Card className="border-2 border-purple-500/20 bg-gradient-to-r from-purple-500/5 to-pink-500/5">
              <CardContent className="pt-6">
                <div className="grid md:grid-cols-3 gap-4">
                  <div className="md:col-span-1"></div>
                  <Card className="border-2 border-purple-500/30 hover:border-purple-500 transition-all">
                    <CardHeader className="text-center">
                      <Search className="w-10 h-10 mx-auto mb-2 text-purple-500" />
                      <CardTitle className="text-lg">Naming Server</CardTitle>
                    </CardHeader>
                    <CardContent className="text-center text-sm text-muted-foreground">
                      <p>Netflix Eureka</p>
                      <p>Service Registry</p>
                      <p>Health Monitoring</p>
                      <p className="text-xs text-purple-500 font-semibold mt-2">Port: 8761</p>
                    </CardContent>
                  </Card>
                  <div className="md:col-span-1"></div>
                </div>
              </CardContent>
            </Card>
          </div>

          {/* Connection Arrow */}
          <div className="flex justify-center">
            <ArrowRight className="w-8 h-8 text-primary rotate-90" />
          </div>

          {/* Layer 4: Microservices Layer */}
          <div className="relative">
            <div className="absolute left-0 top-0 -translate-y-6">
              <span className="text-xs font-semibold text-primary bg-primary/10 px-3 py-1 rounded-full">
                MICROSERVICES LAYER
              </span>
            </div>
            <Card className="border-2 border-green-500/20 bg-gradient-to-r from-green-500/5 to-emerald-500/5">
              <CardContent className="pt-6">
                <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-4">

                  {/* Auth Service */}
                  <Card className="border-2 border-green-500/30 hover:border-green-500 transition-all hover:shadow-lg">
                    <CardHeader>
                      <Lock className="w-8 h-8 mb-2 text-green-500" />
                      <CardTitle className="text-base">Auth Service</CardTitle>
                    </CardHeader>
                    <CardContent className="text-sm text-muted-foreground space-y-1">
                      <p>• JWT Authentication</p>
                      <p>• User Management</p>
                      <p>• OAuth2 Integration</p>
                      <p className="text-xs text-green-500 font-semibold mt-2">Port: 8081</p>
                      <p className="text-xs">DB: MySQL (3306)</p>
                    </CardContent>
                  </Card>

                  {/* Chatbot Service */}
                  <Card className="border-2 border-blue-500/30 hover:border-blue-500 transition-all hover:shadow-lg">
                    <CardHeader>
                      <MessageSquare className="w-8 h-8 mb-2 text-blue-500" />
                      <CardTitle className="text-base">Chatbot Service</CardTitle>
                    </CardHeader>
                    <CardContent className="text-sm text-muted-foreground space-y-1">
                      <p>• NL to SQL Conversion</p>
                      <p>• AI Integration (Claude/GPT)</p>
                      <p>• Query Execution</p>
                      <p className="text-xs text-blue-500 font-semibold mt-2">Port: 8086</p>
                      <p className="text-xs">DB: MySQL (3311)</p>
                    </CardContent>
                  </Card>

                  {/* DataSource Service */}
                  <Card className="border-2 border-orange-500/30 hover:border-orange-500 transition-all hover:shadow-lg">
                    <CardHeader>
                      <Database className="w-8 h-8 mb-2 text-orange-500" />
                      <CardTitle className="text-base">DataSource Service</CardTitle>
                    </CardHeader>
                    <CardContent className="text-sm text-muted-foreground space-y-1">
                      <p>• Database Connections</p>
                      <p>• Schema Analysis</p>
                      <p>• Multi-DB Support</p>
                      <p className="text-xs text-orange-500 font-semibold mt-2">Port: 8087</p>
                      <p className="text-xs">DB: MySQL (3308)</p>
                    </CardContent>
                  </Card>

                  {/* User Profile Service */}
                  <Card className="border-2 border-purple-500/30 hover:border-purple-500 transition-all hover:shadow-lg">
                    <CardHeader>
                      <Users className="w-8 h-8 mb-2 text-purple-500" />
                      <CardTitle className="text-base">User Profile Service</CardTitle>
                    </CardHeader>
                    <CardContent className="text-sm text-muted-foreground space-y-1">
                      <p>• Profile Management</p>
                      <p>• API Key Storage</p>
                      <p>• Settings & Preferences</p>
                      <p className="text-xs text-purple-500 font-semibold mt-2">Port: 8088</p>
                      <p className="text-xs">DB: MySQL (3307)</p>
                    </CardContent>
                  </Card>

                  {/* Notification Service */}
                  <Card className="border-2 border-pink-500/30 hover:border-pink-500 transition-all hover:shadow-lg">
                    <CardHeader>
                      <Bell className="w-8 h-8 mb-2 text-pink-500" />
                      <CardTitle className="text-base">Notification Service</CardTitle>
                    </CardHeader>
                    <CardContent className="text-sm text-muted-foreground space-y-1">
                      <p>• Email Notifications</p>
                      <p>• Event Processing</p>
                      <p>• Kafka Consumer</p>
                      <p className="text-xs text-pink-500 font-semibold mt-2">Port: 8089</p>
                      <p className="text-xs">Message: Kafka</p>
                    </CardContent>
                  </Card>

                </div>
              </CardContent>
            </Card>
          </div>

          {/* Layer 5: Data Layer */}
          <div className="relative">
            <div className="absolute left-0 top-0 -translate-y-6">
              <span className="text-xs font-semibold text-primary bg-primary/10 px-3 py-1 rounded-full">
                DATA LAYER
              </span>
            </div>
            <Card className="border-2 border-yellow-500/20 bg-gradient-to-r from-yellow-500/5 to-orange-500/5">
              <CardContent className="pt-6">
                <div className="grid md:grid-cols-2 lg:grid-cols-4 gap-4">

                  <Card className="border-2 border-yellow-500/30 hover:border-yellow-500 transition-all text-center">
                    <CardHeader>
                      <Database className="w-8 h-8 mx-auto mb-2 text-yellow-500" />
                      <CardTitle className="text-sm">Auth DB</CardTitle>
                    </CardHeader>
                    <CardContent className="text-xs text-muted-foreground">
                      <p>MySQL 8.0</p>
                      <p className="text-yellow-600 font-semibold">Port: 3306</p>
                    </CardContent>
                  </Card>

                  <Card className="border-2 border-yellow-500/30 hover:border-yellow-500 transition-all text-center">
                    <CardHeader>
                      <Database className="w-8 h-8 mx-auto mb-2 text-yellow-500" />
                      <CardTitle className="text-sm">User Profile DB</CardTitle>
                    </CardHeader>
                    <CardContent className="text-xs text-muted-foreground">
                      <p>MySQL 8.0</p>
                      <p className="text-yellow-600 font-semibold">Port: 3307</p>
                    </CardContent>
                  </Card>

                  <Card className="border-2 border-yellow-500/30 hover:border-yellow-500 transition-all text-center">
                    <CardHeader>
                      <Database className="w-8 h-8 mx-auto mb-2 text-yellow-500" />
                      <CardTitle className="text-sm">DataSource DB</CardTitle>
                    </CardHeader>
                    <CardContent className="text-xs text-muted-foreground">
                      <p>MySQL 8.0</p>
                      <p className="text-yellow-600 font-semibold">Port: 3308</p>
                    </CardContent>
                  </Card>

                  <Card className="border-2 border-yellow-500/30 hover:border-yellow-500 transition-all text-center">
                    <CardHeader>
                      <Database className="w-8 h-8 mx-auto mb-2 text-yellow-500" />
                      <CardTitle className="text-sm">Chatbot DB</CardTitle>
                    </CardHeader>
                    <CardContent className="text-xs text-muted-foreground">
                      <p>MySQL 8.0</p>
                      <p className="text-yellow-600 font-semibold">Port: 3311</p>
                    </CardContent>
                  </Card>

                </div>
              </CardContent>
            </Card>
          </div>

          {/* Layer 6: Infrastructure & DevOps */}
          <div className="relative">
            <div className="absolute left-0 top-0 -translate-y-6">
              <span className="text-xs font-semibold text-primary bg-primary/10 px-3 py-1 rounded-full">
                INFRASTRUCTURE & DEVOPS LAYER
              </span>
            </div>
            <Card className="border-2 border-red-500/20 bg-gradient-to-r from-red-500/5 to-rose-500/5">
              <CardContent className="pt-6">
                <div className="grid md:grid-cols-2 lg:grid-cols-4 gap-4">

                  {/* Jenkins */}
                  <Card className="border-2 border-blue-600/30 hover:border-blue-600 transition-all">
                    <CardHeader className="text-center">
                      <Container className="w-8 h-8 mx-auto mb-2 text-blue-600" />
                      <CardTitle className="text-sm">Jenkins CI/CD</CardTitle>
                    </CardHeader>
                    <CardContent className="text-xs text-muted-foreground text-center">
                      <p>Build Automation</p>
                      <p>Pipeline Management</p>
                      <p className="text-blue-600 font-semibold mt-1">Port: 8082</p>
                    </CardContent>
                  </Card>

                  {/* SonarQube */}
                  <Card className="border-2 border-indigo-600/30 hover:border-indigo-600 transition-all">
                    <CardHeader className="text-center">
                      <Code className="w-8 h-8 mx-auto mb-2 text-indigo-600" />
                      <CardTitle className="text-sm">SonarQube</CardTitle>
                    </CardHeader>
                    <CardContent className="text-xs text-muted-foreground text-center">
                      <p>Code Quality</p>
                      <p>Security Analysis</p>
                      <p className="text-indigo-600 font-semibold mt-1">Port: 9000</p>
                    </CardContent>
                  </Card>

                  {/* Zipkin */}
                  <Card className="border-2 border-violet-600/30 hover:border-violet-600 transition-all">
                    <CardHeader className="text-center">
                      <Activity className="w-8 h-8 mx-auto mb-2 text-violet-600" />
                      <CardTitle className="text-sm">Zipkin</CardTitle>
                    </CardHeader>
                    <CardContent className="text-xs text-muted-foreground text-center">
                      <p>Distributed Tracing</p>
                      <p>Performance Monitoring</p>
                      <p className="text-violet-600 font-semibold mt-1">Port: 9411</p>
                    </CardContent>
                  </Card>

                  {/* Kafka */}
                  <Card className="border-2 border-purple-600/30 hover:border-purple-600 transition-all">
                    <CardHeader className="text-center">
                      <GitBranch className="w-8 h-8 mx-auto mb-2 text-purple-600" />
                      <CardTitle className="text-sm">Kafka + Zookeeper</CardTitle>
                    </CardHeader>
                    <CardContent className="text-xs text-muted-foreground text-center">
                      <p>Message Streaming</p>
                      <p>Event-Driven Architecture</p>
                      <p className="text-purple-600 font-semibold mt-1">Port: 9092, 2181</p>
                    </CardContent>
                  </Card>

                </div>
              </CardContent>
            </Card>
          </div>

        </div>

        {/* Technology Stack */}
        <Card className="mb-12 border-2 border-primary/20">
          <CardHeader className="text-center">
            <CardTitle className="text-2xl">Technology Stack</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="grid md:grid-cols-3 gap-6">

              <div>
                <h3 className="font-semibold mb-3 text-lg flex items-center gap-2">
                  <Server className="w-5 h-5 text-primary" />
                  Backend
                </h3>
                <ul className="space-y-2 text-sm text-muted-foreground">
                  <li>• Java 17 + Spring Boot 3.x</li>
                  <li>• Spring Cloud (Gateway, Eureka, Config)</li>
                  <li>• Spring Data JPA</li>
                  <li>• Maven Build Tool</li>
                  <li>• RESTful APIs</li>
                  <li>• Microservices Architecture</li>
                </ul>
              </div>

              <div>
                <h3 className="font-semibold mb-3 text-lg flex items-center gap-2">
                  <Globe className="w-5 h-5 text-primary" />
                  Frontend
                </h3>
                <ul className="space-y-2 text-sm text-muted-foreground">
                  <li>• Next.js 14 + React 18</li>
                  <li>• TypeScript</li>
                  <li>• Tailwind CSS</li>
                  <li>• shadcn/ui Components</li>
                  <li>• Server-Side Rendering</li>
                  <li>• Real-time Streaming</li>
                </ul>
              </div>

              <div>
                <h3 className="font-semibold mb-3 text-lg flex items-center gap-2">
                  <Database className="w-5 h-5 text-primary" />
                  Data & Infrastructure
                </h3>
                <ul className="space-y-2 text-sm text-muted-foreground">
                  <li>• MySQL 8.0 Databases</li>
                  <li>• PostgreSQL (SonarQube)</li>
                  <li>• Apache Kafka</li>
                  <li>• Docker & Docker Compose</li>
                  <li>• Jenkins CI/CD</li>
                  <li>• SonarQube Quality Gates</li>
                </ul>
              </div>

            </div>
          </CardContent>
        </Card>

        {/* Key Features */}
        <div className="grid md:grid-cols-2 gap-6 mb-12">

          <Card className="border-2 border-primary/20 hover:border-primary transition-all">
            <CardHeader>
              <Zap className="w-10 h-10 mb-2 text-primary" />
              <CardTitle>Scalability & Performance</CardTitle>
            </CardHeader>
            <CardContent className="space-y-2 text-sm text-muted-foreground">
              <p>• <strong>Horizontal Scaling:</strong> Each service scales independently</p>
              <p>• <strong>Load Balancing:</strong> API Gateway distributes traffic</p>
              <p>• <strong>Caching:</strong> Reduced database load with smart caching</p>
              <p>• <strong>Async Processing:</strong> Kafka for event-driven workflows</p>
              <p>• <strong>Database Per Service:</strong> Isolated data stores</p>
            </CardContent>
          </Card>

          <Card className="border-2 border-primary/20 hover:border-primary transition-all">
            <CardHeader>
              <Shield className="w-10 h-10 mb-2 text-primary" />
              <CardTitle>Security & Reliability</CardTitle>
            </CardHeader>
            <CardContent className="space-y-2 text-sm text-muted-foreground">
              <p>• <strong>JWT Authentication:</strong> Secure token-based auth</p>
              <p>• <strong>AES-256 Encryption:</strong> API keys encrypted at rest</p>
              <p>• <strong>Read-Only Queries:</strong> Database safety guaranteed</p>
              <p>• <strong>Service Discovery:</strong> Automatic failover with Eureka</p>
              <p>• <strong>Distributed Tracing:</strong> Monitor with Zipkin</p>
            </CardContent>
          </Card>

          <Card className="border-2 border-primary/20 hover:border-primary transition-all">
            <CardHeader>
              <Container className="w-10 h-10 mb-2 text-primary" />
              <CardTitle>DevOps & CI/CD</CardTitle>
            </CardHeader>
            <CardContent className="space-y-2 text-sm text-muted-foreground">
              <p>• <strong>Jenkins Pipelines:</strong> Automated build & deploy</p>
              <p>• <strong>Docker Containers:</strong> Consistent environments</p>
              <p>• <strong>SonarQube Integration:</strong> Code quality gates</p>
              <p>• <strong>Automated Testing:</strong> Unit & integration tests</p>
              <p>• <strong>Blue-Green Deployment:</strong> Zero-downtime updates</p>
            </CardContent>
          </Card>

          <Card className="border-2 border-primary/20 hover:border-primary transition-all">
            <CardHeader>
              <Boxes className="w-10 h-10 mb-2 text-primary" />
              <CardTitle>Microservices Benefits</CardTitle>
            </CardHeader>
            <CardContent className="space-y-2 text-sm text-muted-foreground">
              <p>• <strong>Independent Deployment:</strong> Update services separately</p>
              <p>• <strong>Technology Diversity:</strong> Use best tool for each service</p>
              <p>• <strong>Fault Isolation:</strong> Failures don't cascade</p>
              <p>• <strong>Team Autonomy:</strong> Teams own their services</p>
              <p>• <strong>Easier Scaling:</strong> Scale only what needs it</p>
            </CardContent>
          </Card>

        </div>

        {/* Architecture Principles */}
        <Card className="border-2 border-primary/20 mb-12">
          <CardHeader className="text-center">
            <CardTitle className="text-2xl">Architecture Principles</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="grid md:grid-cols-3 gap-6">
              <div className="text-center">
                <div className="w-16 h-16 bg-primary/10 rounded-full flex items-center justify-center mx-auto mb-3">
                  <span className="text-2xl font-bold text-primary">01</span>
                </div>
                <h4 className="font-semibold mb-2">Single Responsibility</h4>
                <p className="text-sm text-muted-foreground">
                  Each microservice focuses on one business capability
                </p>
              </div>

              <div className="text-center">
                <div className="w-16 h-16 bg-primary/10 rounded-full flex items-center justify-center mx-auto mb-3">
                  <span className="text-2xl font-bold text-primary">02</span>
                </div>
                <h4 className="font-semibold mb-2">Decentralized Data</h4>
                <p className="text-sm text-muted-foreground">
                  Each service manages its own database independently
                </p>
              </div>

              <div className="text-center">
                <div className="w-16 h-16 bg-primary/10 rounded-full flex items-center justify-center mx-auto mb-3">
                  <span className="text-2xl font-bold text-primary">03</span>
                </div>
                <h4 className="font-semibold mb-2">Event-Driven</h4>
                <p className="text-sm text-muted-foreground">
                  Services communicate via Kafka for loose coupling
                </p>
              </div>

              <div className="text-center">
                <div className="w-16 h-16 bg-primary/10 rounded-full flex items-center justify-center mx-auto mb-3">
                  <span className="text-2xl font-bold text-primary">04</span>
                </div>
                <h4 className="font-semibold mb-2">API Gateway Pattern</h4>
                <p className="text-sm text-muted-foreground">
                  Single entry point for all client requests
                </p>
              </div>

              <div className="text-center">
                <div className="w-16 h-16 bg-primary/10 rounded-full flex items-center justify-center mx-auto mb-3">
                  <span className="text-2xl font-bold text-primary">05</span>
                </div>
                <h4 className="font-semibold mb-2">Service Discovery</h4>
                <p className="text-sm text-muted-foreground">
                  Dynamic service registration with Eureka
                </p>
              </div>

              <div className="text-center">
                <div className="w-16 h-16 bg-primary/10 rounded-full flex items-center justify-center mx-auto mb-3">
                  <span className="text-2xl font-bold text-primary">06</span>
                </div>
                <h4 className="font-semibold mb-2">Containerization</h4>
                <p className="text-sm text-muted-foreground">
                  Docker ensures consistency across environments
                </p>
              </div>
            </div>
          </CardContent>
        </Card>

        {/* CTA */}
        <Card className="border-2 border-primary text-center">
          <CardContent className="pt-8 pb-8">
            <h3 className="text-2xl font-bold mb-4">Ready to Get Started?</h3>
            <p className="text-muted-foreground mb-6 max-w-2xl mx-auto">
              Experience the power of our microservices architecture with AI-driven database querying
            </p>
            <div className="flex gap-4 justify-center">
              <Link href="/register">
                <Button size="lg">Start Free Trial</Button>
              </Link>
              <Link href="/docs">
                <Button size="lg" variant="outline">View Documentation</Button>
              </Link>
            </div>
          </CardContent>
        </Card>

      </section>

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
