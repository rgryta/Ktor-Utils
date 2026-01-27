package eu.gryta.ktor.utils

import io.ktor.client.HttpClient
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.delete
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.get
import io.ktor.client.request.head
import io.ktor.client.request.headers
import io.ktor.client.request.options
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpHeaders
import io.ktor.http.ParametersBuilder
import io.ktor.http.parameters
import io.ktor.util.reflect.typeInfo
import kotlin.reflect.KClass

/**
 * A type-safe HTTP endpoint wrapper for making API requests with Ktor.
 *
 * Endpoint simplifies HTTP request handling by automatically managing authorization headers
 * through [ApiInstance] and providing type-safe response wrapping via [ResponseWrapper].
 *
 * Example usage:
 * ```kotlin
 * val endpoint = Endpoint(client = httpClient, url = "https://api.example.com/users/1")
 * val response: ResponseWrapper<User> = endpoint.get()
 * if (response.status.isSuccess()) {
 *     val user = response.body()
 * }
 * ```
 *
 * @property client The Ktor [HttpClient] used for making requests.
 * @property url The target URL for this endpoint.
 * @param apiInstanceClass The [ApiInstance] class to use for token management.
 *        Defaults to the base [ApiInstance] class.
 *
 * @see ApiInstance for token management
 * @see ResponseWrapper for response handling
 */
