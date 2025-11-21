import type React from "react";
import { Geist, Geist_Mono } from "next/font/google";
import "./globals.css";
import ThemeProvider from "./ThemeProvider";

const geist = Geist({ subsets: ["latin"] });
const geistMono = Geist_Mono({ subsets: ["latin"] });

export const metadata = {
  generator: "v0.app",
  title: "EadgeQuery - Advanced Data Solutions",
  description: "EadgeQuery provides high-performance database management tools with AI-powered insights. Connect, manage, and analyze your data effortlessly.",
  keywords: "EadgeQuery, Database Management, AI Analytics, MySQL, PostgreSQL, Oracle, SQL Server",
  author: "Yassine ait sidi brahim",
  robots: "index, follow",
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="en" suppressHydrationWarning>
      <head>
        <title>{metadata.title}</title>
        <meta name="description" content={metadata.description} />
        <meta name="keywords" content={metadata.keywords} />
        <meta name="author" content={metadata.author} />
        <meta name="robots" content={metadata.robots} />
        <link rel="icon" type="image/png" href="/logo.png" />

      </head>
      <body className={`${geist.className} antialiased`} suppressHydrationWarning>
        <ThemeProvider>{children}</ThemeProvider>
      </body>
    </html>
  );
}
