"use client"

import { useState, useRef, useEffect } from "react"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Send, BarChart3, TrendingUp, Users } from "lucide-react"
import { Badge } from "@/components/ui/badge"

interface Message {
  id: number
  text?: string
  sender: "user" | "ai"
  cards?: Array<{
    title: string
    value: string
    icon: "chart" | "trend" | "users"
    color: string
  }>
}

export function ChatbotPage() {
  const [messages, setMessages] = useState<Message[]>([
    {
      id: 1,
      text: "Hello! I'm EadgeQuery Chatbot. How can I help you analyze your data today?",
      sender: "ai",
    },
  ])
  const [input, setInput] = useState("")
  const [isLoading, setIsLoading] = useState(false)
  const scrollRef = useRef<HTMLDivElement>(null)

  useEffect(() => {
    if (scrollRef.current) {
      scrollRef.current.scrollTop = scrollRef.current.scrollHeight
    }
  }, [messages])

  const getCardIcon = (icon: string) => {
    switch (icon) {
      case "chart":
        return <BarChart3 className="w-4 h-4" />
      case "trend":
        return <TrendingUp className="w-4 h-4" />
      case "users":
        return <Users className="w-4 h-4" />
      default:
        return null
    }
  }

  const handleSend = () => {
    if (input.trim()) {
      const newMessage: Message = {
        id: messages.length + 1,
        text: input,
        sender: "user",
      }
      setMessages([...messages, newMessage])
      setInput("")
      setIsLoading(true)

      setTimeout(() => {
        const responses = [
          {
            text: "Here's the data analysis you requested:",
            cards: [
              { title: "Active Users", value: "12,543", icon: "users" as const, color: "bg-blue-500" },
              { title: "Revenue", value: "$54,231", icon: "trend" as const, color: "bg-green-500" },
              { title: "Growth Rate", value: "+23%", icon: "chart" as const, color: "bg-purple-500" },
            ],
          },
          {
            text: "Based on your query, here are the key metrics:",
          },
          {
            text: "I found the data you requested. Processing results...",
          },
        ]

        const randomResponse = responses[Math.floor(Math.random() * responses.length)]
        const aiResponse: Message = {
          id: messages.length + 2,
          ...randomResponse,
          sender: "ai",
        }
        setMessages((prev) => [...prev, aiResponse])
        setIsLoading(false)
      }, 1000)
    }
  }

  return (
    <div className="p-6 h-full flex">
      <Card className="w-full bg-card border-border flex flex-col">
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <span>EadgeQuery Chatbot</span>
            <Badge className="bg-primary text-primary-foreground">AI Assistant</Badge>
          </CardTitle>
        </CardHeader>
        <CardContent className="flex-1 flex flex-col gap-4">
          {/* Chat Messages */}
          <div ref={scrollRef} className="flex-1 space-y-4 overflow-y-auto bg-muted/20 rounded-lg p-4">
            {messages.map((message) => (
              <div key={message.id}>
                <div className={`flex ${message.sender === "user" ? "justify-end" : "justify-start"}`}>
                  {message.text && (
                    <div
                      className={`max-w-xs lg:max-w-md px-4 py-2 rounded-lg ${
                        message.sender === "user"
                          ? "bg-primary text-primary-foreground rounded-br-none"
                          : "bg-secondary text-secondary-foreground rounded-bl-none"
                      }`}
                    >
                      <p className="text-sm">{message.text}</p>
                    </div>
                  )}
                </div>

                {message.cards && (
                  <div className="grid grid-cols-1 md:grid-cols-3 gap-3 mt-2">
                    {message.cards.map((card, idx) => (
                      <div key={idx} className={`${card.color} rounded-lg p-4 text-white shadow-lg`}>
                        <div className="flex items-center gap-2 mb-2">
                          {getCardIcon(card.icon)}
                          <p className="text-sm font-medium">{card.title}</p>
                        </div>
                        <p className="text-2xl font-bold">{card.value}</p>
                      </div>
                    ))}
                  </div>
                )}
              </div>
            ))}
            {isLoading && (
              <div className="flex justify-start">
                <div className="bg-secondary text-secondary-foreground px-4 py-2 rounded-lg rounded-bl-none">
                  <div className="flex gap-2">
                    <div className="w-2 h-2 bg-secondary-foreground rounded-full animate-bounce"></div>
                    <div className="w-2 h-2 bg-secondary-foreground rounded-full animate-bounce delay-100"></div>
                    <div className="w-2 h-2 bg-secondary-foreground rounded-full animate-bounce delay-200"></div>
                  </div>
                </div>
              </div>
            )}
          </div>

          {/* Input Area */}
          <div className="flex gap-2">
            <Input
              type="text"
              placeholder="Ask a question about your data..."
              value={input}
              onChange={(e) => setInput(e.target.value)}
              onKeyPress={(e) => e.key === "Enter" && handleSend()}
              className="flex-1 bg-input border-border"
            />
            <Button
              onClick={handleSend}
              disabled={isLoading || !input.trim()}
              className="bg-primary hover:bg-secondary text-primary-foreground"
            >
              <Send className="w-4 h-4" />
            </Button>
          </div>
        </CardContent>
      </Card>
    </div>
  )
}
