"use client"

import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { ArrowLeft, Calendar, Clock, User, ChevronRight } from "lucide-react"
import Link from "next/link"

const blogPosts = [
  {
    slug: "ai-transforming-database-querying",
    title: "How AI is Transforming Database Querying: From SQL to Natural Language",
    description: "Discover how AI-powered natural language processing is revolutionizing the way teams interact with their databases, making data accessible to everyone.",
    author: "Yassine ait sidi brahim",
    date: "November 5, 2025",
    readTime: "8 min read",
    category: "AI & Innovation",
    image: "/blog/ai-database.jpg"
  },
  {
    slug: "security-best-practices-database-ai",
    title: "Security Best Practices for Database AI Chatbots: Protecting Your Data",
    description: "Learn essential security measures for implementing AI chatbots that access your databases, including encryption, access control, and compliance.",
    author: "Yassine ait sidi brahim",
    date: "November 1, 2025",
    readTime: "10 min read",
    category: "Security",
    image: "/blog/security.jpg"
  },
  {
    slug: "natural-language-to-sql-how-it-works",
    title: "Natural Language to SQL: How AI Understands Your Questions",
    description: "A deep dive into the technology behind natural language to SQL conversion, exploring how AI models interpret questions and generate accurate queries.",
    author: "Yassine ait sidi brahim",
    date: "October 28, 2025",
    readTime: "12 min read",
    category: "Technology",
    image: "/blog/nlp-sql.jpg"
  }
]

export default function BlogPage() {
  return (
    <div className="min-h-screen bg-gradient-to-b from-background to-muted">
      {/* Navigation */}
      <nav className="border-b bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60 sticky top-0 z-50">
        <div className="container mx-auto px-4 py-4">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2">
              <a href="/">
              <img src="/logo.png" alt="EadgeQuery Logo" className="w-8 h-8" />
              <span className="text-2xl font-bold bg-gradient-to-r from-primary to-blue-600 bg-clip-text text-transparent">
                EadgeQuery
              </span>
              </a>
              <span className="text-xs text-muted-foreground ml-2">v0.1</span>
            </div>
            <div className="flex items-center gap-4">
              <Link href="/">
                <Button variant="ghost">
                  <ArrowLeft className="w-4 h-4 mr-2" />
                  Back to Home
                </Button>
              </Link>
              <Link href="/login">
                <Button variant="ghost">Sign In</Button>
              </Link>
              <Link href="/register">
                <Button>Get Started</Button>
              </Link>
            </div>
          </div>
        </div>
      </nav>

      {/* Hero Section */}
      <section className="container mx-auto px-4 py-20">
        <div className="text-center mb-16">
          <div className="inline-block mb-6">
            <span className="bg-primary/10 text-primary px-4 py-2 rounded-full text-sm font-semibold">
              📝 EadgeQuery Blog
            </span>
          </div>
          <h1 className="text-4xl md:text-6xl font-bold mb-6">
            Insights on{" "}
            <span className="bg-gradient-to-r from-primary to-blue-600 bg-clip-text text-transparent">
              AI & Databases
            </span>
          </h1>
          <p className="text-xl text-muted-foreground max-w-2xl mx-auto">
            Explore the latest trends, best practices, and innovations in AI-powered database querying
          </p>
        </div>

        {/* Blog Posts Grid */}
        <div className="max-w-6xl mx-auto grid md:grid-cols-2 lg:grid-cols-3 gap-8">
          {blogPosts.map((post) => (
            <Card key={post.slug} className="hover:border-primary transition-all duration-300 hover:shadow-lg flex flex-col">
              <div className="w-full h-48 bg-gradient-to-br from-primary/20 to-blue-600/20 rounded-t-lg flex items-center justify-center">
                <div className="text-6xl">📊</div>
              </div>
              <CardHeader>
                <div className="flex items-center gap-2 mb-2">
                  <span className="text-xs font-semibold text-primary bg-primary/10 px-2 py-1 rounded">
                    {post.category}
                  </span>
                </div>
                <CardTitle className="text-xl leading-tight mb-2">
                  {post.title}
                </CardTitle>
                <CardDescription className="line-clamp-2">
                  {post.description}
                </CardDescription>
              </CardHeader>
              <CardContent className="flex-1 flex flex-col justify-end">
                <div className="flex items-center gap-4 text-xs text-muted-foreground mb-4">
                  <div className="flex items-center gap-1">
                    <User className="w-3 h-3" />
                    {post.author}
                  </div>
                  <div className="flex items-center gap-1">
                    <Calendar className="w-3 h-3" />
                    {post.date}
                  </div>
                </div>
                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-1 text-xs text-muted-foreground">
                    <Clock className="w-3 h-3" />
                    {post.readTime}
                  </div>
                  <Link href={`/blog/${post.slug}`}>
                    <Button variant="ghost" size="sm">
                      Read More
                      <ChevronRight className="w-4 h-4 ml-1" />
                    </Button>
                  </Link>
                </div>
              </CardContent>
            </Card>
          ))}
        </div>


      </section>

      {/* Footer */}
      <footer className="bg-muted/50 border-t mt-20">
        <div className="container mx-auto px-4 py-8 text-center text-sm text-muted-foreground">
          <div className="flex items-center justify-center gap-2 mb-2">
            <img src="/logo.png" alt="EadgeQuery Logo" className="w-5 h-5" />
            <span className="font-semibold">EadgeQuery</span>
            <span className="text-xs">v0.1</span>
          </div>
          <p>© 2025 EadgeQuery. All rights reserved.</p>
        </div>
      </footer>
    </div>
  )
}
