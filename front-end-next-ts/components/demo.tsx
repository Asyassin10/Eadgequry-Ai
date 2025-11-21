"use client"

import { useState } from "react"
import { Play } from "lucide-react"

interface YouTubePlayerProps {
    videoId: string
    title?: string
}

export function Demo({ videoId, title = "Product Demo" }: YouTubePlayerProps) {
    const [isOpen, setIsOpen] = useState(false)

    if (isOpen) {
        return (
            <div className=" mt-10 relative w-250 aspect-video cursor-pointer group overflow-hidden rounded-xl shadow-xl ml-60">
                <iframe
                    src={`https://www.youtube.com/embed/${videoId}?autoplay=1`}
                    title={title}
                    allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
                    allowFullScreen
                    className="w-full h-full rounded-xl"
                />
            </div>
        )
    }

    return (
        <div
            onClick={() => setIsOpen(true)}
            className=" mt-10 relative w-250 aspect-video cursor-pointer group overflow-hidden rounded-xl shadow-xl ml-60"
        >
            <img
                src="/logo.png"
                alt={title}
                className="w-full h-full object-cover rounded-xl transition-transorm duration-300 group-hover:scale-105"
            />

            {/* Play Button Overlay */}
            <div className="absolute inset-0 bg-black/30 group-hover:bg-black/40 transition-colors duration-300 rounded-xl flex items-center justify-center">
                <div className="bg-blue-500 rounded-full p-3 group-hover:bg-blue-600 transition-colors duration-300 transform group-hover:scale-110">
                    <Play className="w-6 h-6 text-white fill-white" />
                </div>
            </div>
        </div>

    )
}
