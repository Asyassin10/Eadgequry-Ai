"use client"

import { useState, useEffect } from "react"
import { Button } from "@/components/ui/button"
import { Card } from "@/components/ui/card"
import { ArrowLeft } from "lucide-react"
import Link from "next/link"

export default function ArchitecturePage() {
  const [activeFlow, setActiveFlow] = useState<string | null>(null)
  const [hoveredService, setHoveredService] = useState<string | null>(null)

  useEffect(() => {
    // Auto-cycle through flows
    const flows = ['auth', 'chatbot', 'notification']
    let currentIndex = 0

    const interval = setInterval(() => {
      setActiveFlow(flows[currentIndex])
      currentIndex = (currentIndex + 1) % flows.length
    }, 4000)

    return () => clearInterval(interval)
  }, [])

  return (
    <div className="min-h-screen bg-gradient-to-b from-slate-950 via-slate-900 to-slate-950">
      {/* Navigation */}
      <nav className="border-b border-slate-800 bg-slate-950/95 backdrop-blur sticky top-0 z-50">
        <div className="container mx-auto px-4 py-4">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2">
              <img src="/logo.png" alt="EadgeQuery Logo" className="w-8 h-8" />
              <span className="text-2xl font-bold bg-gradient-to-r from-blue-400 to-cyan-400 bg-clip-text text-transparent">
                EadgeQuery
              </span>
              <span className="text-xs text-slate-500 ml-2">v0.1</span>
            </div>
            <Link href="/">
              <Button variant="ghost" className="text-slate-300 hover:text-white">
                <ArrowLeft className="w-4 h-4 mr-2" />
                Back to Home
              </Button>
            </Link>
          </div>
        </div>
      </nav>

      <div className="container mx-auto px-4 py-8">
        {/* Header */}
        <div className="text-center mb-8">
          <h1 className="text-4xl md:text-5xl font-bold mb-4">
            <span className="bg-gradient-to-r from-blue-400 via-cyan-400 to-purple-400 bg-clip-text text-transparent">
              Microservices Architecture
            </span>
          </h1>
          <p className="text-slate-400 text-lg">
            Scalable, Event-Driven, Cloud-Native System
          </p>
        </div>

        {/* Architecture Diagram */}
        <Card className="bg-slate-900/50 border-slate-800 p-8 relative overflow-hidden">
          {/* Background Grid */}
          <div className="absolute inset-0 opacity-10">
            <div className="absolute inset-0" style={{
              backgroundImage: 'linear-gradient(rgba(59, 130, 246, 0.5) 1px, transparent 1px), linear-gradient(90deg, rgba(59, 130, 246, 0.5) 1px, transparent 1px)',
              backgroundSize: '50px 50px'
            }}></div>
          </div>

          <div className="relative">
            <svg viewBox="0 0 1200 1400" className="w-full h-auto">
              <defs>
                {/* Gradients */}
                <linearGradient id="blueGradient" x1="0%" y1="0%" x2="100%" y2="100%">
                  <stop offset="0%" style={{ stopColor: '#3b82f6', stopOpacity: 1 }} />
                  <stop offset="100%" style={{ stopColor: '#06b6d4', stopOpacity: 1 }} />
                </linearGradient>
                <linearGradient id="purpleGradient" x1="0%" y1="0%" x2="100%" y2="100%">
                  <stop offset="0%" style={{ stopColor: '#8b5cf6', stopOpacity: 1 }} />
                  <stop offset="100%" style={{ stopColor: '#ec4899', stopOpacity: 1 }} />
                </linearGradient>
                <linearGradient id="greenGradient" x1="0%" y1="0%" x2="100%" y2="100%">
                  <stop offset="0%" style={{ stopColor: '#10b981', stopOpacity: 1 }} />
                  <stop offset="100%" style={{ stopColor: '#06b6d4', stopOpacity: 1 }} />
                </linearGradient>

                {/* Glow Filters */}
                <filter id="glow">
                  <feGaussianBlur stdDeviation="4" result="coloredBlur"/>
                  <feMerge>
                    <feMergeNode in="coloredBlur"/>
                    <feMergeNode in="SourceGraphic"/>
                  </feMerge>
                </filter>

                {/* Arrow Marker */}
                <marker id="arrowhead" markerWidth="10" markerHeight="10" refX="9" refY="3" orient="auto">
                  <polygon points="0 0, 10 3, 0 6" fill="#3b82f6" />
                </marker>
              </defs>

              {/* Layer Labels */}
              <text x="20" y="40" fill="#94a3b8" fontSize="14" fontWeight="bold">CLIENT LAYER</text>
              <text x="20" y="180" fill="#94a3b8" fontSize="14" fontWeight="bold">GATEWAY LAYER</text>
              <text x="20" y="320" fill="#94a3b8" fontSize="14" fontWeight="bold">DISCOVERY LAYER</text>
              <text x="20" y="460" fill="#94a3b8" fontSize="14" fontWeight="bold">MICROSERVICES LAYER</text>
              <text x="20" y="900" fill="#94a3b8" fontSize="14" fontWeight="bold">DATA LAYER</text>
              <text x="20" y="1100" fill="#94a3b8" fontSize="14" fontWeight="bold">INFRASTRUCTURE LAYER</text>

              {/* CLIENT LAYER - Frontend */}
              <g className="cursor-pointer" onMouseEnter={() => setHoveredService('frontend')}>
                <rect x="450" y="60" width="300" height="80" rx="10"
                  fill="url(#blueGradient)" opacity="0.2" stroke="#3b82f6" strokeWidth="2">
                  <animate attributeName="opacity" values="0.2;0.4;0.2" dur="3s" repeatCount="indefinite" />
                </rect>
                <text x="600" y="95" fill="#60a5fa" fontSize="18" fontWeight="bold" textAnchor="middle">
                  Next.js Frontend
                </text>
                <text x="600" y="115" fill="#94a3b8" fontSize="12" textAnchor="middle">
                  React 18 • TypeScript
                </text>
                <text x="600" y="130" fill="#3b82f6" fontSize="11" fontWeight="bold" textAnchor="middle">
                  Port: 3000
                </text>
              </g>

              {/* Connection: Frontend -> API Gateway */}
              <line x1="600" y1="140" x2="600" y2="190" stroke="#3b82f6" strokeWidth="2" markerEnd="url(#arrowhead)">
                <animate attributeName="stroke-dasharray" values="0,1000;1000,0" dur="2s" repeatCount="indefinite" />
                <animate attributeName="opacity" values="0.3;1;0.3" dur="2s" repeatCount="indefinite" />
              </line>

              {/* GATEWAY LAYER - API Gateway */}
              <g className="cursor-pointer" onMouseEnter={() => setHoveredService('gateway')}>
                <rect x="450" y="200" width="300" height="80" rx="10"
                  fill="url(#blueGradient)" opacity="0.2" stroke="#3b82f6" strokeWidth="2">
                  <animate attributeName="opacity" values="0.2;0.5;0.2" dur="3s" repeatCount="indefinite" />
                </rect>
                <text x="600" y="235" fill="#60a5fa" fontSize="18" fontWeight="bold" textAnchor="middle">
                  API Gateway
                </text>
                <text x="600" y="255" fill="#94a3b8" fontSize="12" textAnchor="middle">
                  Spring Cloud Gateway
                </text>
                <text x="600" y="270" fill="#3b82f6" fontSize="11" fontWeight="bold" textAnchor="middle">
                  Port: 8765
                </text>
              </g>

              {/* Connection: API Gateway -> Eureka */}
              <line x1="600" y1="280" x2="600" y2="330" stroke="#8b5cf6" strokeWidth="2" markerEnd="url(#arrowhead)">
                <animate attributeName="stroke-dasharray" values="0,1000;1000,0" dur="2s" repeatCount="indefinite" />
              </line>

              {/* DISCOVERY LAYER - Eureka */}
              <g className="cursor-pointer" onMouseEnter={() => setHoveredService('eureka')}>
                <rect x="450" y="340" width="300" height="80" rx="10"
                  fill="url(#purpleGradient)" opacity="0.2" stroke="#8b5cf6" strokeWidth="2">
                  <animate attributeName="opacity" values="0.2;0.5;0.2" dur="3s" repeatCount="indefinite" />
                </rect>
                <text x="600" y="375" fill="#a78bfa" fontSize="18" fontWeight="bold" textAnchor="middle">
                  Eureka Discovery
                </text>
                <text x="600" y="395" fill="#94a3b8" fontSize="12" textAnchor="middle">
                  Service Registry
                </text>
                <text x="600" y="410" fill="#8b5cf6" fontSize="11" fontWeight="bold" textAnchor="middle">
                  Port: 8761
                </text>
              </g>

              {/* MICROSERVICES LAYER */}

              {/* Auth Service */}
              <g className="cursor-pointer" onMouseEnter={() => setHoveredService('auth')}>
                <rect x="50" y="480" width="180" height="100" rx="8"
                  fill="url(#greenGradient)" opacity={activeFlow === 'auth' ? 0.5 : 0.2}
                  stroke="#10b981" strokeWidth="2" filter="url(#glow)">
                  {activeFlow === 'auth' && (
                    <animate attributeName="opacity" values="0.3;0.7;0.3" dur="1s" repeatCount="indefinite" />
                  )}
                </rect>
                <text x="140" y="510" fill="#34d399" fontSize="16" fontWeight="bold" textAnchor="middle">
                  🔐 Auth
                </text>
                <text x="140" y="530" fill="#94a3b8" fontSize="10" textAnchor="middle">
                  JWT • OAuth2
                </text>
                <text x="140" y="545" fill="#94a3b8" fontSize="10" textAnchor="middle">
                  User Management
                </text>
                <text x="140" y="565" fill="#10b981" fontSize="10" fontWeight="bold" textAnchor="middle">
                  Port: 8081
                </text>
              </g>

              {/* Chatbot Service */}
              <g className="cursor-pointer" onMouseEnter={() => setHoveredService('chatbot')}>
                <rect x="260" y="480" width="180" height="100" rx="8"
                  fill="url(#blueGradient)" opacity={activeFlow === 'chatbot' ? 0.5 : 0.2}
                  stroke="#3b82f6" strokeWidth="2" filter="url(#glow)">
                  {activeFlow === 'chatbot' && (
                    <animate attributeName="opacity" values="0.3;0.7;0.3" dur="1s" repeatCount="indefinite" />
                  )}
                </rect>
                <text x="350" y="510" fill="#60a5fa" fontSize="16" fontWeight="bold" textAnchor="middle">
                  💬 Chatbot
                </text>
                <text x="350" y="530" fill="#94a3b8" fontSize="10" textAnchor="middle">
                  NL to SQL • AI
                </text>
                <text x="350" y="545" fill="#94a3b8" fontSize="10" textAnchor="middle">
                  Claude • GPT-4
                </text>
                <text x="350" y="565" fill="#3b82f6" fontSize="10" fontWeight="bold" textAnchor="middle">
                  Port: 8086
                </text>
              </g>

              {/* DataSource Service */}
              <g className="cursor-pointer" onMouseEnter={() => setHoveredService('datasource')}>
                <rect x="470" y="480" width="180" height="100" rx="8"
                  fill="url(#purpleGradient)" opacity="0.2" stroke="#f97316" strokeWidth="2">
                  <animate attributeName="opacity" values="0.2;0.4;0.2" dur="4s" repeatCount="indefinite" />
                </rect>
                <text x="560" y="510" fill="#fb923c" fontSize="16" fontWeight="bold" textAnchor="middle">
                  🗄️ DataSource
                </text>
                <text x="560" y="530" fill="#94a3b8" fontSize="10" textAnchor="middle">
                  Multi-DB Support
                </text>
                <text x="560" y="545" fill="#94a3b8" fontSize="10" textAnchor="middle">
                  Schema Analysis
                </text>
                <text x="560" y="565" fill="#f97316" fontSize="10" fontWeight="bold" textAnchor="middle">
                  Port: 8087
                </text>
              </g>

              {/* User Profile Service */}
              <g className="cursor-pointer" onMouseEnter={() => setHoveredService('profile')}>
                <rect x="50" y="610" width="180" height="100" rx="8"
                  fill="url(#purpleGradient)" opacity="0.2" stroke="#8b5cf6" strokeWidth="2">
                  <animate attributeName="opacity" values="0.2;0.4;0.2" dur="4s" repeatCount="indefinite" />
                </rect>
                <text x="140" y="640" fill="#a78bfa" fontSize="16" fontWeight="bold" textAnchor="middle">
                  👤 Profile
                </text>
                <text x="140" y="660" fill="#94a3b8" fontSize="10" textAnchor="middle">
                  User Settings
                </text>
                <text x="140" y="675" fill="#94a3b8" fontSize="10" textAnchor="middle">
                  API Keys
                </text>
                <text x="140" y="695" fill="#8b5cf6" fontSize="10" fontWeight="bold" textAnchor="middle">
                  Port: 8088
                </text>
              </g>

              {/* Notification Service */}
              <g className="cursor-pointer" onMouseEnter={() => setHoveredService('notification')}>
                <rect x="260" y="610" width="180" height="100" rx="8"
                  fill="url(#purpleGradient)" opacity={activeFlow === 'notification' ? 0.5 : 0.2}
                  stroke="#ec4899" strokeWidth="2" filter="url(#glow)">
                  {activeFlow === 'notification' && (
                    <animate attributeName="opacity" values="0.3;0.7;0.3" dur="1s" repeatCount="indefinite" />
                  )}
                </rect>
                <text x="350" y="640" fill="#f472b6" fontSize="16" fontWeight="bold" textAnchor="middle">
                  🔔 Notification
                </text>
                <text x="350" y="660" fill="#94a3b8" fontSize="10" textAnchor="middle">
                  Email Service
                </text>
                <text x="350" y="675" fill="#94a3b8" fontSize="10" textAnchor="middle">
                  Kafka Consumer
                </text>
                <text x="350" y="695" fill="#ec4899" fontSize="10" fontWeight="bold" textAnchor="middle">
                  Port: 8089
                </text>
              </g>

              {/* Connections from Eureka to Services */}
              <line x1="550" y1="420" x2="140" y2="480" stroke="#8b5cf6" strokeWidth="1" strokeDasharray="5,5" opacity="0.3">
                <animate attributeName="stroke-dashoffset" from="0" to="10" dur="1s" repeatCount="indefinite" />
              </line>
              <line x1="600" y1="420" x2="350" y2="480" stroke="#8b5cf6" strokeWidth="1" strokeDasharray="5,5" opacity="0.3">
                <animate attributeName="stroke-dashoffset" from="0" to="10" dur="1s" repeatCount="indefinite" />
              </line>
              <line x1="620" y1="420" x2="560" y2="480" stroke="#8b5cf6" strokeWidth="1" strokeDasharray="5,5" opacity="0.3">
                <animate attributeName="stroke-dashoffset" from="0" to="10" dur="1s" repeatCount="indefinite" />
              </line>
              <line x1="550" y1="420" x2="140" y2="610" stroke="#8b5cf6" strokeWidth="1" strokeDasharray="5,5" opacity="0.3">
                <animate attributeName="stroke-dashoffset" from="0" to="10" dur="1s" repeatCount="indefinite" />
              </line>
              <line x1="580" y1="420" x2="350" y2="610" stroke="#8b5cf6" strokeWidth="1" strokeDasharray="5,5" opacity="0.3">
                <animate attributeName="stroke-dashoffset" from="0" to="10" dur="1s" repeatCount="indefinite" />
              </line>

              {/* Kafka */}
              <g className="cursor-pointer" onMouseEnter={() => setHoveredService('kafka')}>
                <rect x="680" y="480" width="160" height="80" rx="8"
                  fill="#1e293b" stroke="#eab308" strokeWidth="2" opacity="0.8">
                  <animate attributeName="opacity" values="0.6;1;0.6" dur="3s" repeatCount="indefinite" />
                </rect>
                <text x="760" y="510" fill="#fbbf24" fontSize="14" fontWeight="bold" textAnchor="middle">
                  📨 Kafka
                </text>
                <text x="760" y="530" fill="#94a3b8" fontSize="10" textAnchor="middle">
                  Event Streaming
                </text>
                <text x="760" y="545" fill="#eab308" fontSize="10" fontWeight="bold" textAnchor="middle">
                  Port: 9092
                </text>
              </g>

              {/* Kafka connections */}
              <line x1="440" y1="530" x2="680" y2="520" stroke="#eab308" strokeWidth="2" strokeDasharray="5,5" opacity="0.5">
                <animate attributeName="stroke-dashoffset" from="0" to="10" dur="1s" repeatCount="indefinite" />
              </line>
              <line x1="440" y1="660" x2="680" y2="540" stroke="#eab308" strokeWidth="2" strokeDasharray="5,5" opacity="0.5">
                <animate attributeName="stroke-dashoffset" from="0" to="10" dur="1s" repeatCount="indefinite" />
              </line>

              {/* DATA LAYER */}

              {/* Databases */}
              <g>
                <rect x="150" y="780" width="200" height="60" rx="6" fill="#1e293b" stroke="#eab308" strokeWidth="2" opacity="0.6" />
                <text x="250" y="805" fill="#fbbf24" fontSize="14" fontWeight="bold" textAnchor="middle">
                  💾 Auth DB
                </text>
                <text x="250" y="825" fill="#94a3b8" fontSize="10" textAnchor="middle">
                  MySQL 8.0 • Port: 3306
                </text>
              </g>

              <g>
                <rect x="380" y="780" width="200" height="60" rx="6" fill="#1e293b" stroke="#eab308" strokeWidth="2" opacity="0.6" />
                <text x="480" y="805" fill="#fbbf24" fontSize="14" fontWeight="bold" textAnchor="middle">
                  💾 Chatbot DB
                </text>
                <text x="480" y="825" fill="#94a3b8" fontSize="10" textAnchor="middle">
                  MySQL 8.0 • Port: 3311
                </text>
              </g>

              <g>
                <rect x="610" y="780" width="200" height="60" rx="6" fill="#1e293b" stroke="#eab308" strokeWidth="2" opacity="0.6" />
                <text x="710" y="805" fill="#fbbf24" fontSize="14" fontWeight="bold" textAnchor="middle">
                  💾 DataSource DB
                </text>
                <text x="710" y="825" fill="#94a3b8" fontSize="10" textAnchor="middle">
                  MySQL 8.0 • Port: 3308
                </text>
              </g>

              {/* Connections to Databases */}
              <line x1="140" y1="580" x2="250" y2="780" stroke="#eab308" strokeWidth="2" strokeDasharray="3,3" opacity="0.4" />
              <line x1="350" y1="580" x2="480" y2="780" stroke="#eab308" strokeWidth="2" strokeDasharray="3,3" opacity="0.4" />
              <line x1="560" y1="580" x2="710" y2="780" stroke="#eab308" strokeWidth="2" strokeDasharray="3,3" opacity="0.4" />

              {/* INFRASTRUCTURE LAYER */}

              {/* Jenkins */}
              <g className="cursor-pointer" onMouseEnter={() => setHoveredService('jenkins')}>
                <rect x="100" y="950" width="200" height="70" rx="6" fill="#1e293b" stroke="#3b82f6" strokeWidth="2" opacity="0.7">
                  <animate attributeName="opacity" values="0.5;0.9;0.5" dur="4s" repeatCount="indefinite" />
                </rect>
                <text x="200" y="980" fill="#60a5fa" fontSize="14" fontWeight="bold" textAnchor="middle">
                  ⚙️ Jenkins CI/CD
                </text>
                <text x="200" y="1000" fill="#94a3b8" fontSize="10" textAnchor="middle">
                  Build Automation
                </text>
                <text x="200" y="1015" fill="#3b82f6" fontSize="10" fontWeight="bold" textAnchor="middle">
                  Port: 8082
                </text>
              </g>

              {/* SonarQube */}
              <g className="cursor-pointer" onMouseEnter={() => setHoveredService('sonar')}>
                <rect x="330" y="950" width="200" height="70" rx="6" fill="#1e293b" stroke="#6366f1" strokeWidth="2" opacity="0.7">
                  <animate attributeName="opacity" values="0.5;0.9;0.5" dur="4s" repeatCount="indefinite" />
                </rect>
                <text x="430" y="980" fill="#818cf8" fontSize="14" fontWeight="bold" textAnchor="middle">
                  📊 SonarQube
                </text>
                <text x="430" y="1000" fill="#94a3b8" fontSize="10" textAnchor="middle">
                  Code Quality
                </text>
                <text x="430" y="1015" fill="#6366f1" fontSize="10" fontWeight="bold" textAnchor="middle">
                  Port: 9000
                </text>
              </g>

              {/* Zipkin */}
              <g className="cursor-pointer" onMouseEnter={() => setHoveredService('zipkin')}>
                <rect x="560" y="950" width="200" height="70" rx="6" fill="#1e293b" stroke="#8b5cf6" strokeWidth="2" opacity="0.7">
                  <animate attributeName="opacity" values="0.5;0.9;0.5" dur="4s" repeatCount="indefinite" />
                </rect>
                <text x="660" y="980" fill="#a78bfa" fontSize="14" fontWeight="bold" textAnchor="middle">
                  📈 Zipkin
                </text>
                <text x="660" y="1000" fill="#94a3b8" fontSize="10" textAnchor="middle">
                  Distributed Tracing
                </text>
                <text x="660" y="1015" fill="#8b5cf6" fontSize="10" fontWeight="bold" textAnchor="middle">
                  Port: 9411
                </text>
              </g>

              {/* Data Flow Indicator */}
              <g opacity={activeFlow ? 1 : 0}>
                <text x="900" y="600" fill="#3b82f6" fontSize="16" fontWeight="bold">
                  Active Flow:
                </text>
                <text x="900" y="630" fill="#60a5fa" fontSize="14">
                  {activeFlow === 'auth' && '🔐 Authentication'}
                  {activeFlow === 'chatbot' && '💬 AI Query Processing'}
                  {activeFlow === 'notification' && '🔔 Event Notification'}
                </text>
              </g>

              {/* Legend */}
              <g>
                <text x="900" y="100" fill="#94a3b8" fontSize="12" fontWeight="bold">Legend:</text>
                <line x1="900" y1="120" x2="950" y2="120" stroke="#3b82f6" strokeWidth="2" />
                <text x="960" y="125" fill="#94a3b8" fontSize="11">HTTP/REST</text>

                <line x1="900" y1="145" x2="950" y2="145" stroke="#8b5cf6" strokeWidth="1" strokeDasharray="5,5" />
                <text x="960" y="150" fill="#94a3b8" fontSize="11">Service Discovery</text>

                <line x1="900" y1="170" x2="950" y2="170" stroke="#eab308" strokeWidth="2" strokeDasharray="5,5" />
                <text x="960" y="175" fill="#94a3b8" fontSize="11">Kafka Events</text>

                <line x1="900" y1="195" x2="950" y2="195" stroke="#eab308" strokeWidth="2" strokeDasharray="3,3" />
                <text x="960" y="200" fill="#94a3b8" fontSize="11">Database</text>
              </g>

            </svg>
          </div>
        </Card>

        {/* Service Info Panel */}
        {hoveredService && (
          <Card className="mt-6 bg-slate-900/80 border-slate-700 p-6 animate-in slide-in-from-bottom-4">
            <div className="text-center">
              <h3 className="text-xl font-bold text-blue-400 mb-2">
                {hoveredService === 'frontend' && 'Next.js Frontend'}
                {hoveredService === 'gateway' && 'API Gateway'}
                {hoveredService === 'eureka' && 'Eureka Discovery Server'}
                {hoveredService === 'auth' && 'Authentication Service'}
                {hoveredService === 'chatbot' && 'Chatbot Service'}
                {hoveredService === 'datasource' && 'DataSource Service'}
                {hoveredService === 'profile' && 'User Profile Service'}
                {hoveredService === 'notification' && 'Notification Service'}
                {hoveredService === 'kafka' && 'Apache Kafka'}
                {hoveredService === 'jenkins' && 'Jenkins CI/CD'}
                {hoveredService === 'sonar' && 'SonarQube'}
                {hoveredService === 'zipkin' && 'Zipkin Tracing'}
              </h3>
              <p className="text-slate-400">
                {hoveredService === 'frontend' && 'React-based user interface with real-time updates and responsive design'}
                {hoveredService === 'gateway' && 'Single entry point for all client requests with routing and load balancing'}
                {hoveredService === 'eureka' && 'Service registry and discovery server for dynamic service location'}
                {hoveredService === 'auth' && 'Handles authentication, authorization, and user management with JWT'}
                {hoveredService === 'chatbot' && 'Converts natural language to SQL using AI (Claude/GPT-4)'}
                {hoveredService === 'datasource' && 'Manages database connections and schema analysis'}
                {hoveredService === 'profile' && 'Stores user preferences and encrypted API keys'}
                {hoveredService === 'notification' && 'Sends email notifications via event-driven architecture'}
                {hoveredService === 'kafka' && 'Event streaming platform for asynchronous communication'}
                {hoveredService === 'jenkins' && 'Automated CI/CD pipelines for all microservices'}
                {hoveredService === 'sonar' && 'Code quality and security analysis platform'}
                {hoveredService === 'zipkin' && 'Distributed tracing for monitoring request flows'}
              </p>
            </div>
          </Card>
        )}

        {/* Technology Stack */}
        <div className="grid md:grid-cols-3 gap-6 mt-8">
          <Card className="bg-slate-900/50 border-slate-800 p-6">
            <h3 className="text-lg font-bold text-blue-400 mb-3">Backend Stack</h3>
            <ul className="space-y-2 text-slate-400 text-sm">
              <li>• Java 17 + Spring Boot 3.x</li>
              <li>• Spring Cloud (Gateway, Eureka)</li>
              <li>• Maven Build Tool</li>
              <li>• MySQL 8.0 Databases</li>
            </ul>
          </Card>

          <Card className="bg-slate-900/50 border-slate-800 p-6">
            <h3 className="text-lg font-bold text-cyan-400 mb-3">Frontend Stack</h3>
            <ul className="space-y-2 text-slate-400 text-sm">
              <li>• Next.js 14 + React 18</li>
              <li>• TypeScript</li>
              <li>• Tailwind CSS + shadcn/ui</li>
              <li>• Server-Side Rendering</li>
            </ul>
          </Card>

          <Card className="bg-slate-900/50 border-slate-800 p-6">
            <h3 className="text-lg font-bold text-purple-400 mb-3">DevOps Stack</h3>
            <ul className="space-y-2 text-slate-400 text-sm">
              <li>• Docker + Docker Compose</li>
              <li>• Jenkins CI/CD</li>
              <li>• SonarQube Quality Gates</li>
              <li>• Zipkin Distributed Tracing</li>
            </ul>
          </Card>
        </div>

      </div>
    </div>
  )
}
