'use client'

import { useState } from 'react'
import { Play } from 'lucide-react'

interface YouTubePlayerProps {
    videoId: string
    title?: string
}

export function YouTubePlayer({ videoId, title = 'Product Demo' }: YouTubePlayerProps) {
    const [isOpen, setIsOpen] = useState(false)

    if (isOpen) {
        return (
            <div className="relative w-full h-full rounded-xl overflow-hidden bg-black">
                <iframe
                    width="100%"
                    height="410"
                    src={`https://www.youtube.com/embed/${videoId}?autoplay=1`}
                    title={title}
                     allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
                    allowFullScreen
                    className="rounded-xl"
                />
            </div>
        )
    }

    return (
        <div
            onClick={() => setIsOpen(true)}
            className="relative w-full cursor-pointer group overflow-hidden rounded-xl"
        >
            {/* YouTube Thumbnail */}
            <img
                src={`https://img.youtube.com/vi/${videoId}/maxresdefault.jpg`}
                alt={title}
                className="w-full h-auto rounded-xl transition-transform duration-300 group-hover:scale-105"
            />

            {/* Play Button Overlay */}
            <div className="absolute inset-0 bg-black/30 group-hover:bg-black/40 transition-colors duration-300 rounded-xl flex items-center justify-center">
                <div className="bg-blue-500 rounded-full p-4 group-hover:bg-blue-600 transition-colors duration-300 transform group-hover:scale-110">
                    <Play className="w-8 h-8 text-white fill-white" />
                 </div>
            </div>
        </div>
    )
}
