import eu.gryta.ktor.utils.ApiError
import eu.gryta.ktor.utils.RetryConfig
import eu.gryta.ktor.utils.isRetryable
import eu.gryta.ktor.utils.isRetryableStatusCode
import eu.gryta.ktor.utils.withRetry
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RetryTest {

    @Test
    fun `RetryConfig validates maxAttempts`() {
        assertFailsWith<IllegalArgumentException> {
            RetryConfig(maxAttempts = 0)
        }
    }

    @Test
    fun `RetryConfig validates initialDelayMs`() {
        assertFailsWith<IllegalArgumentException> {
            RetryConfig(initialDelayMs = 0)
        }
    }

    @Test
    fun `RetryConfig validates maxDelayMs`() {
        assertFailsWith<IllegalArgumentException> {
            RetryConfig(initialDelayMs = 1000, maxDelayMs = 500)
        }
    }

    @Test
    fun `RetryConfig validates backoffMultiplier`() {
        assertFailsWith<IllegalArgumentException> {
            RetryConfig(backoffMultiplier = 0.5)
        }
    }

    @Test
    fun `withRetry succeeds on first attempt`() = runTest {
        var attempts = 0
        val result = withRetry {
            attempts++
            "success"
        }
        assertEquals("success", result)
        assertEquals(1, attempts)
    }

    @Test
    fun `withRetry retries on retryable error`() = runTest {
        var attempts = 0
        val result = withRetry(RetryConfig(maxAttempts = 3, initialDelayMs = 1)) {
            attempts++
            if (attempts < 3) throw ApiError.TimeoutError()
            "success"
        }
        assertEquals("success", result)
        assertEquals(3, attempts)
    }

    @Test
    fun `withRetry respects maxAttempts`() = runTest {
        var attempts = 0
        assertFailsWith<ApiError.TimeoutError> {
            withRetry(RetryConfig(maxAttempts = 2, initialDelayMs = 1)) {
                attempts++
                throw ApiError.TimeoutError()
            }
        }
        assertEquals(2, attempts)
    }

    @Test
    fun `withRetry fails immediately on non-retryable error`() = runTest {
        var attempts = 0
        assertFailsWith<ApiError.Unauthorized> {
            withRetry(RetryConfig(maxAttempts = 3, initialDelayMs = 1)) {
                attempts++
                throw ApiError.Unauthorized()
            }
        }
        assertEquals(1, attempts)
    }

    @Test
    fun `TimeoutError is retryable`() {
        assertTrue { ApiError.TimeoutError().isRetryable() }
    }

    @Test
    fun `NetworkError is retryable`() {
        assertTrue { ApiError.NetworkError(RuntimeException()).isRetryable() }
    }

    @Test
    fun `ConnectionError is retryable`() {
        assertTrue { ApiError.ConnectionError(RuntimeException()).isRetryable() }
    }

    @Test
    fun `ServiceUnavailable is retryable`() {
        assertTrue { ApiError.ServiceUnavailable().isRetryable() }
    }

    @Test
    fun `TooManyRequests is retryable`() {
        assertTrue { ApiError.TooManyRequests().isRetryable() }
    }

    @Test
    fun `ServerError 502 is retryable`() {
        assertTrue { ApiError.ServerError(502).isRetryable() }
    }

    @Test
    fun `ServerError 500 is not retryable`() {
        assertFalse { ApiError.ServerError(500).isRetryable() }
    }

    @Test
    fun `Unauthorized is not retryable`() {
        assertFalse { ApiError.Unauthorized().isRetryable() }
    }

    @Test
    fun `NotFound is not retryable`() {
        assertFalse { ApiError.NotFound().isRetryable() }
    }

    @Test
    fun `status code 429 is retryable`() {
        assertTrue { 429.isRetryableStatusCode() }
    }

    @Test
    fun `status code 503 is retryable`() {
        assertTrue { 503.isRetryableStatusCode() }
    }

    @Test
    fun `status code 200 is not retryable`() {
        assertFalse { 200.isRetryableStatusCode() }
    }

    @Test
    fun `status code 404 is not retryable`() {
        assertFalse { 404.isRetryableStatusCode() }
    }
}
