"use client"

import { useState, useRef, useEffect } from "react"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Send, MessageSquare, Database, Code, Table, AlertCircle, Loader2, RefreshCw, Copy, Check, ChevronRight, ChevronDown } from "lucide-react"
import { Badge } from "@/components/ui/badge"
import { useAuth } from "@/contexts/AuthContext"
import { chatbotApi, datasourceApi, streamChatbot, type ChatResponse, type DatabaseConfigDTO, type DatabaseSchemaDTO } from "@/lib/api"
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
  const [schema, setSchema] = useState<DatabaseSchemaDTO | null>(null)
  const [loadingSchema, setLoadingSchema] = useState(false)
  const [expandedTables, setExpandedTables] = useState<Set<string>>(new Set())
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

  // Load schema when database is selected (DO NOT clear conversation)
  useEffect(() => {
    if (selectedDatabaseId && user?.userId) {
      loadSchema(selectedDatabaseId, user.userId)
      loadConversationHistory(user.userId, selectedDatabaseId)
    } else {
      setSchema(null)
    }
  }, [selectedDatabaseId, user])

  const [parsedSchema, setParsedSchema] = useState<any>(null)

  const loadConversationHistory = async (userId: number, databaseId: number) => {
    try {
      const response = await chatbotApi.getUserHistory(userId)

      if (response.error) {
        console.error("Failed to load conversation history:", response.error)
        return
      }

      if (response.data) {
        // Filter conversations for this database and convert to Message format
        const filteredHistory = response.data
          .filter(conv => conv.databaseConfigId === databaseId)
          .sort((a, b) => new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime())

        const conversationMessages: Message[] = []

        filteredHistory.forEach((conv) => {
          // Add user question
          conversationMessages.push({
            id: conv.id * 2 - 1, // Unique ID for question
            question: conv.question,
            answer: "",
            sender: "user",
            type: "question"
          })

          // Add AI response
          conversationMessages.push({
            id: conv.id * 2, // Unique ID for answer
            question: conv.question,
            answer: conv.answer,
            sqlQuery: conv.sqlQuery,
            sqlResult: conv.sqlResult,
            sender: "ai",
            type: conv.errorMessage ? "error" : "answer"
          })
        })

        setMessages(conversationMessages)
      }
    } catch (error) {
      console.error("Error loading conversation history:", error)
    }
  }

  const clearConversation = () => {
    setMessages([])
    toast.success("Conversation cleared")
  }

  const loadSchema = async (configId: number, userId: number) => {
    try {
      setLoadingSchema(true)
      const response = await datasourceApi.getSchema(configId, userId)

      if (response.error) {
        toast.error("Failed to load database schema")
        return
      }

      if (response.data) {
        setSchema(response.data)
        const parsed = JSON.parse(response.data.schemaJson)
        setParsedSchema(parsed)
        // Start with all tables collapsed (empty set)
        setExpandedTables(new Set())
      }
    } catch (error) {
      console.error("Error loading schema:", error)
      toast.error("Failed to load schema")
    } finally {
      setLoadingSchema(false)
    }
  }

  const toggleTable = (tableName: string) => {
    setExpandedTables(prev => {
      const newSet = new Set(prev)
      if (newSet.has(tableName)) {
        newSet.delete(tableName)
      } else {
        newSet.add(tableName)
      }
      return newSet
    })
  }

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
    if (!text) {
      return <div className="text-muted-foreground italic">No response</div>
    }

    const lines = text.split('\n')
    const elements: React.ReactNode[] = []
    let inTable = false
    let tableRows: string[][] = []

    for (let i = 0; i < lines.length; i++) {
      const line = lines[i]

      if (line.trim().startsWith('|') && line.trim().endsWith('|')) {
        if (!inTable) {
          inTable = true
          tableRows = []
        }

        if (line.match(/^\|[\s-:|]+\|$/)) {
          continue
        }

        const cells = line
          .split('|')
          .map(cell => cell.trim())
          .filter(cell => cell !== '')

        tableRows.push(cells)
      } else {
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
              <img src="/logo.png" alt="EadgeQuery Logo" className="w-10 h-10" />
              <CardTitle>EadgeQuery Chatbot</CardTitle>
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

      {/* Main Content: Chat Area + Schema Panel */}
      <div className="flex-1 grid grid-cols-1 lg:grid-cols-3 gap-4 min-h-0">
        {/* Chat Area - Takes 2/3 of width on large screens */}
        <Card className="lg:col-span-2 bg-card border-border flex flex-col">
          <CardHeader className="pb-2">
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-2">
                <MessageSquare className="w-4 h-4 text-primary" />
                <CardTitle className="text-sm">Conversation</CardTitle>
                {messages.length > 0 && (
                  <Badge variant="outline" className="text-xs">
                    {Math.floor(messages.length / 2)} messages
                  </Badge>
                )}
              </div>
              {messages.length > 0 && (
                <Button
                  variant="outline"
                  size="sm"
                  onClick={clearConversation}
                  className="h-7 text-xs"
                >
                  <RefreshCw className="w-3 h-3 mr-1" />
                  Clear
                </Button>
              )}
            </div>
          </CardHeader>
          <CardContent className="flex-1 flex flex-col gap-4 p-4 min-h-0">
            {/* Messages with fixed height and scroll */}
            <div
              ref={scrollRef}
              className="flex-1 space-y-4 overflow-y-auto bg-muted/20 rounded-lg p-4 min-h-0"
              style={{ maxHeight: 'calc(100vh - 350px)' }}
            >
              {messages.length === 0 && (
                <div className="flex flex-col items-center justify-center h-full text-center py-8">
                  <img src="/logo.png" alt="EadgeQuery Logo" className="w-32 h-32 mb-4" />
                  <h3 className="text-lg font-semibold mb-2">
                    Welcome to EadgeQuery Chatbot
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
                            <img src="logo.png" className="w-12 h-12   mb-3" alt="" />

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
                            <pre className="text-xs font-mono bg-slate-900 text-green-400 p-4 overflow-x-auto max-h-60 overflow-y-auto">
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
                            <div className="max-h-60 overflow-y-auto">
                              {renderSqlResult(message.sqlResult)}
                            </div>
                          </div>
                        )}
                        <img src="logo.png" className="w-12 h-12   mb-3" alt="" />
                        {/* AI Answer */}
                        <div
                          className={`px-4 py-3 rounded-lg rounded-bl-none ${message.type === "error"
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
                          <div className="text-sm max-h-80 overflow-y-auto">
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

        {/* Schema Panel - Takes 1/3 of width on large screens */}
        <Card className="bg-card border-border flex flex-col">
          <CardHeader className="pb-3">
            <div className="flex items-center gap-2">
              <Database className="w-4 h-4 text-primary" />
              <CardTitle className="text-sm">Database Schema</CardTitle>
            </div>
          </CardHeader>
          <CardContent className="flex-1 overflow-y-auto p-4 min-h-0">
            {loadingSchema && (
              <div className="flex items-center justify-center h-32">
                <div className="flex flex-col items-center gap-2">
                  <Loader2 className="w-6 h-6 animate-spin text-primary" />
                  <span className="text-xs text-muted-foreground">Loading schema...</span>
                </div>
              </div>
            )}

            {!loadingSchema && !schema && selectedDatabaseId && (
              <div className="flex items-center justify-center h-32 text-center">
                <div className="text-xs text-muted-foreground">
                  <AlertCircle className="w-6 h-6 mx-auto mb-2 text-muted-foreground" />
                  <p>No schema available</p>
                </div>
              </div>
            )}

            {!loadingSchema && !selectedDatabaseId && (
              <div className="flex items-center justify-center h-32 text-center">
                <div className="text-xs text-muted-foreground">
                  <Database className="w-6 h-6 mx-auto mb-2 text-muted-foreground" />
                  <p>Select a database to view schema</p>
                </div>
              </div>
            )}

            {!loadingSchema && schema && (
              <div className="space-y-2">
                <div className="mb-3 p-2 bg-muted/50 rounded-lg">
                  <div className="text-xs font-semibold text-muted-foreground">
                    {schema.databaseName || JSON.parse(schema.schemaJson).databaseName}
                  </div>
                  <div className="text-xs text-muted-foreground">
                    Type: {schema.databaseType || JSON.parse(schema.schemaJson).databaseType}
                  </div>
                  <div className="text-xs text-muted-foreground">
                    Tables: {JSON.parse(schema.schemaJson).tables.length}
                  </div>
                </div>

                <div className="space-y-2 max-h-200">
                  {JSON.parse(schema.schemaJson).tables.map((table: any) => (
                    <div
                      key={table.name}
                      className="border border-border rounded-lg overflow-hidden"
                    >
                      <button
                        onClick={() => toggleTable(table.name)}
                        className="w-full flex items-center justify-between p-2 bg-muted/30 hover:bg-muted/50 transition-colors"
                      >
                        <div className="flex items-center gap-2">
                          <Table className="w-3 h-3 text-primary" />
                          <span className="text-xs font-semibold">{table.name}</span>
                        </div>
                        {expandedTables.has(table.name) ? (
                          <ChevronDown className="w-3 h-3 text-muted-foreground" />
                        ) : (
                          <ChevronRight className="w-3 h-3 text-muted-foreground" />
                        )}
                      </button>

                      {expandedTables.has(table.name) && (
                        <div className="p-2 space-y-1 bg-card max-h-60 overflow-y-auto">
                          {table.columns.map((column: any) => (
                            <div
                              key={column.name}
                              className="flex items-start justify-between text-xs p-1 hover:bg-muted/20 rounded"
                            >
                              <div className="flex-1">
                                <div className="font-mono text-foreground">
                                  {column.name}
                                </div>
                                <div className="text-muted-foreground text-[10px]">
                                  {column.type}
                                  {!column.nullable && (
                                    <span className="ml-1 text-primary">NOT NULL</span>
                                  )}
                                  {table.primaryKeys && table.primaryKeys.includes(column.name) && (
                                    <span className="ml-1 text-yellow-500">PK</span>
                                  )}
                                </div>
                              </div>
                            </div>
                          ))}

                          {table.foreignKeys && table.foreignKeys.length > 0 && (
                            <div className="mt-2 pt-2 border-t border-border">
                              <div className="text-[10px] font-semibold text-muted-foreground mb-1">
                                Foreign Keys
                              </div>
                              {table.foreignKeys.map((fk: any, idx: number) => (
                                <div key={idx} className="text-[10px] text-muted-foreground">
                                  {fk.column} → {fk.referencedTable}.{fk.referencedColumn}
                                </div>
                              ))}
                            </div>
                          )}
                        </div>
                      )}
                    </div>
                  ))}
                </div>
              </div>
            )}
          </CardContent>
        </Card>
      </div>
    </div>
  )
}