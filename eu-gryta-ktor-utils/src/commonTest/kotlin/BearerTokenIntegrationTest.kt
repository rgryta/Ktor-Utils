import eu.gryta.ktor.utils.ApiInstance
import eu.gryta.ktor.utils.clearToken
import eu.gryta.ktor.utils.setBearerToken
import io.ktor.client.statement.request
import io.ktor.http.HttpHeaders
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class BearerTokenIntegrationTest {

    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun `Lock for test synchronization`() = runTest(testDispatcher) {
        TestSync.mutex.lock()
    }

    @Test
    fun `setBearerToken sets token with Bearer prefix`() = runTest(testDispatcher) {
        val instance = ApiInstance.getInstance()
        instance.setBearerToken("test-token")
        assertEquals("Bearer test-token", instance.token.toString())
    }

    @Test
    fun `clearToken removes token`() = runTest(testDispatcher) {
        val instance = ApiInstance.getInstance()
        instance.setBearerToken("test-token")
        instance.clearToken()
        assertNull(instance.token)
    }

    @Test
    fun `setBearerToken is used in Authorization header`() = runTest(testDispatcher) {
        val instance = ApiInstance.getInstance()
        instance.setBearerToken("test-token")

        val endpoint = ApiClient.Todos.TodoId(todoId = 1)
        val response = endpoint.get()

        val authHeader = response.response.request.headers[HttpHeaders.Authorization]
        assertEquals("Bearer test-token", authHeader)
    }

    @AfterTest
    fun `Cleanup after tests`() = runTest(testDispatcher) {
        ApiInstance.getInstance().clearToken()
        TestSync.mutex.unlock()
    }
}
