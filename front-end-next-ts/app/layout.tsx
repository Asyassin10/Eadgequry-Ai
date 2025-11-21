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
  author: "EadgeQuery Team",
  robots: "index, follow",
  openGraph: {
    title: "EadgeQuery - Advanced Data Solutions",
    description: "EadgeQuery provides high-performance database management tools with AI-powered insights. Connect, manage, and analyze your data effortlessly.",
    type: "website",
    url: "https://www.eadgequery.com",
    site_name: "EadgeQuery",
    images: [
      {
        url: "https://www.eadgequery.com/og-image.jpg",
        width: 1200,
        height: 630,
        alt: "EadgeQuery Open Graph Image",
      },
    ],
  },
  twitter: {
    card: "summary_large_image",
    site: "@EadgeQuery",
    title: "EadgeQuery - Advanced Data Solutions",
    description: "EadgeQuery provides high-performance database management tools with AI-powered insights. Connect, manage, and analyze your data effortlessly.",
    image: "https://www.eadgequery.com/og-image.jpg",
  },
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

        {/* Open Graph */}
        <meta property="og:title" content={metadata.openGraph.title} />
        <meta property="og:description" content={metadata.openGraph.description} />
        <meta property="og:type" content={metadata.openGraph.type} />
        <meta property="og:url" content={metadata.openGraph.url} />
        <meta property="og:site_name" content={metadata.openGraph.site_name} />
        <meta property="og:image" content={metadata.openGraph.images[0].url} />
        <meta property="og:image:width" content={`${metadata.openGraph.images[0].width}`} />
        <meta property="og:image:height" content={`${metadata.openGraph.images[0].height}`} />
        <meta property="og:image:alt" content={metadata.openGraph.images[0].alt} />

        {/* Twitter Card */}
        <meta name="twitter:card" content={metadata.twitter.card} />
        <meta name="twitter:site" content={metadata.twitter.site} />
        <meta name="twitter:title" content={metadata.twitter.title} />
        <meta name="twitter:description" content={metadata.twitter.description} />
        <meta name="twitter:image" content={metadata.twitter.image} />
      </head>
      <body className={`${geist.className} antialiased`} suppressHydrationWarning>
        <ThemeProvider>{children}</ThemeProvider>
      </body>
    </html>
  );
}
