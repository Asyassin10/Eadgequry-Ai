"use client"

import { useState, useRef, useEffect } from "react"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Send, BarChart3, TrendingUp, Users, MessageSquare, Plus, Trash2 } from "lucide-react"
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

interface Conversation {
  id: number
  title: string
  lastMessage: string
  timestamp: string
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
  const [conversations, setConversations] = useState<Conversation[]>([
    {
      id: 1,
      title: "New Conversation",
      lastMessage: "Hello! I'm EadgeQuery Chatbot...",
      timestamp: "Just now",
    },
  ])
  const [activeConversationId, setActiveConversationId] = useState(1)
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

      // Update conversation last message
      setConversations((prev) =>
        prev.map((conv) =>
          conv.id === activeConversationId
            ? { ...conv, lastMessage: input, timestamp: "Just now" }
            : conv
        )
      )

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

  const handleNewConversation = () => {
    const newId = conversations.length + 1
    const newConv: Conversation = {
      id: newId,
      title: `Conversation ${newId}`,
      lastMessage: "Start a new conversation...",
      timestamp: "Just now",
    }
    setConversations([newConv, ...conversations])
    setActiveConversationId(newId)
    setMessages([
      {
        id: 1,
        text: "Hello! I'm EadgeQuery Chatbot. How can I help you analyze your data today?",
        sender: "ai",
      },
    ])
  }

  const handleDeleteConversation = (id: number) => {
    if (conversations.length === 1) {
      return // Don't delete the last conversation
    }
    setConversations((prev) => prev.filter((conv) => conv.id !== id))
    if (activeConversationId === id) {
      setActiveConversationId(conversations[0].id === id ? conversations[1].id : conversations[0].id)
    }
  }

  return (
    <div className="p-6 h-full flex gap-4">
      {/* Main Chat Area */}
      <Card className="flex-1 bg-card border-border flex flex-col">
        <CardHeader className="border-b border-border">
          <CardTitle className="flex items-center gap-2">
            <MessageSquare className="w-5 h-5" />
            <span>EadgeQuery Chatbot</span>
            <Badge className="bg-primary text-primary-foreground">AI Assistant</Badge>
          </CardTitle>
        </CardHeader>
        <CardContent className="flex-1 flex flex-col gap-4 p-4">
          {/* Chat Messages */}
          <div ref={scrollRef} className="flex-1 space-y-4 overflow-y-auto bg-muted/20 rounded-lg p-4">
            {messages.map((message) => (
              <div key={message.id}>
                <div className={`flex ${message.sender === "user" ? "justify-end" : "justify-start"}`}>
                  {message.text && (
                    <div
                      className={`max-w-xs lg:max-w-md px-4 py-3 rounded-lg ${
                        message.sender === "user"
                          ? "bg-primary text-primary-foreground rounded-br-none"
                          : "bg-card border border-border rounded-bl-none"
                      }`}
                    >
                      <p className="text-sm">{message.text}</p>
                    </div>
                  )}
                </div>

                {message.cards && (
                  <div className="grid grid-cols-1 md:grid-cols-3 gap-3 mt-3">
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
                <div className="bg-card border border-border px-4 py-3 rounded-lg rounded-bl-none">
                  <div className="flex gap-2">
                    <div className="w-2 h-2 bg-primary rounded-full animate-bounce"></div>
                    <div className="w-2 h-2 bg-primary rounded-full animate-bounce delay-100"></div>
                    <div className="w-2 h-2 bg-primary rounded-full animate-bounce delay-200"></div>
                  </div>
                </div>
              </div>
            )}
          </div>

          {/* Input Area */}
          <div className="flex gap-2 pt-2 border-t border-border">
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

      {/* Conversation History Panel */}
      <Card className="w-80 bg-card border-border flex flex-col">
        <CardHeader className="border-b border-border">
          <div className="flex items-center justify-between">
            <CardTitle className="text-lg">Conversations</CardTitle>
            <Button
              size="sm"
              onClick={handleNewConversation}
              className="bg-primary hover:bg-secondary text-primary-foreground"
            >
              <Plus className="w-4 h-4" />
            </Button>
          </div>
        </CardHeader>
        <CardContent className="flex-1 overflow-y-auto p-3 space-y-2">
          {conversations.map((conv) => (
            <div
              key={conv.id}
              onClick={() => setActiveConversationId(conv.id)}
              className={`p-3 rounded-lg cursor-pointer transition-colors group ${
                activeConversationId === conv.id
                  ? "bg-primary/10 border-2 border-primary"
                  : "bg-muted/30 border-2 border-transparent hover:bg-muted/50"
              }`}
            >
              <div className="flex items-start justify-between gap-2">
                <div className="flex-1 min-w-0">
                  <div className="flex items-center gap-2 mb-1">
                    <MessageSquare className="w-4 h-4 text-primary flex-shrink-0" />
                    <h4 className="text-sm font-semibold truncate">{conv.title}</h4>
                  </div>
                  <p className="text-xs text-muted-foreground truncate">{conv.lastMessage}</p>
                  <p className="text-xs text-muted-foreground mt-1">{conv.timestamp}</p>
                </div>
                <Button
                  size="sm"
                  variant="ghost"
                  onClick={(e) => {
                    e.stopPropagation()
                    handleDeleteConversation(conv.id)
                  }}
                  className="opacity-0 group-hover:opacity-100 transition-opacity text-destructive hover:text-destructive hover:bg-destructive/10 p-1 h-auto"
                  disabled={conversations.length === 1}
                >
                  <Trash2 className="w-3 h-3" />
                </Button>
              </div>
            </div>
          ))}
        </CardContent>
      </Card>
    </div>
  )
}
