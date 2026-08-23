package com.example.ecotech

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform