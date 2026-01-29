import eu.gryta.ktor.utils.ApiError
import eu.gryta.ktor.utils.getOrDefault
import eu.gryta.ktor.utils.getOrElse
import eu.gryta.ktor.utils.getOrNull
import eu.gryta.ktor.utils.map
import eu.gryta.ktor.utils.onFailure
import eu.gryta.ktor.utils.onSuccess
import eu.gryta.ktor.utils.toResult
import eu.gryta.ktor.utils.unwrapOrThrow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ResultExtensionsTest {

    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun `Lock for test synchronization`() = runTest(testDispatcher) {
        TestSync.mutex.lock()
    }

    @Test
    fun `unwrapOrThrow returns body on success`() = runTest(testDispatcher) {
        val endpoint = ApiClient.Todos.TodoId(todoId = 1)
        val response = endpoint.get()
        val todo = response.unwrapOrThrow()
        assertNotNull(todo)
        assertEquals(1, todo.id)
    }

    @Test
    fun `unwrapOrThrow throws NotFound on 404`() = runTest(testDispatcher) {
        val endpoint = ApiClient.Todos.TodoId(todoId = 999999)
        val response = endpoint.get()
        assertFailsWith<ApiError.NotFound> {
            response.unwrapOrThrow()
        }
    }

    @Test
    fun `toResult returns success for 2xx`() = runTest(testDispatcher) {
        val endpoint = ApiClient.Todos.TodoId(todoId = 1)
        val response = endpoint.get()
        val result = response.toResult()
        assertTrue { result.isSuccess }
        assertNotNull(result.getOrNull())
    }

    @Test
    fun `toResult returns failure for 404`() = runTest(testDispatcher) {
        val endpoint = ApiClient.Todos.TodoId(todoId = 999999)
        val response = endpoint.get()
        val result = response.toResult()
        assertTrue { result.isFailure }
        assertIs<ApiError.NotFound>(result.exceptionOrNull())
    }

    @Test
    fun `getOrNull returns body on success`() = runTest(testDispatcher) {
        val endpoint = ApiClient.Todos.TodoId(todoId = 1)
        val response = endpoint.get()
        val todo = response.getOrNull()
        assertNotNull(todo)
    }

    @Test
    fun `getOrNull returns null on failure`() = runTest(testDispatcher) {
        val endpoint = ApiClient.Todos.TodoId(todoId = 999999)
        val response = endpoint.get()
        val todo = response.getOrNull()
        assertNull(todo)
    }

    @Test
    fun `getOrDefault returns body on success`() = runTest(testDispatcher) {
        val endpoint = ApiClient.Todos.TodoId(todoId = 1)
        val response = endpoint.get()
        val default = Todo(id = 0, userId = 0, title = "default", completed = false)
        val todo = response.getOrDefault(default)
        assertEquals(1, todo.id)
    }

    @Test
    fun `getOrDefault returns default on failure`() = runTest(testDispatcher) {
        val endpoint = ApiClient.Todos.TodoId(todoId = 999999)
        val response = endpoint.get()
        val default = Todo(id = 0, userId = 0, title = "default", completed = false)
        val todo = response.getOrDefault(default)
        assertEquals(0, todo.id)
        assertEquals("default", todo.title)
    }

    @Test
    fun `getOrElse returns body on success`() = runTest(testDispatcher) {
        val endpoint = ApiClient.Todos.TodoId(todoId = 1)
        val response = endpoint.get()
        val todo = response.getOrElse { Todo(id = 0, userId = 0, title = "fallback", completed = false) }
        assertEquals(1, todo.id)
    }

    @Test
    fun `getOrElse computes fallback on failure`() = runTest(testDispatcher) {
        val endpoint = ApiClient.Todos.TodoId(todoId = 999999)
        val response = endpoint.get()
        val todo = response.getOrElse { Todo(id = 0, userId = 0, title = "fallback", completed = false) }
        assertEquals("fallback", todo.title)
    }

    @Test
    fun `map transforms successful response`() = runTest(testDispatcher) {
        val endpoint = ApiClient.Todos.TodoId(todoId = 1)
        val response = endpoint.get()
        val result = response.map { it.title }
        assertTrue { result.isSuccess }
        assertNotNull(result.getOrNull())
    }

    @Test
    fun `map returns failure on error response`() = runTest(testDispatcher) {
        val endpoint = ApiClient.Todos.TodoId(todoId = 999999)
        val response = endpoint.get()
        val result = response.map { it.title }
        assertTrue { result.isFailure }
    }

    @Test
    fun `onSuccess executes on 2xx`() = runTest(testDispatcher) {
        val endpoint = ApiClient.Todos.TodoId(todoId = 1)
        val response = endpoint.get()
        var executed = false
        response.onSuccess { executed = true }
        assertTrue { executed }
    }

    @Test
    fun `onSuccess does not execute on failure`() = runTest(testDispatcher) {
        val endpoint = ApiClient.Todos.TodoId(todoId = 999999)
        val response = endpoint.get()
        var executed = false
        response.onSuccess { executed = true }
        assertTrue { !executed }
    }

    @Test
    fun `onFailure executes on error`() = runTest(testDispatcher) {
        val endpoint = ApiClient.Todos.TodoId(todoId = 999999)
        val response = endpoint.get()
        var statusCode: Int? = null
        response.onFailure { code, _ -> statusCode = code }
        assertEquals(404, statusCode)
    }

    @Test
    fun `onFailure does not execute on success`() = runTest(testDispatcher) {
        val endpoint = ApiClient.Todos.TodoId(todoId = 1)
        val response = endpoint.get()
        var executed = false
        response.onFailure { _, _ -> executed = true }
        assertTrue { !executed }
    }

    @AfterTest
    fun `Cleanup after tests`() = runTest(testDispatcher) {
        TestSync.mutex.unlock()
    }
}
