import eu.gryta.ktor.utils.BearerToken
import kotlin.test.Test
import kotlin.test.assertEquals

class BearerTokenTest {

    @Test
    fun `BearerToken formats with Bearer prefix`() {
        val token = BearerToken("my-jwt-token")
        assertEquals("Bearer my-jwt-token", token.toString())
    }

    @Test
    fun `BearerToken rawToken returns token without prefix`() {
        val token = BearerToken("my-jwt-token")
        assertEquals("my-jwt-token", token.rawToken)
    }

    @Test
    fun `BearerToken fromRaw creates token correctly`() {
        val token = BearerToken.fromRaw("my-jwt-token")
        assertEquals("Bearer my-jwt-token", token.toString())
        assertEquals("my-jwt-token", token.rawToken)
    }
}
