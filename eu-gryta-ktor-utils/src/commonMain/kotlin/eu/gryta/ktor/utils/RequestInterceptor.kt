package eu.gryta.ktor.utils

import io.ktor.client.request.HttpRequestBuilder

fun interface RequestInterceptor {
    fun intercept(builder: HttpRequestBuilder)
}

object InterceptorRegistry {
    private val interceptors = mutableListOf<RequestInterceptor>()

    fun register(interceptor: RequestInterceptor) {
        interceptors.add(interceptor)
    }

    fun unregister(interceptor: RequestInterceptor) {
        interceptors.remove(interceptor)
    }

    fun clear() {
        interceptors.clear()
    }

    fun applyAll(builder: HttpRequestBuilder) {
        interceptors.forEach { it.intercept(builder) }
    }
}

object CommonInterceptors {
    fun customHeader(name: String, value: () -> String): RequestInterceptor {
        return RequestInterceptor { builder ->
            builder.headers.append(name, value())
        }
    }

    fun deviceId(deviceIdProvider: () -> String?): RequestInterceptor {
        return RequestInterceptor { builder ->
            deviceIdProvider()?.let {
                builder.headers.append("X-Device-ID", it)
            }
        }
    }

    fun requestId(idGenerator: () -> String): RequestInterceptor {
        return RequestInterceptor { builder ->
            builder.headers.append("X-Request-ID", idGenerator())
        }
    }
}
