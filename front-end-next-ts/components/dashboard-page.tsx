"use client"

import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { CheckCircle } from "lucide-react"

export function DashboardPage() {
  const queries = [
    { id: 1, question: "How many active users are there?", status: "answered" },
    { id: 2, question: "What is the revenue trend this month?", status: "answered" },
    { id: 3, question: "Show me top selling products", status: "answered" },
  ]

  return (
    <div className="p-6 space-y-6">
      {/* Status Cards */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        <Card className="bg-card border-border">
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium text-muted-foreground">AI Model</CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-2xl font-bold text-foreground">GPT-4</p>
            <Badge className="mt-2 bg-primary text-primary-foreground">Connected</Badge>
          </CardContent>
        </Card>

        <Card className="bg-card border-border">
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium text-muted-foreground">Database</CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-2xl font-bold text-foreground">PostgreSQL</p>
            <Badge className="mt-2 bg-secondary text-secondary-foreground">Connected</Badge>
          </CardContent>
        </Card>

        <Card className="bg-card border-border">
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium text-muted-foreground">Queries Today</CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-2xl font-bold text-foreground">24</p>
            <p className="text-xs text-muted-foreground mt-2">↑ 12% from yesterday</p>
          </CardContent>
        </Card>
      </div>

      {/* Recent Queries */}
      <Card className="bg-card border-border">
        <CardHeader>
          <CardTitle>Recent Queries</CardTitle>
          <CardDescription>Your latest database queries and questions</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="space-y-3">
            {queries.map((query) => (
              <div key={query.id} className="flex items-center justify-between p-3 bg-muted rounded-lg">
                <p className="text-sm text-foreground">{query.question}</p>
                <CheckCircle className="w-5 h-5 text-primary" />
              </div>
            ))}
          </div>
        </CardContent>
      </Card>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        <Card className="bg-card border-border">
          <CardHeader>
            <CardTitle>How to Get OpenAI API Key</CardTitle>
            <CardDescription>Step-by-step guide to set up your OpenAI API</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="aspect-video bg-muted rounded-lg overflow-hidden">
              <iframe
                width="100%"
                height="100%"
                src="https://www.youtube.com/embed/SzPE_AE0eEo"
                title="How to Get OpenAI API Key"
                frameBorder="0"
                allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
                allowFullScreen
                className="rounded-lg"
              />
            </div>
          </CardContent>
        </Card>

        <Card className="bg-card border-border">
          <CardHeader>
            <CardTitle>How to Get Claude AI API Key</CardTitle>
            <CardDescription>Step-by-step guide to set up your Claude AI API</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="aspect-video bg-muted rounded-lg overflow-hidden">
              <iframe
                width="100%"
                height="100%"
                src="https://www.youtube.com/embed/vgncj7MJbVU"
                title="How to Get Claude AI API Key"
                frameBorder="0"
                allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
                allowFullScreen
                className="rounded-lg"
              />
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  )
}
