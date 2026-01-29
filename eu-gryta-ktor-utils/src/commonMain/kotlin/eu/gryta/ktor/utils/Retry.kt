package eu.gryta.ktor.utils

import kotlinx.coroutines.delay
import kotlin.math.pow

data class RetryConfig(
    val maxAttempts: Int = 3,
    val initialDelayMs: Long = 1000,
    val maxDelayMs: Long = 10000,
    val backoffMultiplier: Double = 2.0,
    val retryOn: (Throwable) -> Boolean = { it.isRetryable() }
) {
    init {
        require(maxAttempts >= 1) { "maxAttempts must be >= 1" }
        require(initialDelayMs > 0) { "initialDelayMs must be > 0" }
        require(maxDelayMs >= initialDelayMs) { "maxDelayMs must be >= initialDelayMs" }
        require(backoffMultiplier >= 1.0) { "backoffMultiplier must be >= 1.0" }
    }
}

fun Throwable.isRetryable(): Boolean = when (this) {
    is ApiError.TimeoutError -> true
    is ApiError.NetworkError -> true
    is ApiError.ConnectionError -> true
    is ApiError.ServiceUnavailable -> true
    is ApiError.TooManyRequests -> true
    is ApiError.ServerError -> this.statusCode in listOf(502, 503, 504)
    else -> false
}

fun Int.isRetryableStatusCode(): Boolean = this in listOf(408, 429, 500, 502, 503, 504)

suspend fun <T> withRetry(
    config: RetryConfig = RetryConfig(),
    block: suspend () -> T
): T {
    var lastException: Throwable? = null
    var currentDelay = config.initialDelayMs

    repeat(config.maxAttempts) { attempt ->
        try {
            return block()
        } catch (e: Throwable) {
            lastException = e

            if (attempt == config.maxAttempts - 1 || !config.retryOn(e)) {
                throw e
            }

            val retryAfter = (e as? ApiError.TooManyRequests)?.retryAfterSeconds
            val delayMs = if (retryAfter != null) {
                (retryAfter * 1000L).coerceAtMost(config.maxDelayMs)
            } else {
                currentDelay.coerceAtMost(config.maxDelayMs)
            }

            delay(delayMs)
            currentDelay = (currentDelay * config.backoffMultiplier).toLong()
        }
    }

    throw lastException ?: IllegalStateException("Retry failed with no exception")
}

suspend inline fun <reified T : Any> retryRequest(
    config: RetryConfig = RetryConfig(),
    crossinline request: suspend () -> ResponseWrapper<T>
): ResponseWrapper<T> {
    return withRetry(config) {
        val response = request()
        if (response.status.value.isRetryableStatusCode()) {
            throw StatusCodeMapper.map(response.status.value, response.errorBody())
        }
        response
    }
}
