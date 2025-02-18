package eu.gryta.ktorUtils

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform