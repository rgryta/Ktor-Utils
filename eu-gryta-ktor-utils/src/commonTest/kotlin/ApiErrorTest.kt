import eu.gryta.ktor.utils.ApiError
import eu.gryta.ktor.utils.StatusCodeMapper
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ApiErrorTest {

    @Test
    fun `NetworkError has correct message`() {
        val cause = RuntimeException("connection failed")
        val error = ApiError.NetworkError(cause)
        assertEquals("Network error occurred", error.message)
        assertEquals(cause, error.cause)
    }

    @Test
    fun `TimeoutError has correct message`() {
        val error = ApiError.TimeoutError()
        assertEquals("Request timed out", error.message)
    }

    @Test
    fun `Unauthorized has correct message`() {
        val error = ApiError.Unauthorized()
        assertEquals("Authentication required", error.message)
    }

    @Test
    fun `NotFound formats resource name`() {
        val error = ApiError.NotFound("User")
        assertEquals("User not found", error.message)
    }

    @Test
    fun `TooManyRequests includes retry after`() {
        val error = ApiError.TooManyRequests(60)
        assertTrue { error.message!!.contains("60 seconds") }
        assertEquals(60, error.retryAfterSeconds)
    }

    @Test
    fun `TooManyRequests without retry after`() {
        val error = ApiError.TooManyRequests()
        assertTrue { error.message!!.contains("later") }
        assertNull(error.retryAfterSeconds)
    }

    @Test
    fun `ServerError includes status code`() {
        val error = ApiError.ServerError(502)
        assertEquals(502, error.statusCode)
        assertTrue { error.message!!.contains("502") }
    }

    @Test
    fun `StatusCodeMapper maps 400 to BadRequest`() {
        val error = StatusCodeMapper.map(400)
        assertIs<ApiError.BadRequest>(error)
    }

    @Test
    fun `StatusCodeMapper maps 401 to Unauthorized`() {
        val error = StatusCodeMapper.map(401)
        assertIs<ApiError.Unauthorized>(error)
    }

    @Test
    fun `StatusCodeMapper maps 403 to Forbidden`() {
        val error = StatusCodeMapper.map(403)
        assertIs<ApiError.Forbidden>(error)
    }

    @Test
    fun `StatusCodeMapper maps 404 to NotFound`() {
        val error = StatusCodeMapper.map(404)
        assertIs<ApiError.NotFound>(error)
    }

    @Test
    fun `StatusCodeMapper maps 409 to Conflict`() {
        val error = StatusCodeMapper.map(409)
        assertIs<ApiError.Conflict>(error)
    }

    @Test
    fun `StatusCodeMapper maps 413 to PayloadTooLarge`() {
        val error = StatusCodeMapper.map(413)
        assertIs<ApiError.PayloadTooLarge>(error)
    }

    @Test
    fun `StatusCodeMapper maps 422 to UnprocessableEntity`() {
        val error = StatusCodeMapper.map(422)
        assertIs<ApiError.UnprocessableEntity>(error)
    }

    @Test
    fun `StatusCodeMapper maps 429 to TooManyRequests`() {
        val error = StatusCodeMapper.map(429, retryAfterHeader = "120")
        assertIs<ApiError.TooManyRequests>(error)
        assertEquals(120, error.retryAfterSeconds)
    }

    @Test
    fun `StatusCodeMapper maps 503 to ServiceUnavailable`() {
        val error = StatusCodeMapper.map(503)
        assertIs<ApiError.ServiceUnavailable>(error)
    }

    @Test
    fun `StatusCodeMapper maps 500 to ServerError`() {
        val error = StatusCodeMapper.map(500)
        assertIs<ApiError.ServerError>(error)
        assertEquals(500, error.statusCode)
    }

    @Test
    fun `StatusCodeMapper maps 502 to ServerError`() {
        val error = StatusCodeMapper.map(502)
        assertIs<ApiError.ServerError>(error)
        assertEquals(502, error.statusCode)
    }

    @Test
    fun `StatusCodeMapper maps unknown code to HttpError`() {
        val error = StatusCodeMapper.map(418, errorBody = "I'm a teapot")
        assertIs<ApiError.HttpError>(error)
        assertEquals(418, error.statusCode)
        assertEquals("I'm a teapot", error.errorBody)
    }

    @Test
    fun `BadRequest includes error body`() {
        val error = ApiError.BadRequest(errorBody = "missing field")
        assertEquals("missing field", error.errorBody)
    }
}
