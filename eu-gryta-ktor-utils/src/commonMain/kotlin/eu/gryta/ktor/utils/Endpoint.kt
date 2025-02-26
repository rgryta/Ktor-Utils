package eu.gryta.ktor.utils

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.get
import io.ktor.client.request.head
import io.ktor.client.request.headers
import io.ktor.client.request.options
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HeadersBuilder
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.ParametersBuilder
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.http.parameters
import io.ktor.util.reflect.TypeInfo
import io.ktor.util.reflect.typeInfo
import kotlin.reflect.KClass


data class ResponseWrapper<T : Any>(
    val response: HttpResponse,
    private val typeInfo: TypeInfo
) {
    val status: HttpStatusCode = response.status

    suspend fun body(): T {
        if (status.isSuccess()) {
            return response.body(typeInfo) as T
        } else {
            throw IllegalStateException("Response was not successful: $status")
        }
    }
}

class Endpoint(
    val client: HttpClient,
    val url: String,
    private val apiInstanceClass: KClass<out ApiInstance> = ApiInstance::class,
) {
    suspend fun getInstance(): ApiInstance = ApiInstance.getInstance(apiInstanceClass)

    suspend inline fun <reified T : Any> get(
        crossinline headers: HeadersBuilder.() -> Unit = { },
    ): ResponseWrapper<T> {
        val instance = getInstance()
        val response: HttpResponse = client.get(url) {
            headers {
                headers()

                // Authorize if available
                this[HttpHeaders.Authorization] ?: instance.token?.let { token ->
                    append(HttpHeaders.Authorization, token.toString())
                }
            }
        }
        return ResponseWrapper(response, typeInfo<T>())
    }

    @Suppress("UNUSED")
    suspend inline fun <reified T : Any> post(
        body: Any,
        contentType: ContentType = ContentType.Application.Json,
        crossinline headers: HeadersBuilder.() -> Unit = { }
    ): ResponseWrapper<T> {
        val instance = getInstance()
        val response: HttpResponse = client.post(url) {
            headers {
                headers()

                // Authorize if available
                this[HttpHeaders.Authorization] ?: instance.token?.let { token ->
                    append(HttpHeaders.Authorization, token.toString())
                }
            }
            contentType(contentType)
            setBody(body)
        }
        return ResponseWrapper(response, typeInfo<T>())
    }

    @Suppress("UNUSED")
    suspend inline fun <reified T : Any> put(
        body: Any,
        contentType: ContentType = ContentType.Application.Json,
        crossinline headers: HeadersBuilder.() -> Unit = { }
    ): ResponseWrapper<T> {
        val instance = getInstance()
        val response: HttpResponse = client.put(url) {
            headers {
                headers()

                // Authorize if available
                this[HttpHeaders.Authorization] ?: instance.token?.let { token ->
                    append(HttpHeaders.Authorization, token.toString())
                }
            }
            contentType(contentType)
            setBody(body)
        }
        return ResponseWrapper(response, typeInfo<T>())
    }

    @Suppress("UNUSED")
    suspend inline fun <reified T : Any> delete(
        crossinline headers: HeadersBuilder.() -> Unit = { }
    ): ResponseWrapper<T> {
        val instance = getInstance()
        val response: HttpResponse = client.delete(url) {
            headers {
                headers()

                // Authorize if available
                this[HttpHeaders.Authorization] ?: instance.token?.let { token ->
                    append(HttpHeaders.Authorization, token.toString())
                }
            }
        }
        return ResponseWrapper(response, typeInfo<T>())
    }

    @Suppress("UNUSED")
    suspend inline fun <reified T : Any> options(
        crossinline headers: HeadersBuilder.() -> Unit = { },
    ): ResponseWrapper<T> {
        val instance = getInstance()
        val response: HttpResponse = client.options(url) {
            headers {
                headers()

                // Authorize if available
                this[HttpHeaders.Authorization] ?: instance.token?.let { token ->
                    append(HttpHeaders.Authorization, token.toString())
                }
            }
        }
        return ResponseWrapper(response, typeInfo<T>())
    }

    @Suppress("UNUSED")
    suspend inline fun <reified T : Any> patch(
        crossinline headers: HeadersBuilder.() -> Unit = { },
    ): ResponseWrapper<T> {
        val instance = getInstance()
        val response: HttpResponse = client.patch(url) {
            headers {
                headers()

                // Authorize if available
                this[HttpHeaders.Authorization] ?: instance.token?.let { token ->
                    append(HttpHeaders.Authorization, token.toString())
                }
            }
        }
        return ResponseWrapper(response, typeInfo<T>())
    }

    @Suppress("UNUSED")
    suspend inline fun <reified T : Any> head(
        crossinline headers: HeadersBuilder.() -> Unit = { },
    ): ResponseWrapper<T> {
        val instance = getInstance()
        val response: HttpResponse = client.head(url) {
            headers {
                headers()

                // Authorize if available
                this[HttpHeaders.Authorization] ?: instance.token?.let { token ->
                    append(HttpHeaders.Authorization, token.toString())
                }
            }
        }
        return ResponseWrapper(response, typeInfo<T>())
    }

    @Suppress("UNUSED")
    suspend inline fun <reified T : Any> submitForm(
        crossinline formParameters: ParametersBuilder.() -> Unit = {},
        crossinline headers: HeadersBuilder.() -> Unit = { },
    ): ResponseWrapper<T> {
        val instance = getInstance()
        val response: HttpResponse = client.submitForm(url = url,
            formParameters = parameters {
                formParameters()
            }) {
            headers {
                headers()

                // Authorize if available
                this[HttpHeaders.Authorization] ?: instance.token?.let { token ->
                    append(HttpHeaders.Authorization, token.toString())
                }
            }
        }
        return ResponseWrapper(response, typeInfo<T>())
    }
}