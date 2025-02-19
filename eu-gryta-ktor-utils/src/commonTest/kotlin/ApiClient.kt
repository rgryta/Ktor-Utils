import eu.gryta.ktor.utils.Endpoint
import eu.gryta.ktor.utils.ResponseWrapper
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.HeadersBuilder
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

fun HttpClientConfig<*>.addLogging() {
    install(Logging) {
        level = LogLevel.ALL
    }
}

object ApiClient {
    private const val URL = "https://jsonplaceholder.typicode.com"
    val client = HttpClient {
        addLogging()
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = true
                isLenient = true
            })
        }
    }

    object Todos {
        private const val URL = "${ApiClient.URL}/todos"

        class TodoId(private val todoId: Int) {
            private val URL: String
                get() = "${Todos.URL}/$todoId"
            private val endpoint = Endpoint(client = client, url = URL)

            suspend fun get(headers: HeadersBuilder.() -> Unit = { }): ResponseWrapper<Todo> {
                return endpoint.get { headers() }
            }
        }
    }
}