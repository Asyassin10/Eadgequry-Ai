"use client"

import { useState, useRef, useEffect } from "react"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Send, MessageSquare, Database, Code, Table, AlertCircle, Loader2, RefreshCw, Copy, Check } from "lucide-react"
import { Badge } from "@/components/ui/badge"
import { useAuth } from "@/contexts/AuthContext"
import { chatbotApi, datasourceApi, streamChatbot, type ChatResponse, type DatabaseConfigDTO } from "@/lib/api"
import { toast } from "sonner"
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select"

interface Message {
  id: number
  question: string
  answer: string
  sqlQuery?: string
  sqlResult?: Array<Record<string, any>>
  sender: "user" | "ai"
  type: "question" | "answer" | "error"
  isStreaming?: boolean
}

export function ChatbotPage() {
  const { user } = useAuth()
  const [messages, setMessages] = useState<Message[]>([])
  const [input, setInput] = useState("")
  const [isLoading, setIsLoading] = useState(false)
  const [loadingMessage, setLoadingMessage] = useState("")
  const [databases, setDatabases] = useState<DatabaseConfigDTO[]>([])
  const [selectedDatabaseId, setSelectedDatabaseId] = useState<number | null>(null)
  const [loadingDatabases, setLoadingDatabases] = useState(true)
  const [copiedSql, setCopiedSql] = useState<number | null>(null)
  const scrollRef = useRef<HTMLDivElement>(null)

  // Auto-scroll to bottom when messages change
  useEffect(() => {
    if (scrollRef.current) {
      scrollRef.current.scrollTop = scrollRef.current.scrollHeight
    }
  }, [messages])

  // Load database configurations on mount
  useEffect(() => {
    if (user?.userId) {
      loadDatabases()
    }
  }, [user])

  const copySqlToClipboard = async (sql: string, messageId: number) => {
    try {
      await navigator.clipboard.writeText(sql)
      setCopiedSql(messageId)
      toast.success("SQL copied to clipboard!")
      setTimeout(() => setCopiedSql(null), 2000)
    } catch (error) {
      toast.error("Failed to copy SQL")
    }
  }

  const loadDatabases = async () => {
    if (!user?.userId) return

    try {
      setLoadingDatabases(true)
      const response = await datasourceApi.getAllConfigs(user.userId)

      if (response.error) {
        toast.error(response.error.message || "Failed to load databases")
        return
      }

      if (response.data) {
        setDatabases(response.data)
        // Auto-select first database if available
        if (response.data.length > 0 && !selectedDatabaseId) {
          setSelectedDatabaseId(response.data[0].id)
        }
      }
    } catch (error) {
      console.error("Error loading databases:", error)
      toast.error("Failed to load databases")
    } finally {
      setLoadingDatabases(false)
    }
  }

  const handleSend = async () => {
    if (!input.trim()) {
      toast.error("Please enter a question")
      return
    }

    if (!selectedDatabaseId) {
      toast.error("Please select a database first")
      return
    }

    if (!user?.userId) {
      toast.error("You must be logged in")
      return
    }

    const questionText = input.trim()
    setInput("")
    setIsLoading(true)
    setLoadingMessage("Generating query...")

    // Add user question to messages
    const questionMessage: Message = {
      id: Date.now(),
      question: questionText,
      answer: "",
      sender: "user",
      type: "question",
    }
    setMessages((prev) => [...prev, questionMessage])

    try {
      setLoadingMessage("Analyzing data...")
      const response = await chatbotApi.ask({
        question: questionText,
        databaseConfigId: selectedDatabaseId,
        userId: user.userId,
      })

      if (response.error) {
        // Add error message
        const errorMessage: Message = {
          id: Date.now() + 1,
          question: questionText,
          answer: response.error.message || "Failed to process question",
          sender: "ai",
          type: "error",
        }
        setMessages((prev) => [...prev, errorMessage])
        toast.error(response.error.message || "Failed to process question")
        return
      }

      if (response.data) {
        // Add AI answer with SQL query and results
        const answerMessage: Message = {
          id: Date.now() + 1,
          question: response.data.question,
          answer: response.data.answer,
          sqlQuery: response.data.sqlQuery,
          sqlResult: response.data.sqlResult,
          sender: "ai",
          type: "answer",
        }
        setMessages((prev) => [...prev, answerMessage])
      }
    } catch (error) {
      console.error("Error sending message:", error)
      const errorMessage: Message = {
        id: Date.now() + 1,
        question: questionText,
        answer: "An unexpected error occurred. Please try again.",
        sender: "ai",
        type: "error",
      }
      setMessages((prev) => [...prev, errorMessage])
      toast.error("An unexpected error occurred")
    } finally {
      setIsLoading(false)
    }
  }

  const handleSendStreaming = async () => {
    if (!input.trim()) {
      toast.error("Please enter a question")
      return
    }

    if (!selectedDatabaseId) {
      toast.error("Please select a database first")
      return
    }

    if (!user?.userId) {
      toast.error("You must be logged in")
      return
    }

    const questionText = input.trim()
    setInput("")
    setIsLoading(true)

    // Add user question
    const questionMessage: Message = {
      id: Date.now(),
      question: questionText,
      answer: "",
      sender: "user",
      type: "question",
    }
    setMessages((prev) => [...prev, questionMessage])

    // Add streaming AI answer placeholder
    const streamingMessageId = Date.now() + 1
    const streamingMessage: Message = {
      id: streamingMessageId,
      question: questionText,
      answer: "",
      sender: "ai",
      type: "answer",
      isStreaming: true,
    }
    setMessages((prev) => [...prev, streamingMessage])

    try {
      let accumulatedAnswer = ""

      for await (const chunk of streamChatbot({
        question: questionText,
        databaseConfigId: selectedDatabaseId,
        userId: user.userId,
      })) {
        accumulatedAnswer += chunk
        // Update streaming message
        setMessages((prev) =>
          prev.map((msg) =>
            msg.id === streamingMessageId
              ? { ...msg, answer: accumulatedAnswer }
              : msg
          )
        )
      }

      // Mark streaming as complete
      setMessages((prev) =>
        prev.map((msg) =>
          msg.id === streamingMessageId
            ? { ...msg, isStreaming: false }
            : msg
        )
      )
    } catch (error) {
      console.error("Streaming error:", error)
      toast.error("Streaming failed. Try non-streaming mode.")
      // Remove streaming message and add error
      setMessages((prev) =>
        prev.filter((msg) => msg.id !== streamingMessageId)
      )
      const errorMessage: Message = {
        id: Date.now() + 2,
        question: questionText,
        answer: "Streaming failed. Please try again.",
        sender: "ai",
        type: "error",
      }
      setMessages((prev) => [...prev, errorMessage])
    } finally {
      setIsLoading(false)
    }
  }

  const renderMarkdown = (text: string) => {
    // Simple markdown renderer for tables and basic formatting
    const lines = text.split('\n')
    const elements: React.ReactNode[] = []
    let inTable = false
    let tableRows: string[][] = []

    for (let i = 0; i < lines.length; i++) {
      const line = lines[i]

      // Detect table rows (lines with |)
      if (line.trim().startsWith('|') && line.trim().endsWith('|')) {
        if (!inTable) {
          inTable = true
          tableRows = []
        }

        // Skip separator row (|---|---|)
        if (line.match(/^\|[\s-:|]+\|$/)) {
          continue
        }

        const cells = line
          .split('|')
          .map(cell => cell.trim())
          .filter(cell => cell !== '')

        tableRows.push(cells)
      } else {
        // End of table or regular text
        if (inTable && tableRows.length > 0) {
          const headers = tableRows[0]
          const rows = tableRows.slice(1)

          elements.push(
            <div key={`table-${i}`} className="overflow-x-auto my-3">
              <table className="w-full text-xs border-collapse border border-border rounded-lg">
                <thead className="bg-muted">
                  <tr>
                    {headers.map((header, idx) => (
                      <th key={idx} className="text-left p-2 font-semibold border-b border-border">
                        {header}
                      </th>
                    ))}
                  </tr>
                </thead>
                <tbody>
                  {rows.map((row, rowIdx) => (
                    <tr key={rowIdx} className="border-b border-border/50 hover:bg-muted/30">
                      {row.map((cell, cellIdx) => (
                        <td key={cellIdx} className="p-2">
                          {cell}
                        </td>
                      ))}
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )

          inTable = false
          tableRows = []
        }

        if (line.trim()) {
          elements.push(<p key={`p-${i}`} className="my-1">{line}</p>)
        }
      }
    }

    // Handle remaining table at end
    if (inTable && tableRows.length > 0) {
      const headers = tableRows[0]
      const rows = tableRows.slice(1)

      elements.push(
        <div key="table-final" className="overflow-x-auto my-3">
          <table className="w-full text-xs border-collapse border border-border rounded-lg">
            <thead className="bg-muted">
              <tr>
                {headers.map((header, idx) => (
                  <th key={idx} className="text-left p-2 font-semibold border-b border-border">
                    {header}
                  </th>
                ))}
              </tr>
            </thead>
            <tbody>
              {rows.map((row, rowIdx) => (
                <tr key={rowIdx} className="border-b border-border/50 hover:bg-muted/30">
                  {row.map((cell, cellIdx) => (
                    <td key={cellIdx} className="p-2">
                      {cell}
                    </td>
                  ))}
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )
    }

    return <div>{elements}</div>
  }

  const renderSqlResult = (result: Array<Record<string, any>>) => {
    if (!result || result.length === 0) {
      return (
        <div className="text-xs text-muted-foreground italic">
          No results returned
        </div>
      )
    }

    const columns = Object.keys(result[0])

    return (
      <div className="overflow-x-auto">
        <table className="w-full text-xs border-collapse">
          <thead>
            <tr className="border-b border-border">
              {columns.map((col) => (
                <th key={col} className="text-left p-2 font-semibold">
                  {col}
                </th>
              ))}
            </tr>
          </thead>
          <tbody>
            {result.slice(0, 5).map((row, idx) => (
              <tr key={idx} className="border-b border-border/50">
                {columns.map((col) => (
                  <td key={col} className="p-2">
                    {row[col] !== null && row[col] !== undefined
                      ? String(row[col])
                      : "null"}
                  </td>
                ))}
              </tr>
            ))}
          </tbody>
        </table>
        {result.length > 5 && (
          <div className="text-xs text-muted-foreground mt-2 text-center">
            Showing 5 of {result.length} rows
          </div>
        )}
      </div>
    )
  }

  return (
    <div className="p-6 h-full flex flex-col gap-4">
      {/* Header with Database Selector */}
      <Card className="bg-card border-border">
        <CardHeader className="pb-3">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2">
              <MessageSquare className="w-5 h-5 text-primary" />
              <CardTitle>EadgeQuery AI Chatbot</CardTitle>
              <Badge className="bg-primary text-primary-foreground">
                AI Assistant
              </Badge>
            </div>
            <div className="flex items-center gap-2">
              <Database className="w-4 h-4 text-muted-foreground" />
              <Select
                value={selectedDatabaseId?.toString()}
                onValueChange={(value) => setSelectedDatabaseId(Number(value))}
                disabled={loadingDatabases || databases.length === 0}
              >
                <SelectTrigger className="w-[250px]">
                  <SelectValue placeholder="Select a database..." />
                </SelectTrigger>
                <SelectContent>
                  {databases.map((db) => (
                    <SelectItem key={db.id} value={db.id.toString()}>
                      {db.name} ({db.type})
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
              <Button
                size="sm"
                variant="ghost"
                onClick={loadDatabases}
                disabled={loadingDatabases}
              >
                <RefreshCw className={`w-4 h-4 ${loadingDatabases ? "animate-spin" : ""}`} />
              </Button>
            </div>
          </div>
        </CardHeader>
      </Card>

      {/* Chat Area */}
      <Card className="flex-1 bg-card border-border flex flex-col">
        <CardContent className="flex-1 flex flex-col gap-4 p-4">
          {/* Messages */}
          <div
            ref={scrollRef}
            className="flex-1 space-y-4 overflow-y-auto bg-muted/20 rounded-lg p-4"
          >
            {messages.length === 0 && (
              <div className="flex flex-col items-center justify-center h-full text-center">
                <MessageSquare className="w-12 h-12 text-muted-foreground mb-3" />
                <h3 className="text-lg font-semibold mb-2">
                  Welcome to EadgeQuery AI Chatbot
                </h3>
                <p className="text-sm text-muted-foreground max-w-md">
                  Ask questions about your database in natural language. I'll
                  generate SQL queries and provide answers.
                </p>
              </div>
            )}

            {messages.map((message) => (
              <div key={message.id} className="space-y-2">
                {/* User Question */}
                {message.type === "question" && (
                  <div className="flex justify-end">
                    <div className="max-w-xs lg:max-w-md px-4 py-3 rounded-lg bg-primary text-primary-foreground rounded-br-none">
                      <p className="text-sm">{message.question}</p>
                    </div>
                  </div>
                )}

                {/* AI Answer */}
                {(message.type === "answer" || message.type === "error") && (
                  <div className="flex justify-start">
                    <div className="max-w-2xl space-y-3">
                      {/* SQL Query - Terminal Style */}
                      {message.sqlQuery && (
                        <div className="bg-slate-900 border border-slate-700 rounded-lg overflow-hidden">
                          <div className="flex items-center justify-between bg-slate-800 px-3 py-2 border-b border-slate-700">
                            <div className="flex items-center gap-2">
                              <div className="flex gap-1.5">
                                <div className="w-3 h-3 rounded-full bg-red-500"></div>
                                <div className="w-3 h-3 rounded-full bg-yellow-500"></div>
                                <div className="w-3 h-3 rounded-full bg-green-500"></div>
                              </div>
                              <Code className="w-4 h-4 text-slate-400" />
                              <span className="text-xs font-mono text-slate-300">
                                Generated SQL Query
                              </span>
                            </div>
                            <Button
                              size="sm"
                              variant="ghost"
                              className="h-6 px-2 text-slate-400 hover:text-white hover:bg-slate-700"
                              onClick={() => copySqlToClipboard(message.sqlQuery!, message.id)}
                            >
                              {copiedSql === message.id ? (
                                <>
                                  <Check className="w-3 h-3 mr-1" />
                                  <span className="text-xs">Copied!</span>
                                </>
                              ) : (
                                <>
                                  <Copy className="w-3 h-3 mr-1" />
                                  <span className="text-xs">Copy</span>
                                </>
                              )}
                            </Button>
                          </div>
                          <pre className="text-xs font-mono bg-slate-900 text-green-400 p-4 overflow-x-auto">
                            <code>{message.sqlQuery}</code>
                          </pre>
                        </div>
                      )}

                      {/* SQL Results */}
                      {message.sqlResult && message.sqlResult.length > 0 && (
                        <div className="bg-card border border-border rounded-lg p-3">
                          <div className="flex items-center gap-2 mb-2">
                            <Table className="w-4 h-4 text-green-500" />
                            <span className="text-xs font-semibold">
                              Query Results ({message.sqlResult.length} rows)
                            </span>
                          </div>
                          {renderSqlResult(message.sqlResult)}
                        </div>
                      )}

                      {/* AI Answer */}
                      <div
                        className={`px-4 py-3 rounded-lg rounded-bl-none ${
                          message.type === "error"
                            ? "bg-destructive/10 border border-destructive text-destructive"
                            : "bg-card border border-border"
                        }`}
                      >
                        {message.type === "error" && (
                          <div className="flex items-center gap-2 mb-2">
                            <AlertCircle className="w-4 h-4" />
                            <span className="text-xs font-semibold">Error</span>
                          </div>
                        )}
                        <div className="text-sm">
                          {renderMarkdown(message.answer)}
                          {message.isStreaming && (
                            <span className="inline-block w-2 h-4 ml-1 bg-primary animate-pulse" />
                          )}
                        </div>
                      </div>
                    </div>
                  </div>
                )}
              </div>
            ))}

            {isLoading && (
              <div className="flex justify-start">
                <div className="bg-card border border-border px-4 py-3 rounded-lg rounded-bl-none">
                  <div className="flex gap-2 items-center">
                    <Loader2 className="w-4 h-4 animate-spin text-primary" />
                    <span className="text-sm text-muted-foreground">
                      {loadingMessage || "Processing your question..."}
                    </span>
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
              disabled={isLoading || !selectedDatabaseId}
            />
            <Button
              onClick={handleSend}
              disabled={isLoading || !input.trim() || !selectedDatabaseId}
              className="bg-primary hover:bg-secondary text-primary-foreground"
            >
              <Send className="w-4 h-4" />
            </Button>
          </div>

          {!selectedDatabaseId && databases.length > 0 && (
            <div className="text-xs text-center text-muted-foreground">
              Please select a database to start chatting
            </div>
          )}

          {databases.length === 0 && !loadingDatabases && (
            <div className="text-xs text-center text-muted-foreground">
              No databases configured.{" "}
              <a href="/datasource" className="text-primary underline">
                Add a database connection
              </a>{" "}
              to get started.
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  )
}
