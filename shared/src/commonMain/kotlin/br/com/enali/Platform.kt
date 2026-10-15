package br.com.enali

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform