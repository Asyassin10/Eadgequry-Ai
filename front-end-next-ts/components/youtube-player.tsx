"use client"

import { useRef } from "react"

interface BackgroundVideoProps {
    videoSrc: string
    title: string
}

export function BackgroundVideo({ videoSrc, title }: BackgroundVideoProps) {
    const videoRef = useRef<HTMLVideoElement>(null)

    return (
        <div className="relative w-auto h-auto overflow-hidden rounded-lg bg-black">
 
            <video
                ref={videoRef}
                autoPlay
                muted
                loop   // native loop
                playsInline
                className="w-full h-full object-cover"
                title={title}
            >
                <source src={videoSrc} type="video/mp4" />
                Your browser does not support the video tag.
            </video>

            {/* Optional overlay content */}
            <div className="absolute inset-0 flex items-center justify-center pointer-events-none">
                <h1 className="text-white text-3xl font-bold">{title}</h1>
            </div>
        </div>
    )
}
