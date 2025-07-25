package eu.gryta.ktor.utils

import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
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
            return response.body(typeInfo) as T
        } else {
            throw IllegalStateException("Response was not successful: $status")
        }
    }
}
