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


class Endpoint(
    val client: HttpClient,
    val url: String,
    private val apiInstanceClass: KClass<out ApiInstance> = ApiInstance::class,
) {
    suspend fun getInstance(): ApiInstance = ApiInstance.getInstance(apiInstanceClass)

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

    @Suppress("UNUSED")
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

    @Suppress("UNUSED")
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

    @Suppress("UNUSED")
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

    @Suppress("UNUSED")
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

    @Suppress("UNUSED")
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

    @Suppress("UNUSED")
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

    @Suppress("UNUSED")
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