"use client"

import type React from "react"

import { useState } from "react"
import Link from "next/link"
import { usePathname, useRouter } from "next/navigation"
import { Bell, LogOut, LayoutDashboard, MessageCircle, Database, Settings } from "lucide-react"
import { Button } from "@/components/ui/button"
import { Avatar, AvatarImage, AvatarFallback } from "@/components/ui/avatar"
import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger } from "@/components/ui/dropdown-menu"

interface LayoutProps {
  children: React.ReactNode
  isDark: boolean
  onToggleTheme: () => void
}

export function Layout({ children, isDark, onToggleTheme }: LayoutProps) {
  const [sidebarOpen, setSidebarOpen] = useState(true)
  const [showNotifications, setShowNotifications] = useState(false)
  const pathname = usePathname()
  const router = useRouter()

  const isActive = (path: string) => pathname === path

  const notifications = [
    { id: 1, title: "System Update", message: "Database upgraded to v2.1", time: "2 hours ago" },
    { id: 2, title: "New Feature", message: "Multi-datasource support now available", time: "1 day ago" },
    { id: 3, title: "Alert", message: "API rate limit increased", time: "3 days ago" },
  ]

  const navigationItems = [
    { href: "/", label: "Dashboard", icon: LayoutDashboard },
    { href: "/chatbot", label: "Chatbot", icon: MessageCircle },
    { href: "/datasource", label: "Datasource", icon: Database },
    { href: "/settings", label: "Settings", icon: Settings },
  ]

  const handleLogout = () => {
    console.log("Logging out...")
    router.push("/")
  }

  return (
    <div className="flex h-screen bg-background">
      {/* Sidebar */}
      <aside
        className={`${
          sidebarOpen ? "w-64" : "w-20"
        } bg-sidebar border-r border-sidebar-border transition-all duration-300 flex flex-col`}
      >
        {/* Logo */}
        <div className="p-6 border-b border-sidebar-border">
          <Link href="/" className="flex items-center gap-3">
            <img src="/logo.png" alt="EadgeQuery AI" className="w-10 h-10" />
            {sidebarOpen && <span className="font-bold text-lg text-sidebar-foreground truncate">EadgeQuery</span>}
          </Link>
        </div>

        {/* Navigation */}
        <nav className="flex-1 p-4 space-y-2">
          {navigationItems.map((item) => {
            const IconComponent = item.icon
            return (
              <Link
                key={item.href}
                href={item.href}
                className={`flex items-center gap-3 px-4 py-2 rounded-lg transition-colors ${
                  isActive(item.href)
                    ? "bg-sidebar-primary text-sidebar-primary-foreground"
                    : "text-sidebar-foreground hover:bg-sidebar-accent"
                }`}
              >
                <IconComponent className="w-5 h-5 flex-shrink-0" />
                {sidebarOpen && <span className="text-sm">{item.label}</span>}
              </Link>
            )
          })}
        </nav>

        {/* Logout Button */}
        <div className="p-4 border-t border-sidebar-border">
          <Button variant="ghost" size="sm" className="w-full justify-start gap-2" onClick={handleLogout}>
            <LogOut className="w-5 h-5" />
            {sidebarOpen && <span className="text-sm">Logout</span>}
          </Button>
        </div>
      </aside>

      {/* Main Content */}
      <div className="flex-1 flex flex-col overflow-hidden">
        {/* Navbar */}
        <header className="bg-card border-b border-border px-6 py-4 flex items-center justify-between">
          <h1 className="text-2xl font-bold text-foreground">EadgeQuery AI</h1>

          <div className="flex items-center gap-4">
            {/* Notifications */}
            <div className="relative">
              <Button variant="ghost" size="icon" onClick={() => setShowNotifications(!showNotifications)}>
                <Bell className="w-5 h-5" />
              </Button>

              {showNotifications && (
                <div className="absolute right-0 mt-2 w-80 bg-card border border-border rounded-lg shadow-lg z-50 max-h-96 overflow-y-auto">
                  <div className="p-4 border-b border-border sticky top-0 bg-card">
                    <h3 className="font-semibold text-foreground">Platform Updates</h3>
                  </div>
                  <div className="divide-y divide-border">
                    {notifications.map((notif) => (
                      <div key={notif.id} className="p-4 hover:bg-muted/50 transition-colors">
                        <p className="font-medium text-sm text-foreground">{notif.title}</p>
                        <p className="text-xs text-muted-foreground mt-1">{notif.message}</p>
                        <p className="text-xs text-muted-foreground mt-2">{notif.time}</p>
                      </div>
                    ))}
                  </div>
                </div>
              )}
            </div>

            {/* User Menu */}
            <DropdownMenu>
              <DropdownMenuTrigger asChild>
                <Button variant="ghost" size="icon">
                  <Avatar className="w-8 h-8">
                    <AvatarImage src="https://avatar.vercel.sh/user?size=32" />
                    <AvatarFallback>U</AvatarFallback>
                  </Avatar>
                </Button>
              </DropdownMenuTrigger>
              <DropdownMenuContent align="end">
                <DropdownMenuItem onClick={() => router.push("/settings")}>Profile Settings</DropdownMenuItem>
                <DropdownMenuItem>
                  <LogOut className="w-4 h-4 mr-2" />
                  Sign Out
                </DropdownMenuItem>
              </DropdownMenuContent>
            </DropdownMenu>
          </div>
        </header>

        {/* Page Content */}
        <main className="flex-1 overflow-auto bg-muted/30">{children}</main>
      </div>
    </div>
  )
}
