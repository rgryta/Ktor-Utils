import eu.gryta.ktor.utils.ApiInstance
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotSame
import kotlin.test.assertSame

/**
 * Extended tests for ApiInstance singleton management.
 */
class ApiInstanceExtendedTest {

    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun `Lock for test synchronization`() = runTest(testDispatcher) {
        TestSync.mutex.lock()
    }

    @Test
    fun `getInstance returns same instance on multiple calls`() = runTest(testDispatcher) {
        val instance1 = ApiInstance.getInstance()
        val instance2 = ApiInstance.getInstance()

        assertSame(instance1, instance2, "getInstance should return the same singleton instance")
    }

    @Test
    fun `different ApiInstance classes have different instances`() = runTest(testDispatcher) {
        ApiInstanceSecondary() // Register the secondary instance

        val defaultInstance = ApiInstance.getInstance()
        val secondaryInstance = ApiInstance.getInstance(ApiInstanceSecondary::class)

        assertNotSame(defaultInstance, secondaryInstance, "Different classes should have different instances")
    }

    @Test
    fun `token is isolated per instance class`() = runTest(testDispatcher) {
        ApiInstanceSecondary() // Register the secondary instance

        val defaultInstance = ApiInstance.getInstance()
        val secondaryInstance = ApiInstance.getInstance(ApiInstanceSecondary::class)

        defaultInstance.token = "default-token"
        secondaryInstance.token = "secondary-token"

        assertEquals("default-token", defaultInstance.token, "Default instance should have its own token")
        assertEquals("secondary-token", secondaryInstance.token, "Secondary instance should have its own token")
    }

    @Test
    fun `token can be set to null to clear it`() = runTest(testDispatcher) {
        val instance = ApiInstance.getInstance()
        instance.token = "some-token"

        assertEquals("some-token", instance.token)

        instance.token = null

        assertEquals(null, instance.token, "Token should be clearable by setting to null")
    }

    @Test
    fun `token toString is used for header value`() = runTest(testDispatcher) {
        val instance = ApiInstance.getInstance()

        // Test with custom object that has toString
        val customToken = object {
            override fun toString() = "Bearer custom-token-value"
        }
        instance.token = customToken

        assertEquals("Bearer custom-token-value", instance.token.toString())
    }

    @AfterTest
    fun `Cleanup after tests`() = runTest(testDispatcher) {
        ApiInstance.getInstance().token = null
        // Also clean up secondary instance if it was registered
        try {
            ApiInstance.getInstance(ApiInstanceSecondary::class).token = null
        } catch (_: IllegalArgumentException) {
            // Secondary instance not registered, ignore
        }
        TestSync.mutex.unlock()
    }
}