class Endpoint(
    val client: HttpClient,
    val url: String,
    private val apiInstanceClass: KClass<out ApiInstance> = ApiInstance::class,
) {
    /**
     * Retrieves the [ApiInstance] associated with this endpoint.
     *
     * @return The [ApiInstance] for managing authentication tokens.
     */
    suspend fun getInstance(): ApiInstance = ApiInstance.getInstance(apiInstanceClass)

    /**
     * Performs an HTTP GET request to this endpoint.
     *
     * Automatically includes the Authorization header if a token is set on the [ApiInstance]
     * and no Authorization header is explicitly provided in the request block.
     *
     * @param T The expected response body type.
     * @param block Optional configuration block for the HTTP request.
     * @return A [ResponseWrapper] containing the response with type information.
     */
    suspend inline fun <reified T : Any> get(
        crossinline block: HttpRequestBuilder.() -> Unit = { },
    ): ResponseWrapper<T> {
        val instance = getInstance()
        val response: HttpResponse = client.get(url) {
            headers {
                this[HttpHeaders.Authorization] ?: instance.token?.let { token ->
                    append(HttpHeaders.Authorization, token.toString())
                }
            }
            block()
        }
        return ResponseWrapper(response, typeInfo<T>())
    }

    /**
     * Performs an HTTP POST request to this endpoint.
     *
     * Automatically includes the Authorization header if a token is set on the [ApiInstance]
     * and no Authorization header is explicitly provided in the request block.
     *
     * @param T The expected response body type.
     * @param block Optional configuration block for the HTTP request (typically used to set body).
     * @return A [ResponseWrapper] containing the response with type information.
     */
    suspend inline fun <reified T : Any> post(
        crossinline block: HttpRequestBuilder.() -> Unit = { },
    ): ResponseWrapper<T> {
        val instance = getInstance()
        val response: HttpResponse = client.post(url) {
            headers {
                this[HttpHeaders.Authorization] ?: instance.token?.let { token ->
                    append(HttpHeaders.Authorization, token.toString())
                }
            }
            block()
        }
        return ResponseWrapper(response, typeInfo<T>())
    }

    /**
     * Performs an HTTP PUT request to this endpoint.
     *
     * Automatically includes the Authorization header if a token is set on the [ApiInstance]
     * and no Authorization header is explicitly provided in the request block.
     *
     * @param T The expected response body type.
     * @param block Optional configuration block for the HTTP request.
     * @return A [ResponseWrapper] containing the response with type information.
     */
    suspend inline fun <reified T : Any> put(
        crossinline block: HttpRequestBuilder.() -> Unit = { },
    ): ResponseWrapper<T> {
        val instance = getInstance()
        val response: HttpResponse = client.put(url) {
            headers {
                this[HttpHeaders.Authorization] ?: instance.token?.let { token ->
                    append(HttpHeaders.Authorization, token.toString())
                }
            }
            block()
        }
        return ResponseWrapper(response, typeInfo<T>())
    }

    /**
     * Performs an HTTP DELETE request to this endpoint.
     *
     * Automatically includes the Authorization header if a token is set on the [ApiInstance]
     * and no Authorization header is explicitly provided in the request block.
     *
     * @param T The expected response body type.
     * @param block Optional configuration block for the HTTP request.
     * @return A [ResponseWrapper] containing the response with type information.
     */
    suspend inline fun <reified T : Any> delete(
        crossinline block: HttpRequestBuilder.() -> Unit = { },
    ): ResponseWrapper<T> {
        val instance = getInstance()
        val response: HttpResponse = client.delete(url) {
            headers {
                this[HttpHeaders.Authorization] ?: instance.token?.let { token ->
                    append(HttpHeaders.Authorization, token.toString())
                }
            }
            block()
        }
        return ResponseWrapper(response, typeInfo<T>())
    }

    /**
     * Performs an HTTP OPTIONS request to this endpoint.
     *
     * Automatically includes the Authorization header if a token is set on the [ApiInstance]
     * and no Authorization header is explicitly provided in the request block.
     *
     * @param T The expected response body type.
     * @param block Optional configuration block for the HTTP request.
     * @return A [ResponseWrapper] containing the response with type information.
     */
    suspend inline fun <reified T : Any> options(
        crossinline block: HttpRequestBuilder.() -> Unit = { },
    ): ResponseWrapper<T> {
        val instance = getInstance()
        val response: HttpResponse = client.options(url) {
            headers {
                this[HttpHeaders.Authorization] ?: instance.token?.let { token ->
                    append(HttpHeaders.Authorization, token.toString())
                }
            }
            block()
        }
        return ResponseWrapper(response, typeInfo<T>())
    }

    /**
     * Performs an HTTP PATCH request to this endpoint.
     *
     * Automatically includes the Authorization header if a token is set on the [ApiInstance]
     * and no Authorization header is explicitly provided in the request block.
     *
     * @param T The expected response body type.
     * @param block Optional configuration block for the HTTP request.
     * @return A [ResponseWrapper] containing the response with type information.
     */
    suspend inline fun <reified T : Any> patch(
        crossinline block: HttpRequestBuilder.() -> Unit = { },
    ): ResponseWrapper<T> {
        val instance = getInstance()
        val response: HttpResponse = client.patch(url) {
            headers {
                this[HttpHeaders.Authorization] ?: instance.token?.let { token ->
                    append(HttpHeaders.Authorization, token.toString())
                }
            }
            block()
        }
        return ResponseWrapper(response, typeInfo<T>())
    }

    /**
     * Performs an HTTP HEAD request to this endpoint.
     *
     * Automatically includes the Authorization header if a token is set on the [ApiInstance]
     * and no Authorization header is explicitly provided in the request block.
     *
     * @param T The expected response body type.
     * @param block Optional configuration block for the HTTP request.
     * @return A [ResponseWrapper] containing the response with type information.
     */
    suspend inline fun <reified T : Any> head(
        crossinline block: HttpRequestBuilder.() -> Unit = { },
    ): ResponseWrapper<T> {
        val instance = getInstance()
        val response: HttpResponse = client.head(url) {
            headers {
                this[HttpHeaders.Authorization] ?: instance.token?.let { token ->
                    append(HttpHeaders.Authorization, token.toString())
                }
            }
            block()
        }
        return ResponseWrapper(response, typeInfo<T>())
    }

    /**
     * Submits form data to this endpoint using application/x-www-form-urlencoded encoding.
     *
     * Automatically includes the Authorization header if a token is set on the [ApiInstance]
     * and no Authorization header is explicitly provided in the request block.
     *
     * @param T The expected response body type.
     * @param formParameters Builder for form parameters.
     * @param block Optional configuration block for the HTTP request.
     * @return A [ResponseWrapper] containing the response with type information.
     */
    suspend inline fun <reified T : Any> submitForm(
        crossinline formParameters: ParametersBuilder.() -> Unit = {},
        crossinline block: HttpRequestBuilder.() -> Unit = { },
    ): ResponseWrapper<T> {
        val instance = getInstance()
        val response: HttpResponse = client.submitForm(
            url = url,
            formParameters = parameters {
                formParameters()
            }) {
            headers {
                this[HttpHeaders.Authorization] ?: instance.token?.let { token ->
                    append(HttpHeaders.Authorization, token.toString())
                }
            }
            block()
        }
        return ResponseWrapper(response, typeInfo<T>())
    }
}
