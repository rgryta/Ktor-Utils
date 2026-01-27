package eu.gryta.ktor.utils

import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import io.ktor.http.isSuccess
import io.ktor.util.reflect.TypeInfo

/**
 * A type-safe wrapper for HTTP responses from Ktor.
 *
 * ResponseWrapper encapsulates an [HttpResponse] along with type information,
 * enabling safe deserialization of the response body to the expected type.
 *
 * ## Usage
 * ```kotlin
 * val response: ResponseWrapper<User> = endpoint.get()
 *
 * // Check status before accessing body
 * if (response.status.isSuccess()) {
 *     val user: User = response.body()
 *     println("Got user: ${user.name}")
 * } else {
 *     println("Request failed: ${response.status}")
 * }
 * ```
 *
 * @param T The expected type of the response body.
 * @property response The underlying Ktor [HttpResponse].
 * @property status The HTTP status code of the response.
 *
 * @see Endpoint for creating ResponseWrapper instances
 */
data class ResponseWrapper<T : Any>(
    val response: HttpResponse,
    private val typeInfo: TypeInfo
) {
    /**
     * The HTTP status code of the response.
     *
     * Convenience property for `response.status`.
     */
    val status: HttpStatusCode = response.status

    /**
     * Deserializes and returns the response body.
     *
     * This method should only be called when the response was successful.
     * Calling it on a non-successful response will throw an [IllegalStateException].
     *
     * @return The deserialized response body of type [T].
     * @throws IllegalStateException if the response status is not successful (2xx).
     */
    suspend fun body(): T {
        if (status.isSuccess()) {
            @Suppress("UNCHECKED_CAST")
            return response.body(typeInfo) as T
        } else {
            throw IllegalStateException("Response was not successful: $status")
        }
    }
}
