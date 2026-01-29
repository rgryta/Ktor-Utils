import eu.gryta.ktor.utils.ResponseWrapper
import io.ktor.http.isSuccess
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ResponseWrapperExtendedTest {

    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun `Lock for test synchronization`() = runTest(testDispatcher) {
        TestSync.mutex.lock()
    }

    @Test
    fun `bodyOrNull returns body on success`() = runTest(testDispatcher) {
        val endpoint = ApiClient.Todos.TodoId(todoId = 1)
        val response = endpoint.get()
        assertTrue { response.status.isSuccess() }
        assertNotNull(response.bodyOrNull())
    }

    @Test
    fun `isSuccess returns true for 2xx`() = runTest(testDispatcher) {
        val endpoint = ApiClient.Todos.TodoId(todoId = 1)
        val response = endpoint.get()
        assertTrue { response.isSuccess() }
    }

    @Test
    fun `isClientError returns false for 2xx`() = runTest(testDispatcher) {
        val endpoint = ApiClient.Todos.TodoId(todoId = 1)
        val response = endpoint.get()
        assertFalse { response.isClientError() }
    }

    @Test
    fun `isServerError returns false for 2xx`() = runTest(testDispatcher) {
        val endpoint = ApiClient.Todos.TodoId(todoId = 1)
        val response = endpoint.get()
        assertFalse { response.isServerError() }
    }

    @Test
    fun `errorBody returns null on success`() = runTest(testDispatcher) {
        val endpoint = ApiClient.Todos.TodoId(todoId = 1)
        val response = endpoint.get()
        assertNull(response.errorBody())
    }

    @Test
    fun `bodyOrNull returns null on 404`() = runTest(testDispatcher) {
        val endpoint = ApiClient.Todos.TodoId(todoId = 999999)
        val response = endpoint.get()
        assertFalse { response.isSuccess() }
        assertNull(response.bodyOrNull())
    }

    @Test
    fun `isClientError returns true for 404`() = runTest(testDispatcher) {
        val endpoint = ApiClient.Todos.TodoId(todoId = 999999)
        val response = endpoint.get()
        assertTrue { response.isClientError() }
        assertFalse { response.isServerError() }
    }

    @Test
    fun `errorBody returns body on 404`() = runTest(testDispatcher) {
        val endpoint = ApiClient.Todos.TodoId(todoId = 999999)
        val response = endpoint.get()
        assertNotNull(response.errorBody())
    }

    @AfterTest
    fun `Cleanup after tests`() = runTest(testDispatcher) {
        TestSync.mutex.unlock()
    }
}
