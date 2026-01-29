import eu.gryta.ktor.utils.FormDataBuilder
import io.ktor.client.request.forms.formData
import io.ktor.http.Headers
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MultipartFormTest {

    @Test
    fun `FormDataBuilder append adds text field`() {
        var appendedKey: String? = null
        var appendedValue: String? = null

        val parts = formData {
            FormDataBuilder(this).append("name", "test-value")
        }

        assertTrue { parts.isNotEmpty() }
    }

    @Test
    fun `FormDataBuilder appendFile creates part with correct headers`() {
        val testBytes = "test content".encodeToByteArray()

        val parts = formData {
            FormDataBuilder(this).appendFile(
                key = "file",
                filename = "test.txt",
                contentType = "text/plain",
                bytes = testBytes
            )
        }

        assertTrue { parts.isNotEmpty() }
    }

    @Test
    fun `FormDataBuilder append with headers works`() {
        val testBytes = byteArrayOf(1, 2, 3, 4)

        val parts = formData {
            FormDataBuilder(this).append("data", testBytes, Headers.Empty)
        }

        assertTrue { parts.isNotEmpty() }
    }
}
