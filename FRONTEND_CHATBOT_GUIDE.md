# Next.js Frontend - Chatbot Integration Guide

## Overview

The Next.js frontend has been fully integrated with the chatbot backend APIs. Users can now ask natural language questions about their databases and receive AI-generated SQL queries and answers.

## Architecture

```
┌─────────────┐       ┌──────────────┐       ┌─────────────────┐
│   Next.js   │──────▶│ API Gateway  │──────▶│ Chatbot Service │
│  Frontend   │       │  (Port 8765) │       │  (Port 8087)    │
│ (Port 3000) │       └──────────────┘       └─────────────────┘
└─────────────┘              │                          │
                             │                          ▼
                             │                 ┌──────────────────┐
                             │                 │ Datasource       │
                             │                 │ Service          │
                             │                 │  (Port 8083)     │
                             │                 └──────────────────┘
                             ▼
                      ┌──────────────┐
                      │ Auth Service │
                      │  (Port 8081) │
                      └──────────────┘
```

## Features

### ✅ Implemented Features

1. **Authentication Integration**
   - JWT token management
   - Auto-redirect on session expiry
   - Protected routes

2. **Database Management**
   - Load user's database configurations
   - Dropdown selector for active database
   - Auto-select first database
   - Refresh database list

3. **Chatbot Interface**
   - Clean, modern chat UI
   - Natural language input
   - Real-time loading indicators
   - Error handling with user feedback

4. **SQL Query Display**
   - Show generated SQL queries
   - Syntax-highlighted code blocks
   - Copy-friendly format

5. **Query Results**
   - Tabular display of results
   - Show first 5 rows
   - Row count indicator
   - Handle empty results

6. **AI Answer Display**
   - Natural language responses
   - Markdown support
   - Streaming support (optional)
   - Error messages with icons

## Setup & Installation

### 1. Prerequisites

```bash
# Ensure you have Node.js 18+ and pnpm installed
node --version  # Should be 18+
pnpm --version  # Or npm/yarn
```

### 2. Install Dependencies

```bash
cd front-end-next-ts
pnpm install
```

### 3. Environment Configuration

Create `.env.local` file:

```env
NEXT_PUBLIC_API_URL=http://localhost:8765
NEXT_PUBLIC_AUTH_API=/auth
```

### 4. Start Development Server

```bash
pnpm dev
```

The frontend will be available at: **http://localhost:3000**

## Usage Workflow

### Step 1: Login

1. Navigate to http://localhost:3000/login
2. Enter credentials:
   ```
   Email: testuser@example.com
   Password: Test@123456
   ```
3. Click "Login"

### Step 2: Navigate to Chatbot

1. Click "Chatbot" in the sidebar/navigation
2. Or go directly to http://localhost:3000/chatbot

### Step 3: Select Database

1. In the header, you'll see a database dropdown
2. Select your configured database
3. The chatbot will be enabled once a database is selected

### Step 4: Ask Questions

**Example Questions:**

```
"Show all users"
"How many orders were placed last month?"
"What is the total revenue?"
"Find users with gmail email"
"Show top 10 products by sales"
"Count active users"
```

### Step 5: View Results

For each question, you'll see:
1. **Your Question** - displayed on the right in a blue bubble
2. **SQL Query** - the generated SQL (blue card with code icon)
3. **Query Results** - table showing data (green card with table icon)
4. **AI Answer** - natural language explanation

## UI Components

### Main Components

#### 1. Database Selector
```typescript
<Select
  value={selectedDatabaseId}
  onChange={setSelectedDatabaseId}
>
  {databases.map(db => (
    <SelectItem value={db.id}>
      {db.name} ({db.type})
    </SelectItem>
  ))}
</Select>
```

#### 2. Chat Input
```typescript
<Input
  placeholder="Ask a question about your data..."
  value={input}
  onChange={(e) => setInput(e.target.value)}
  onKeyPress={(e) => e.key === "Enter" && handleSend()}
/>
```

#### 3. Message Display
- User questions: Right-aligned, blue background
- AI answers: Left-aligned, with SQL query and results cards

## API Integration

### Endpoints Used

#### 1. Load Databases
```typescript
const response = await datasourceApi.getAllConfigs(userId)
// GET /datasource/configs/user/{userId}
```

#### 2. Ask Question
```typescript
const response = await chatbotApi.ask({
  question: "Show all users",
  databaseConfigId: 1,
  userId: 1
})
// POST /chatbot/ask
```

#### 3. Streaming (Optional)
```typescript
for await (const chunk of streamChatbot(request)) {
  accumulatedAnswer += chunk
  // Update UI in real-time
}
// POST /chatbot/ask/stream (SSE)
```

## Error Handling

### Common Errors

#### 1. No Database Selected
```
❌ Error: "Please select a database first"
Solution: Select a database from the dropdown
```

#### 2. No Databases Configured
```
❌ Error: "No databases configured"
Solution: Click "Add a database connection" link → navigates to /datasource
```

#### 3. Session Expired
```
❌ Error: "Session expired. Please login again."
Solution: User is automatically redirected to /login
```

#### 4. SQL Generation Failed
```
❌ Error: "Failed to generate SQL query"
Solution: Try rephrasing your question or check AI API configuration
```

#### 5. Query Execution Failed
```
❌ Error: "Query execution failed: <reason>"
Solution: Check database connection and query permissions
```

## Testing

### Manual Testing Steps

#### Test 1: Login & Navigate
```bash
1. Open http://localhost:3000/login
2. Login with valid credentials
3. Navigate to /chatbot
✅ Should see database selector and chat interface
```

