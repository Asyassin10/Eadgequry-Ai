 
import type React from "react";
import { Geist, Geist_Mono } from "next/font/google";
import "./globals.css";
import ThemeProvider from "./ThemeProvider"; // 👈 ملف العميل الجديد

const geist = Geist({ subsets: ["latin"] });
const geistMono = Geist_Mono({ subsets: ["latin"] });

export const metadata = {
  generator: "v0.app",
};

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="en">
      <body className={`${geist.className} antialiased`}>
        <ThemeProvider>{children}</ThemeProvider>
      </body>
    </html>
  );
}
