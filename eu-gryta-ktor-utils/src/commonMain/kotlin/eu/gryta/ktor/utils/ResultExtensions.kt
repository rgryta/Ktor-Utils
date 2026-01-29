package eu.gryta.ktor.utils

import io.ktor.http.isSuccess

suspend fun <T : Any> ResponseWrapper<T>.unwrapOrThrow(
    mapper: (Int, String?) -> ApiError = { code, body ->
        StatusCodeMapper.map(code, body, response.headers["Retry-After"])
    }
): T {
    return when {
        status.isSuccess() -> {
            try {
                body()
            } catch (e: Exception) {
                throw ApiError.DeserializationError(e)
            }
        }
        else -> {
            val errorBody = try { errorBody() } catch (_: Exception) { null }
            throw mapper(status.value, errorBody)
        }
    }
}

suspend fun <T : Any> ResponseWrapper<T>.toResult(
    mapper: (Int, String?) -> ApiError = { code, body ->
        StatusCodeMapper.map(code, body, response.headers["Retry-After"])
    }
): Result<T> {
    return try {
        Result.success(unwrapOrThrow(mapper))
    } catch (e: ApiError) {
        Result.failure(e)
    }
}

suspend fun <T : Any> ResponseWrapper<T>.getOrNull(): T? {
    return try {
        if (status.isSuccess()) body() else null
    } catch (_: Exception) {
        null
    }
}

suspend fun <T : Any> ResponseWrapper<T>.getOrDefault(default: T): T {
    return getOrNull() ?: default
}

suspend inline fun <T : Any> ResponseWrapper<T>.getOrElse(
    crossinline onFailure: suspend (ResponseWrapper<T>) -> T
): T {
    return getOrNull() ?: onFailure(this)
}

suspend inline fun <T : Any, R : Any> ResponseWrapper<T>.map(
    crossinline transform: (T) -> R
): Result<R> {
    return toResult().map(transform)
}

suspend inline fun <T : Any> ResponseWrapper<T>.onSuccess(
    crossinline action: suspend (T) -> Unit
): ResponseWrapper<T> {
    if (status.isSuccess()) {
        try {
            action(body())
        } catch (_: Exception) { }
    }
    return this
}

suspend inline fun <T : Any> ResponseWrapper<T>.onFailure(
    crossinline action: suspend (Int, String?) -> Unit
): ResponseWrapper<T> {
    if (!status.isSuccess()) {
        val errorBody = try { errorBody() } catch (_: Exception) { null }
        action(status.value, errorBody)
    }
    return this
}
