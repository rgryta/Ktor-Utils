import eu.gryta.ktor.utils.Endpoint
import eu.gryta.ktor.utils.ResponseWrapper
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Serializable
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@Serializable
data class Post(
    val userId: Int,
    val id: Int? = null,
    val title: String,
    val body: String
)

/**
 * Tests for all HTTP methods supported by Endpoint.
 * Uses JSONPlaceholder API for integration testing.
 */
class HttpMethodsTest {

    private val testDispatcher = StandardTestDispatcher()
    private val baseUrl = "https://jsonplaceholder.typicode.com"

    @BeforeTest
    fun `Lock for test synchronization`() = runTest(testDispatcher) {
        TestSync.mutex.lock()
    }

    @Test
    fun `POST creates new resource`() = runTest(testDispatcher) {
        val endpoint = Endpoint(client = ApiClient.client, url = "$baseUrl/posts")
        val newPost = Post(userId = 1, title = "Test Title", body = "Test Body")

        val response: ResponseWrapper<Post> = endpoint.post {
            contentType(ContentType.Application.Json)
            setBody(newPost)
        }

        assertTrue(response.status.isSuccess(), "POST should return success status")
        val created = response.body()
        assertEquals("Test Title", created.title)
        assertEquals("Test Body", created.body)
    }

    @Test
    fun `PUT updates existing resource`() = runTest(testDispatcher) {
        val endpoint = Endpoint(client = ApiClient.client, url = "$baseUrl/posts/1")
        val updatedPost = Post(userId = 1, id = 1, title = "Updated Title", body = "Updated Body")

        val response: ResponseWrapper<Post> = endpoint.put {
            contentType(ContentType.Application.Json)
            setBody(updatedPost)
        }

        assertTrue(response.status.isSuccess(), "PUT should return success status")
        val result = response.body()
        assertEquals("Updated Title", result.title)
    }

    @Test
    fun `PATCH partially updates resource`() = runTest(testDispatcher) {
        val endpoint = Endpoint(client = ApiClient.client, url = "$baseUrl/posts/1")

        val response: ResponseWrapper<Post> = endpoint.patch {
            contentType(ContentType.Application.Json)
            setBody(mapOf("title" to "Patched Title"))
        }

        assertTrue(response.status.isSuccess(), "PATCH should return success status")
    }

    @Test
    fun `DELETE removes resource`() = runTest(testDispatcher) {
        val endpoint = Endpoint(client = ApiClient.client, url = "$baseUrl/posts/1")

        // JSONPlaceholder returns empty object {} for DELETE, so we use Any
        val response: ResponseWrapper<Map<String, String>> = endpoint.delete()

        assertTrue(response.status.isSuccess(), "DELETE should return success status")
    }

    @Test
    fun `HEAD returns headers only`() = runTest(testDispatcher) {
        val endpoint = Endpoint(client = ApiClient.client, url = "$baseUrl/posts/1")

        // HEAD requests don't return a body, using String for empty response
        val response: ResponseWrapper<String> = endpoint.head()

        assertTrue(response.status.isSuccess(), "HEAD should return success status")
    }

    @Test
    fun `OPTIONS returns allowed methods`() = runTest(testDispatcher) {
        val endpoint = Endpoint(client = ApiClient.client, url = "$baseUrl/posts")

        val response: ResponseWrapper<String> = endpoint.options()

        // OPTIONS may return 200 or 204 depending on server
        assertTrue(response.status.value in 200..299, "OPTIONS should return success status")
    }

    @AfterTest
    fun `Cleanup after tests`() = runTest(testDispatcher) {
        TestSync.mutex.unlock()
    }
}
