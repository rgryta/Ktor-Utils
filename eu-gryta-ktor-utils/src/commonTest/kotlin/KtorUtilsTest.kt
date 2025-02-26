import eu.gryta.ktor.utils.ApiInstance
import io.ktor.client.statement.request
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
import io.ktor.util.reflect.instanceOf
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

object TestSync {
    val mutex = Mutex()
}

class EndpointTest {

    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun `Lock for test synchronization`() = runTest(testDispatcher) {
        TestSync.mutex.lock() // Lock at the start of each test class
    }

    @Test
    fun `API is successfully called`() = runTest(testDispatcher) {
        val endpoint = ApiClient.Todos.TodoId(todoId = 1)
        assertTrue { endpoint.get().status.isSuccess() }
    }

    @Test
    fun `API is called without header`() = runTest(testDispatcher) {
        val endpoint = ApiClient.Todos.TodoId(todoId = 1)
        assertFalse { endpoint.get().response.request.headers.contains(HttpHeaders.Authorization) }
    }

    @Test
    fun `API is called with header`() = runTest(testDispatcher) {
        val endpoint = ApiClient.Todos.TodoId(todoId = 1)
        assertTrue {
            endpoint.get {
                append(HttpHeaders.Authorization, "")
            }.response.request.headers.contains(HttpHeaders.Authorization)
        }
    }

    @Test
    fun `API response returns valid body`() = runTest(testDispatcher) {
        val endpoint = ApiClient.Todos.TodoId(todoId = 1)
        val response = endpoint.get()
        assertTrue { response.body().instanceOf(Todo::class) }
    }

    @AfterTest
    fun `Cleanup after tests`() = runTest(testDispatcher) {
        TestSync.mutex.unlock()
    }
}

class ApiInstanceTest {

    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun `Lock for test synchronization`() = runTest(testDispatcher) {
        TestSync.mutex.lock()
        ApiInstance.getInstance().token = ""
    }

    @Test
    fun `API is successfully called with header from ApiInstance`() = runTest(testDispatcher) {
        val endpoint = ApiClient.Todos.TodoId(todoId = 1)
        assertTrue { endpoint.get().response.request.headers.contains(HttpHeaders.Authorization) }
    }

    @AfterTest
    fun `Cleanup after tests`() = runTest(testDispatcher) {
        ApiInstance.getInstance().token = null
        TestSync.mutex.unlock()
    }
}

class TwoApiInstancesBaseTokenTest {

    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun `Lock for test synchronization`() = runTest(testDispatcher) {
        TestSync.mutex.lock()
        ApiInstanceSecondary()
        ApiInstance.getInstance().token = ""
    }

    @Test
    fun `API is successfully called with header from ApiInstance`() = runTest(testDispatcher) {
        val endpoint = ApiClient.Todos.TodoId(todoId = 1)
        val endpointSecondary = ApiClientSecondary.Todos.TodoId(todoId = 1)
        assertTrue { endpoint.get().response.request.headers.contains(HttpHeaders.Authorization) }
        assertFalse { endpointSecondary.get().response.request.headers.contains(HttpHeaders.Authorization) }
    }

    @AfterTest
    fun `Cleanup after tests`() = runTest(testDispatcher) {
        ApiInstance.getInstance().token = null
        TestSync.mutex.unlock()
    }
}

class TwoApiInstancesSecondaryTokenTest {

    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun `Lock for test synchronization`() = runTest(testDispatcher) {
        TestSync.mutex.lock()
        ApiInstanceSecondary()
        ApiInstance.getInstance(ApiInstanceSecondary::class).token = ""
    }

    @Test
    fun `API is successfully called with header from ApiInstance`() = runTest(testDispatcher) {
        val endpoint = ApiClient.Todos.TodoId(todoId = 1)
        val endpointSecondary = ApiClientSecondary.Todos.TodoId(todoId = 1)
        assertFalse { endpoint.get().response.request.headers.contains(HttpHeaders.Authorization) }
        assertTrue { endpointSecondary.get().response.request.headers.contains(HttpHeaders.Authorization) }
    }

    @AfterTest
    fun `Cleanup after tests`() = runTest(testDispatcher) {
        ApiInstance.getInstance(ApiInstanceSecondary::class).token = null
        TestSync.mutex.unlock()
    }
}