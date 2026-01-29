import eu.gryta.ktor.utils.CommonInterceptors
import eu.gryta.ktor.utils.InterceptorRegistry
import eu.gryta.ktor.utils.RequestInterceptor
import io.ktor.client.statement.request
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class InterceptorTest {

    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun `Lock for test synchronization`() = runTest(testDispatcher) {
        TestSync.mutex.lock()
        InterceptorRegistry.clear()
    }

    @Test
    fun `interceptor adds custom header`() = runTest(testDispatcher) {
        val interceptor = CommonInterceptors.customHeader("X-Custom-Header") { "custom-value" }
        InterceptorRegistry.register(interceptor)

        val endpoint = ApiClient.Todos.TodoId(todoId = 1)
        val response = endpoint.get()

        val header = response.response.request.headers["X-Custom-Header"]
        assertEquals("custom-value", header)
    }

    @Test
    fun `multiple interceptors applied in order`() = runTest(testDispatcher) {
        InterceptorRegistry.register(CommonInterceptors.customHeader("X-First") { "first" })
        InterceptorRegistry.register(CommonInterceptors.customHeader("X-Second") { "second" })

        val endpoint = ApiClient.Todos.TodoId(todoId = 1)
        val response = endpoint.get()

        assertEquals("first", response.response.request.headers["X-First"])
        assertEquals("second", response.response.request.headers["X-Second"])
    }

    @Test
    fun `interceptor can be unregistered`() = runTest(testDispatcher) {
        val interceptor = CommonInterceptors.customHeader("X-Remove-Me") { "value" }
        InterceptorRegistry.register(interceptor)
        InterceptorRegistry.unregister(interceptor)

        val endpoint = ApiClient.Todos.TodoId(todoId = 1)
        val response = endpoint.get()

        assertFalse { response.response.request.headers.contains("X-Remove-Me") }
    }

    @Test
    fun `clear removes all interceptors`() = runTest(testDispatcher) {
        InterceptorRegistry.register(CommonInterceptors.customHeader("X-First") { "first" })
        InterceptorRegistry.register(CommonInterceptors.customHeader("X-Second") { "second" })
        InterceptorRegistry.clear()

        val endpoint = ApiClient.Todos.TodoId(todoId = 1)
        val response = endpoint.get()

        assertFalse { response.response.request.headers.contains("X-First") }
        assertFalse { response.response.request.headers.contains("X-Second") }
    }

    @Test
    fun `deviceId interceptor adds X-Device-ID header`() = runTest(testDispatcher) {
        val interceptor = CommonInterceptors.deviceId { "device-123" }
        InterceptorRegistry.register(interceptor)

        val endpoint = ApiClient.Todos.TodoId(todoId = 1)
        val response = endpoint.get()

        assertEquals("device-123", response.response.request.headers["X-Device-ID"])
    }

    @Test
    fun `deviceId interceptor skips null`() = runTest(testDispatcher) {
        val interceptor = CommonInterceptors.deviceId { null }
        InterceptorRegistry.register(interceptor)

        val endpoint = ApiClient.Todos.TodoId(todoId = 1)
        val response = endpoint.get()

        assertFalse { response.response.request.headers.contains("X-Device-ID") }
    }

    @Test
    fun `requestId interceptor adds X-Request-ID header`() = runTest(testDispatcher) {
        val interceptor = CommonInterceptors.requestId { "req-456" }
        InterceptorRegistry.register(interceptor)

        val endpoint = ApiClient.Todos.TodoId(todoId = 1)
        val response = endpoint.get()

        assertEquals("req-456", response.response.request.headers["X-Request-ID"])
    }

    @AfterTest
    fun `Cleanup after tests`() = runTest(testDispatcher) {
        InterceptorRegistry.clear()
        TestSync.mutex.unlock()
    }
}