#### Test 2: Database Selection
```bash
1. Check database dropdown
✅ Should list all configured databases
2. Select a database
✅ Chat input should be enabled
```

#### Test 3: Ask Simple Question
```bash
1. Type: "Show all users"
2. Press Enter or click Send
✅ Should see:
   - Your question (right side)
   - Generated SQL query (blue card)
   - Query results (green table)
   - AI answer (grey card)
```

#### Test 4: Error Handling
```bash
1. Don't select a database
2. Try to send a question
✅ Should see error: "Please select a database first"
```

#### Test 5: Security Validation
```bash
1. Ask: "Delete all users"
2. Send question
✅ Should receive security error from backend
✅ Error message should be displayed in red
```

### Automated Testing (Future)

```typescript
// Example test case
describe('Chatbot Page', () => {
  it('should load databases on mount', async () => {
    render(<ChatbotPage />)
    await waitFor(() => {
      expect(screen.getByRole('combobox')).toBeInTheDocument()
    })
  })

  it('should send question when database selected', async () => {
    // Test implementation
  })
})
```

## Customization

### Styling

The chatbot uses Tailwind CSS and shadcn/ui components. To customize:

```typescript
// Modify colors in components/chatbot-page.tsx
const userMessageStyle = "bg-primary text-primary-foreground"
const aiMessageStyle = "bg-card border border-border"
```

### Message Limits

```typescript
// Limit displayed rows in SQL results
{result.slice(0, 5).map(row => (
  // Render row
))}

// Change limit:
{result.slice(0, 10).map(row => (
  // Show 10 rows instead
))}
```

### Loading Messages

```typescript
// Customize loading text
<span className="text-sm text-muted-foreground">
  Processing your question...
</span>

// Change to:
<span className="text-sm text-muted-foreground">
  Generating SQL query...
</span>
```

## Best Practices

### 1. Question Formulation
✅ **Good Questions:**
- "Show all users"
- "Count orders by status"
- "Find products with price > 100"

❌ **Bad Questions:**
- "Hello" (not database-related)
- "Delete all records" (security violation)
- "How are you?" (not database query)

### 2. Database Selection
- Always select a database before asking questions
- Select the database that contains the relevant data
- Use refresh button if database list is outdated

### 3. Result Interpretation
- Check SQL query to understand what was executed
- Verify results match your expectation
- If results are unexpected, rephrase question

### 4. Security
- The system only allows SELECT queries
- DELETE, DROP, UPDATE are automatically blocked
- You cannot harm the database through the chatbot

## Troubleshooting

### Problem: Database dropdown is empty

**Possible Causes:**
1. No databases configured
2. API Gateway is down
3. Authentication token expired

**Solutions:**
1. Navigate to /datasource and add a database
2. Check that backend services are running
3. Logout and login again

---

### Problem: Chat input is disabled

**Possible Causes:**
1. No database selected
2. Currently processing a question

**Solutions:**
1. Select a database from dropdown
2. Wait for current question to complete

---

### Problem: Questions return errors

**Possible Causes:**
1. Database connection lost
2. AI API not configured
3. Invalid SQL generated

**Solutions:**
1. Test database connection in /datasource
2. Check chat-bot-service logs
3. Try rephrasing the question

---

### Problem: Slow responses

**Possible Causes:**
1. Large database queries
2. AI API latency
3. Network issues

**Solutions:**
1. Ask more specific questions
2. Use streaming mode (if implemented)
3. Check network connection

## File Structure

```
front-end-next-ts/
├── app/
│   ├── chatbot/
│   │   └── page.tsx           # Chatbot route
│   ├── datasource/
│   │   └── page.tsx           # Database management
│   └── login/
│       └── page.tsx           # Login page
├── components/
│   ├── chatbot-page.tsx       # Main chatbot component ⭐
│   └── ui/                    # shadcn/ui components
├── contexts/
│   └── AuthContext.tsx        # Authentication context
├── lib/
│   └── api.ts                 # API client & endpoints ⭐
└── package.json
```

## API Response Examples

### Successful Response
```json
{
  "success": true,
  "question": "Show all users",
  "sqlQuery": "SELECT * FROM users",
  "sqlResult": [
    {
      "id": 1,
      "name": "John Doe",
      "email": "john@example.com"
    }
  ],
  "answer": "I found 1 user in the database:\n\nJohn Doe (john@example.com)"
}
```

### Error Response
```json
{
  "success": false,
  "error": "Security error: Forbidden SQL keyword detected: DELETE"
}
```

## Future Enhancements

### Planned Features

1. **Conversation History Panel**
   - View past conversations
   - Resume previous sessions
   - Export conversation history

2. **Advanced Streaming**
   - Word-by-word streaming
   - Real-time SQL execution progress

3. **Query Editor**
   - Edit generated SQL before execution
   - Save frequently used queries

4. **Export Results**
   - Download results as CSV/Excel
   - Copy results to clipboard

5. **Voice Input**
   - Ask questions using voice
   - Text-to-speech for answers

6. **Multi-Database Queries**
   - Query multiple databases simultaneously
   - Join data across databases

## Support

### Documentation
- Backend API: `/CHATBOT_TESTING_GUIDE.md`
- Main README: `/README.md`

### Logs
```bash
# Frontend logs
# Check browser console (F12)

# Backend logs
docker-compose logs chat-bot-service
docker-compose logs data-source
```

### Common Issues
See [Troubleshooting](#troubleshooting) section above.

---

**Version:** 1.0.0
**Last Updated:** 2025-01-15
**Frontend Framework:** Next.js 14 + TypeScript
**UI Library:** shadcn/ui + Tailwind CSS
