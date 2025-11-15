"use client"

 import { Button } from "@/components/ui/button"
import { Card } from "@/components/ui/card"
import { ArrowLeft } from "lucide-react"
import Link from "next/link"
import React, { useState, useRef, useEffect } from 'react';



interface Service {
  id: string;
  name: string;
  port: string;
  x: number;
  y: number;
  width: number;
  height: number;
}

interface Connection {
  from: string;
  to: string;
  label: string;
  type: 'solid' | 'dashed';
}


export default function ArchitecturePage() {
  const svgRef = useRef<SVGSVGElement>(null);
  const [services, setServices] = useState<Service[]>([
    { id: 'customer', name: 'Customer', port: '3001', x: 320, y: 140, width: 100, height: 80 },
    { id: 'product', name: 'Product', port: '3002', x: 320, y: 250, width: 100, height: 80 },
    { id: 'order', name: 'Order', port: '3003', x: 320, y: 360, width: 100, height: 80 },
    { id: 'payment', name: 'Payment', port: '3004', x: 620, y: 250, width: 100, height: 80 },
    { id: 'notification', name: 'Notification', port: '3005', x: 920, y: 250, width: 120, height: 80 },
    { id: 'kafka', name: 'Kafka', port: '9092', x: 750, y: 380, width: 140, height: 80 },
    { id: 'zipkin', name: 'ZIPKIN', port: '9411', x: 1080, y: 240, width: 120, height: 100 },
    { id: 'mongodb', name: 'MongoDB', port: '27017', x: 620, y: 100, width: 110, height: 80 },
    { id: 'eureka', name: 'Eureka', port: '8761', x: 380, y: 530, width: 120, height: 80 },
    { id: 'config', name: 'Config', port: '8888', x: 820, y: 530, width: 120, height: 80 },
  ]);

  const connections: Connection[] = [
    { from: 'customer', to: 'payment', label: 'Payment Info', type: 'dashed' },
    { from: 'product', to: 'payment', label: 'Product Data', type: 'dashed' },
    { from: 'order', to: 'payment', label: 'Order Details', type: 'dashed' },
    { from: 'payment', to: 'notification', label: 'Confirmation', type: 'dashed' },
    { from: 'notification', to: 'zipkin', label: 'Tracing', type: 'solid' },
    { from: 'customer', to: 'mongodb', label: 'Persist', type: 'solid' },
    { from: 'order', to: 'kafka', label: 'Async Order', type: 'dashed' },
    { from: 'kafka', to: 'notification', label: 'Async Event', type: 'dashed' },
    { from: 'customer', to: 'eureka', label: 'Register', type: 'dashed' },
    { from: 'product', to: 'eureka', label: 'Register', type: 'dashed' },
    { from: 'order', to: 'eureka', label: 'Register', type: 'dashed' },
    { from: 'eureka', to: 'config', label: 'Config Sync', type: 'dashed' },
  ];

  const [dragging, setDragging] = useState<string | null>(null);
  const [dragOffset, setDragOffset] = useState({ x: 0, y: 0 });

  const handleMouseDown = (e: React.MouseEvent<SVGRectElement>, serviceId: string) => {
    if (!svgRef.current) return;
    
    const svg = svgRef.current;
    const rect = svg.getBoundingClientRect();
    const clickX = e.clientX - rect.left;
    const clickY = e.clientY - rect.top;
    
    const service = services.find(s => s.id === serviceId);
    if (service) {
      setDragging(serviceId);
      setDragOffset({
        x: clickX - service.x,
        y: clickY - service.y,
      });
    }
  };

  const handleMouseMove = (e: React.MouseEvent<SVGSVGElement>) => {
    if (!dragging || !svgRef.current) return;

    const svg = svgRef.current;
    const rect = svg.getBoundingClientRect();
    const moveX = e.clientX - rect.left;
    const moveY = e.clientY - rect.top;

    setServices(services.map(service => {
      if (service.id === dragging) {
        return {
          ...service,
          x: Math.max(250, Math.min(moveX - dragOffset.x, 1300)),
          y: Math.max(90, Math.min(moveY - dragOffset.y, 700)),
        };
      }
      return service;
    }));
  };

  const handleMouseUp = () => {
    setDragging(null);
  };

  const getServiceCenter = (serviceId: string) => {
    const service = services.find(s => s.id === serviceId);
    if (!service) return { x: 0, y: 0 };
    return {
      x: service.x + service.width / 2,
      y: service.y + service.height / 2,
    };
  };

  const createCurvePath = (fromId: string, toId: string) => {
    const from = getServiceCenter(fromId);
    const to = getServiceCenter(toId);
    const dx = to.x - from.x;
    const dy = to.y - from.y;
    const distance = Math.sqrt(dx * dx + dy * dy);
    const controlDistance = distance / 2.5;
    const perpX = (-dy / distance) * controlDistance;
    const perpY = (dx / distance) * controlDistance;
    return `M ${from.x} ${from.y} Q ${from.x + perpX} ${from.y + perpY} ${to.x} ${to.y}`;
  };

  return (
    <div className="min-h-screen bg-gradient-to-b from-slate-950 via-slate-900 to-slate-950">
      {/* Navigation */}
      <nav className=" bg-white  sticky top-0 z-50">
        <div className=" bg-white container mx-auto px-4 py-4 bg-gradient ">
          <div className="flex items-center justify-between bg-white  to-slate-950">
            <div className="flex items-center gap-2 ">
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

    <div className=" bg-gradient-to-br from-blue-50 to-blue-100 p-8 ">
       
      <div className="w-400 ml-50">
        <div className="mb-4">
          <h1 className="text-4xl font-bold text-blue-900 mb-2">EdgeQuery Microservices Architecture</h1>
          <p className="text-blue-700">Interactive Drag Services • Port Mapping & API Gateway Routing</p>
          <p className="text-sm text-blue-600 mt-2">💡 Tip: Drag any service box to reposition it</p>
        </div>

        <div className="bg-white rounded-lg shadow-xl overflow-hidden border-2 border-blue-200   w-full">
          <svg
            ref={svgRef}
            viewBox="0 0 1400 900"
            className="w-full cursor-move"
            onMouseMove={handleMouseMove}
            onMouseUp={handleMouseUp}
            onMouseLeave={handleMouseUp}
          >
            <defs>
              <style>{`
                .service-box { fill: white; stroke: #1e3a8a; stroke-width: 2; transition: all 0.2s; }
                .service-box:hover { stroke-width: 3; filter: drop-shadow(0 0 8px rgba(30, 58, 138, 0.4)); }
                .service-text { font-size: 14px; font-weight: 600; fill: #1e3a8a; font-family: 'Geist', sans-serif; pointer-events: none; }
                .port-label { font-size: 12px; fill: #1e40af; font-weight: 500; font-family: 'Geist Mono', monospace; pointer-events: none; }
                .connection-solid { stroke: #16a34a; stroke-width: 2; fill: none; }
                .connection-dashed { stroke: #16a34a; stroke-width: 2; fill: none; stroke-dasharray: 5,5; }
                .region-border { stroke: #1e3a8a; stroke-width: 2; fill: none; stroke-dasharray: 8,4; }
                .section-label { font-size: 13px; font-weight: 700; fill: #0c2340; font-family: 'Geist', sans-serif; pointer-events: none; }
              `}</style>
              <marker id="arrowhead" markerWidth="10" markerHeight="10" refX="9" refY="3" orient="auto">
                <polygon points="0 0, 10 3, 0 6" fill="#16a34a" />
              </marker>
            </defs>

            {/* Background */}
            <rect x="0" y="0" width="1400" height="900" fill="#f8fafc" />

            {/* Public Network Label */}
            <text x="30" y="60" className="section-label">Public Network</text>

            {/* Private Network Region */}
            <rect x="250" y="90" width="1100" height="700" className="region-border" />
            <text x="270" y="115" className="section-label">Private Network</text>

            {/* API Gateway */}
            <g>
              <rect x="50" y="240" width="120" height="80" className="service-box" rx="8" />
              <text x="110" y="275" className="service-text" textAnchor="middle">API GW</text>
              <text x="110" y="295" className="port-label" textAnchor="middle">80/443</text>
            </g>

            {/* Connections from API Gateway */}
            <path d="M 170 260 Q 250 200 320 220" className="connection-dashed" markerEnd="url(#arrowhead)" />
            <text x="210" y="210" className="port-label" fill="#16a34a">/customers</text>

            <path d="M 170 280 Q 250 280 320 290" className="connection-dashed" markerEnd="url(#arrowhead)" />
            <text x="210" y="270" className="port-label" fill="#16a34a">/products</text>

            <path d="M 170 300 Q 250 360 320 400" className="connection-dashed" markerEnd="url(#arrowhead)" />
            <text x="210" y="350" className="port-label" fill="#16a34a">/orders</text>

            {connections.map((conn, idx) => (
              <g key={idx}>
                <path
                  d={createCurvePath(conn.from, conn.to)}
                  className={conn.type === 'solid' ? 'connection-solid' : 'connection-dashed'}
                  markerEnd="url(#arrowhead)"
                />
                <text
                  x={(getServiceCenter(conn.from).x + getServiceCenter(conn.to).x) / 2}
                  y={(getServiceCenter(conn.from).y + getServiceCenter(conn.to).y) / 2 - 10}
                  className="port-label"
                  fill="#16a34a"
                  fontSize="11"
                  textAnchor="middle"
                >
                  {conn.label}
                </text>
              </g>
            ))}

            {/* Draggable Services */}
            {services.map(service => (
              <g key={service.id}>
                <rect
                  x={service.x}
                  y={service.y}
                  width={service.width}
                  height={service.height}
                  className="service-box"
                  rx="8"
                  onMouseDown={(e) => handleMouseDown(e, service.id)}
                  style={{ cursor: dragging === service.id ? 'grabbing' : 'grab' }}
                />
                <circle cx={service.x + 15} cy={service.y + 15} r="6" fill="#16a34a" />
                <text
                  x={service.x + service.width / 2}
                  y={service.y + 35}
                  className="service-text"
                  textAnchor="middle"
                  onMouseDown={(e) => handleMouseDown(e, service.id)}
                >
                  {service.name}
                </text>
                <text
                  x={service.x + service.width / 2}
                  y={service.y + service.height - 15}
                  className="port-label"
                  textAnchor="middle"
                  onMouseDown={(e) => handleMouseDown(e, service.id)}
                >
                  Port {service.port}
                </text>
              </g>
            ))}
          </svg>
        </div>

        {/* Service Details */}
        <div className="mt-8 grid grid-cols-2 gap-6">
          <div className="bg-white rounded-lg shadow p-6 border-l-4 border-blue-500">
            <h2 className="text-xl font-bold text-blue-900 mb-4">🔧 Core Services</h2>
            <div className="space-y-3">
              {[
                { name: 'Customer Service', port: '3001' },
                { name: 'Product Service', port: '3002' },
                { name: 'Order Service', port: '3003' },
                { name: 'Payment Service', port: '3004' },
                { name: 'Notification Service', port: '3005' },
              ].map(svc => (
                <div key={svc.port} className="flex justify-between items-center pb-2 border-b border-blue-100">
                  <span className="text-blue-700 font-semibold">{svc.name}</span>
                  <span className="text-green-600 font-mono text-sm">{svc.port}</span>
                </div>
              ))}
            </div>
          </div>

          <div className="bg-white rounded-lg shadow p-6 border-l-4 border-green-500">
            <h2 className="text-xl font-bold text-blue-900 mb-4">⚙️ Infrastructure</h2>
            <div className="space-y-3">
              {[
                { name: 'API Gateway', port: '80/443' },
                { name: 'Eureka Server', port: '8761' },
                { name: 'Config Server', port: '8888' },
                { name: 'Kafka Broker', port: '9092' },
                { name: 'Zipkin Tracing', port: '9411' },
              ].map(svc => (
                <div key={svc.port} className="flex justify-between items-center pb-2 border-b border-blue-100">
                  <span className="text-blue-700 font-semibold">{svc.name}</span>
                  <span className="text-green-600 font-mono text-sm">{svc.port}</span>
                </div>
              ))}
            </div>
          </div>
        </div>

        {/* Database Mappings */}
        <div className="mt-6 bg-white rounded-lg shadow p-6 border-l-4 border-blue-500">
          <h2 className="text-xl font-bold text-blue-900 mb-4">💾 Database Port Mappings</h2>
          <div className="grid grid-cols-4 gap-4">
            {[
              { name: 'Customer DB', port: 'PostgreSQL:5432' },
              { name: 'Product DB', port: 'PostgreSQL:5433' },
              { name: 'Order DB', port: 'PostgreSQL:5434' },
              { name: 'Document Store', port: 'MongoDB:27017' },
            ].map(db => (
              <div key={db.port} className="bg-blue-50 p-4 rounded">
                <p className="text-blue-900 font-semibold text-sm">{db.name}</p>
                <p className="text-green-600 font-mono mt-2 text-sm">{db.port}</p>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
    </div>
  )
}
