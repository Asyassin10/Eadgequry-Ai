"use client"

import { Button } from "@/components/ui/button"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { ArrowLeft, FileText } from "lucide-react"
import Link from "next/link"

export default function TermsPage() {
  return (
    <div className="min-h-screen bg-gradient-to-b from-background to-muted">
      {/* Navigation */}
      <nav className="border-b bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60 sticky top-0 z-50">
        <div className="container mx-auto px-4 py-4">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2">
              <img src="/logo.png" alt="EadgeQuery Logo" className="w-8 h-8" />
              <span className="text-2xl font-bold bg-gradient-to-r from-primary to-blue-600 bg-clip-text text-transparent">
                EadgeQuery
              </span>
              <span className="text-xs text-muted-foreground ml-2">v0.1</span>
            </div>
            <div className="flex items-center gap-4">
              <Link href="/">
                <Button variant="ghost">
                  <ArrowLeft className="w-4 h-4 mr-2" />
                  Back to Home
                </Button>
              </Link>
            </div>
          </div>
        </div>
      </nav>

      {/* Content */}
      <section className="container mx-auto px-4 py-20 max-w-4xl">
        <div className="text-center mb-12">
          <div className="flex justify-center mb-6">
            <div className="w-16 h-16 bg-primary/10 rounded-full flex items-center justify-center">
              <FileText className="w-8 h-8 text-primary" />
            </div>
          </div>
          <h1 className="text-4xl md:text-5xl font-bold mb-4">Terms of Service</h1>
          <p className="text-muted-foreground">Last updated: November 2024</p>
        </div>

        <Card className="mb-8">
          <CardHeader>
            <CardTitle>1. Acceptance of Terms</CardTitle>
          </CardHeader>
          <CardContent className="space-y-4 text-muted-foreground">
            <p>
              By accessing or using EadgeQuery ("Service"), you agree to be bound by these Terms of Service ("Terms"). If you do not agree to these Terms, do not use the Service.
            </p>
          </CardContent>
        </Card>

        <Card className="mb-8">
          <CardHeader>
            <CardTitle>2. Description of Service</CardTitle>
          </CardHeader>
          <CardContent className="space-y-4 text-muted-foreground">
            <p>
              EadgeQuery is an AI-powered database querying platform that allows users to interact with their databases using natural language. The Service includes:
            </p>
            <ul className="list-disc list-inside space-y-2 ml-4">
              <li>Natural language to SQL query generation</li>
              <li>Database connection management</li>
              <li>Conversation history and query tracking</li>
              <li>Data visualization and export capabilities</li>
              <li>AI provider integration (Demo, Claude, OpenAI)</li>
            </ul>
          </CardContent>
        </Card>

        <Card className="mb-8">
          <CardHeader>
            <CardTitle>3. User Accounts</CardTitle>
          </CardHeader>
          <CardContent className="space-y-4 text-muted-foreground">
            <p>
              To use the Service, you must:
            </p>
            <ul className="list-disc list-inside space-y-2 ml-4">
              <li>Create an account with accurate information</li>
              <li>Maintain the security of your account credentials</li>
              <li>Be at least 18 years old or have parental consent</li>
              <li>Not share your account with others</li>
              <li>Notify us immediately of any unauthorized access</li>
            </ul>
          </CardContent>
        </Card>

        <Card className="mb-8">
          <CardHeader>
            <CardTitle>4. Acceptable Use</CardTitle>
          </CardHeader>
          <CardContent className="space-y-4 text-muted-foreground">
            <p>
              You agree NOT to:
            </p>
            <ul className="list-disc list-inside space-y-2 ml-4">
              <li>Use the Service for any illegal purposes</li>
              <li>Attempt to modify, delete, or manipulate database data (Service is read-only)</li>
              <li>Reverse engineer or attempt to extract source code</li>
              <li>Overload or disrupt the Service infrastructure</li>
              <li>Share API keys or credentials with unauthorized parties</li>
              <li>Use the Service to store or transmit malicious code</li>
              <li>Violate any applicable laws or regulations</li>
            </ul>
          </CardContent>
        </Card>

        <Card className="mb-8">
          <CardHeader>
            <CardTitle>5. Subscription and Payments</CardTitle>
          </CardHeader>
          <CardContent className="space-y-4 text-muted-foreground">
            <p>
              The Service offers multiple pricing tiers:
            </p>
            <ul className="list-disc list-inside space-y-2 ml-4">
              <li><strong>Free Plan:</strong> 30 queries/day with Demo AI, 100 queries/day with your own API key</li>
              <li><strong>Professional Plan:</strong> $30/month with unlimited queries using premium AI models</li>
              <li><strong>Enterprise Plan:</strong> Custom pricing with dedicated support</li>
            </ul>
            <p className="mt-4">
              Payments are processed securely. Subscriptions auto-renew unless cancelled. Refunds are provided according to our refund policy.
            </p>
          </CardContent>
        </Card>

        <Card className="mb-8">
          <CardHeader>
            <CardTitle>6. API Keys and Third-Party Services</CardTitle>
          </CardHeader>
          <CardContent className="space-y-4 text-muted-foreground">
            <p>
              When you provide your own API keys (Claude, OpenAI):
            </p>
            <ul className="list-disc list-inside space-y-2 ml-4">
              <li>You are responsible for compliance with the third-party provider's terms</li>
              <li>You are responsible for any costs incurred with the third-party provider</li>
              <li>We encrypt and securely store your API keys using AES-256 encryption</li>
              <li>We never share your API keys with anyone</li>
              <li>You can delete your API keys at any time</li>
            </ul>
          </CardContent>
        </Card>

        <Card className="mb-8">
          <CardHeader>
            <CardTitle>7. Data and Privacy</CardTitle>
          </CardHeader>
          <CardContent className="space-y-4 text-muted-foreground">
            <p>
              We are committed to protecting your data:
            </p>
            <ul className="list-disc list-inside space-y-2 ml-4">
              <li>We do not store your actual database data</li>
              <li>We only execute read-only (SELECT) queries</li>
              <li>We store query history and metadata for service functionality</li>
              <li>All sensitive data is encrypted at rest and in transit</li>
              <li>See our Privacy Policy for complete details</li>
            </ul>
          </CardContent>
        </Card>

        <Card className="mb-8">
          <CardHeader>
            <CardTitle>8. Service Limitations</CardTitle>
          </CardHeader>
          <CardContent className="space-y-4 text-muted-foreground">
            <p>
              The Service has the following limitations:
            </p>
            <ul className="list-disc list-inside space-y-2 ml-4">
              <li>Read-only access (SELECT queries only)</li>
              <li>Query limits based on your plan</li>
              <li>Timeout limits for long-running queries</li>
              <li>Rate limiting to prevent abuse</li>
              <li>Database connection limits based on your plan</li>
            </ul>
          </CardContent>
        </Card>

        <Card className="mb-8">
          <CardHeader>
            <CardTitle>9. Intellectual Property</CardTitle>
          </CardHeader>
          <CardContent className="space-y-4 text-muted-foreground">
            <p>
              The Service and its original content, features, and functionality are owned by EadgeQuery and are protected by international copyright, trademark, and other intellectual property laws.
            </p>
            <p className="mt-4">
              You retain ownership of your data and queries. We retain ownership of the Service and its underlying technology.
            </p>
          </CardContent>
        </Card>

        <Card className="mb-8">
          <CardHeader>
            <CardTitle>10. Disclaimer of Warranties</CardTitle>
          </CardHeader>
          <CardContent className="space-y-4 text-muted-foreground">
            <p>
              THE SERVICE IS PROVIDED "AS IS" AND "AS AVAILABLE" WITHOUT WARRANTIES OF ANY KIND. We do not guarantee that:
            </p>
            <ul className="list-disc list-inside space-y-2 ml-4">
              <li>The Service will be uninterrupted or error-free</li>
              <li>All generated SQL queries will be accurate</li>
              <li>The Service will meet all your requirements</li>
              <li>Any errors will be corrected immediately</li>
            </ul>
          </CardContent>
        </Card>

        <Card className="mb-8">
          <CardHeader>
            <CardTitle>11. Limitation of Liability</CardTitle>
          </CardHeader>
          <CardContent className="space-y-4 text-muted-foreground">
            <p>
              IN NO EVENT SHALL EADGEQUERY BE LIABLE FOR ANY INDIRECT, INCIDENTAL, SPECIAL, CONSEQUENTIAL, OR PUNITIVE DAMAGES ARISING OUT OF YOUR USE OF THE SERVICE.
            </p>
            <p className="mt-4">
              Our total liability shall not exceed the amount you paid us in the 12 months prior to the event giving rise to liability.
            </p>
          </CardContent>
        </Card>

        <Card className="mb-8">
          <CardHeader>
            <CardTitle>12. Termination</CardTitle>
          </CardHeader>
          <CardContent className="space-y-4 text-muted-foreground">
            <p>
              We may terminate or suspend your account and access to the Service:
            </p>
            <ul className="list-disc list-inside space-y-2 ml-4">
              <li>For violations of these Terms</li>
              <li>For fraudulent or illegal activity</li>
              <li>For non-payment of fees</li>
              <li>At our discretion with or without notice</li>
            </ul>
            <p className="mt-4">
              You may terminate your account at any time through your account settings.
            </p>
          </CardContent>
        </Card>

        <Card className="mb-8">
          <CardHeader>
            <CardTitle>13. Changes to Terms</CardTitle>
          </CardHeader>
          <CardContent className="space-y-4 text-muted-foreground">
            <p>
              We reserve the right to modify these Terms at any time. We will notify users of material changes via email or through the Service. Continued use of the Service after changes constitutes acceptance of the new Terms.
            </p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>14. Contact Information</CardTitle>
          </CardHeader>
          <CardContent className="space-y-4 text-muted-foreground">
            <p>
              For questions about these Terms, please contact us at:
            </p>
            <ul className="list-none space-y-2 ml-4">
              <li>Email: legal@eadgequery.com</li>
              <li>Address: [Company Address]</li>
            </ul>
          </CardContent>
        </Card>
      </section>

      {/* Footer */}
      <footer className="bg-muted/50 border-t mt-20">
        <div className="container mx-auto px-4 py-8 text-center text-sm text-muted-foreground">
          <div className="flex items-center justify-center gap-2 mb-2">
            <img src="/logo.png" alt="EadgeQuery Logo" className="w-5 h-5" />
            <span className="font-semibold">EadgeQuery</span>
            <span className="text-xs">v0.1</span>
          </div>
          <p>© 2024 EadgeQuery. All rights reserved.</p>
        </div>
      </footer>
    </div>
  )
}
