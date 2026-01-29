package eu.gryta.ktor.utils

import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.http.isSuccess
import io.ktor.util.reflect.TypeInfo

data class ResponseWrapper<T : Any>(
    val response: HttpResponse,
    private val typeInfo: TypeInfo
) {
    val status: HttpStatusCode = response.status

    suspend fun body(): T {
        if (status.isSuccess()) {
            @Suppress("UNCHECKED_CAST")
            return response.body(typeInfo) as T
        } else {
            throw IllegalStateException("Response was not successful: $status")
        }
    }

    suspend fun bodyOrNull(): T? {
        return if (status.isSuccess()) {
            @Suppress("UNCHECKED_CAST")
            response.body(typeInfo) as T
        } else {
            null
        }
    }

    suspend fun errorBody(): String? {
        return if (!status.isSuccess()) {
            response.bodyAsText()
        } else {
            null
        }
    }

    suspend inline fun <reified E : Any> errorBodyAs(): E? {
        return if (!status.isSuccess()) {
            response.body<E>()
        } else {
            null
        }
    }

    fun isSuccess(): Boolean = status.isSuccess()

    fun isClientError(): Boolean = status.value in 400..499

    fun isServerError(): Boolean = status.value in 500..599
}
