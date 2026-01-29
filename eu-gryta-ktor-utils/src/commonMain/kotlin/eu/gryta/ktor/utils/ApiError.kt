package eu.gryta.ktor.utils

sealed class ApiError(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause) {

    class NetworkError(cause: Throwable) : ApiError("Network error occurred", cause)
    class TimeoutError(message: String = "Request timed out") : ApiError(message)
    class ConnectionError(cause: Throwable) : ApiError("Failed to connect to server", cause)

    class BadRequest(
        message: String = "Invalid request",
        val errorBody: String? = null
    ) : ApiError(message)

    class Unauthorized(message: String = "Authentication required") : ApiError(message)
    class Forbidden(message: String = "Access denied") : ApiError(message)
    class NotFound(resource: String = "Resource") : ApiError("$resource not found")
    class Conflict(message: String = "Resource conflict") : ApiError(message)

    class UnprocessableEntity(
        message: String = "Validation failed",
        val errors: Map<String, List<String>> = emptyMap()
    ) : ApiError(message)

    class TooManyRequests(
        val retryAfterSeconds: Int? = null
    ) : ApiError("Too many requests. Please try again${retryAfterSeconds?.let { " after $it seconds" } ?: " later"}.")

    class PayloadTooLarge(message: String = "Request payload too large") : ApiError(message)

    class ServerError(
        val statusCode: Int,
        message: String = "Server error"
    ) : ApiError("HTTP $statusCode: $message")

    class ServiceUnavailable(message: String = "Service temporarily unavailable") : ApiError(message)

    class DeserializationError(cause: Throwable) : ApiError("Failed to parse response", cause)

    class HttpError(
        val statusCode: Int,
        message: String = "HTTP error",
        val errorBody: String? = null
    ) : ApiError("HTTP $statusCode: $message")
}

object StatusCodeMapper {
    fun map(
        statusCode: Int,
        errorBody: String? = null,
        retryAfterHeader: String? = null
    ): ApiError = when (statusCode) {
        400 -> ApiError.BadRequest(errorBody = errorBody)
        401 -> ApiError.Unauthorized()
        403 -> ApiError.Forbidden()
        404 -> ApiError.NotFound()
        409 -> ApiError.Conflict()
        413 -> ApiError.PayloadTooLarge()
        422 -> ApiError.UnprocessableEntity()
        429 -> ApiError.TooManyRequests(retryAfterHeader?.toIntOrNull())
        503 -> ApiError.ServiceUnavailable()
        in 500..599 -> ApiError.ServerError(statusCode)
        else -> ApiError.HttpError(statusCode, errorBody = errorBody)
    }
}
