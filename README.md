# Ktor-Utils

[![Version](https://img.shields.io/badge/version-1.3.0-blue)](https://github.com/rgryta/Ktor-Utils/releases)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.0-purple.svg)](https://kotlinlang.org)
[![Ktor](https://img.shields.io/badge/Ktor-3.2.2-blue.svg)](https://ktor.io/)
[![License: Apache 2.0](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0)

A **Kotlin Multiplatform** library providing utility classes for building API clients with Ktor. Simplifies HTTP request handling with automatic token management, response wrapping, and type-safe endpoints.

## Features

- **Endpoint** - Type-safe HTTP endpoint wrapper with automatic authorization headers
- **ApiInstance** - Thread-safe singleton for managing API authentication tokens
- **ResponseWrapper** - Type-safe response handling with status code checking and error body access
- **ApiError** - Sealed class hierarchy for typed error handling
- **Result Extensions** - Functional extensions for ResponseWrapper (toResult, getOrNull, map, etc.)
- **BearerToken** - Helper class for token management with automatic Bearer prefix
- **Pagination** - Skip/limit and page-based pagination helpers
- **Request Interceptors** - Global request interceptor system for headers, logging, etc.
- **Multipart Forms** - DSL for file uploads and multipart form data
- **Retry Mechanism** - Configurable retry with exponential backoff
- **All HTTP Methods** - Support for GET, POST, PUT, DELETE, PATCH, HEAD, OPTIONS, submitForm
- **Multiple API Instances** - Support for independent token management across different APIs
- **Coroutine-based** - Fully suspending functions for async operations

## Platform Support

| Platform | Status |
|----------|--------|
| Android (API 26+) | Supported |
| iOS (arm64, x64, simulator) | Supported |
| JVM/Desktop | Supported |
| JavaScript | Supported |
| WebAssembly (WASM) | Supported |

## Installation

### Gradle (Kotlin DSL)

Add the GitHub Packages repository to your `settings.gradle.kts`:

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("https://maven.pkg.github.com/rgryta/Ktor-Utils")
            credentials {
                username = project.findProperty("gpr.user") as String? ?: System.getenv("GPR_USERNAME")
                password = project.findProperty("gpr.key") as String? ?: System.getenv("GPR_TOKEN")
            }
        }
    }
}
```

Add the dependency to your module's `build.gradle.kts`:

```kotlin
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("eu.gryta:ktor.utils:1.3.0")
        }
    }
}
```

### GitHub Authentication

To access GitHub Packages, you need a Personal Access Token (PAT) with `read:packages` permission.

1. Create a PAT at: https://github.com/settings/tokens
2. Add to your `gradle.properties`:

```properties
gpr.user=your-github-username
gpr.key=your-github-token
```

Or set environment variables:

```bash
export GPR_USERNAME=your-github-username
export GPR_TOKEN=your-github-token
```

## Usage

### Basic Setup

```kotlin
import eu.gryta.ktor.utils.Endpoint
import eu.gryta.ktor.utils.ApiInstance
import eu.gryta.ktor.utils.ResponseWrapper
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json

// Create your HTTP client
val client = HttpClient {
    install(ContentNegotiation) {
        json()
    }
}
```

### Creating API Endpoints

```kotlin
import kotlinx.serialization.Serializable

@Serializable
data class User(val id: Int, val name: String, val email: String)

object MyApi {
    private const val BASE_URL = "https://api.example.com"

    object Users {
        private const val URL = "$BASE_URL/users"

        class ById(private val userId: Int) {
            private val endpoint = Endpoint(client = client, url = "$URL/$userId")

            suspend fun get(): ResponseWrapper<User> = endpoint.get()
            suspend fun update(user: User): ResponseWrapper<User> = endpoint.put {
                setBody(user)
            }
            suspend fun delete(): ResponseWrapper<Unit> = endpoint.delete()
        }

        object All {
            private val endpoint = Endpoint(client = client, url = URL)

            suspend fun list(): ResponseWrapper<List<User>> = endpoint.get()
            suspend fun create(user: User): ResponseWrapper<User> = endpoint.post {
                setBody(user)
            }
        }
    }
}
```

### Using ResponseWrapper

```kotlin
suspend fun fetchUser(id: Int) {
    val response = MyApi.Users.ById(id).get()

    if (response.status.isSuccess()) {
        val user: User = response.body()
        println("User: ${user.name}")
    } else {
        println("Error: ${response.status}")
    }
}
```

### Setting Authorization Tokens

```kotlin
// Set token on the default ApiInstance
ApiInstance.getInstance().token = "Bearer your-jwt-token"

// All subsequent requests will include the Authorization header automatically
val response = MyApi.Users.ById(1).get()
```

### Multiple API Instances

```kotlin
// Create a custom ApiInstance for a different API
class SecondaryApiInstance : ApiInstance() {
    companion object {
        init {
            register(SecondaryApiInstance::class) { SecondaryApiInstance() }
        }
    }
}

// Create endpoint with custom instance
val endpoint = Endpoint(
    client = client,
    url = "https://other-api.com/resource",
    apiInstanceClass = SecondaryApiInstance::class
)

// Set token for secondary instance
ApiInstance.getInstance(SecondaryApiInstance::class).token = "Bearer other-token"
```

### Form Submission

```kotlin
suspend fun login(username: String, password: String) {
    val endpoint = Endpoint(client = client, url = "https://api.example.com/login")

    val response = endpoint.submitForm<LoginResponse>(
        formParameters = {
            append("username", username)
            append("password", password)
        }
    )

    if (response.status.isSuccess()) {
        val loginData = response.body()
        ApiInstance.getInstance().setBearerToken(loginData.accessToken)
    }
}
```

### Error Handling with Result Extensions

```kotlin
suspend fun fetchUserSafely(id: Int): User? {
    return MyApi.Users.ById(id).get()
        .onSuccess { user -> println("Fetched: ${user.name}") }
        .onFailure { status -> println("Failed: $status") }
        .getOrNull()
}

suspend fun fetchUserWithDefault(id: Int): User {
    return MyApi.Users.ById(id).get()
        .getOrDefault(User(id = 0, name = "Guest", email = ""))
}

suspend fun fetchUserOrThrow(id: Int): User {
    return MyApi.Users.ById(id).get().unwrapOrThrow()
}
```

### Pagination

```kotlin
suspend fun fetchAllUsers(): List<User> {
    val allUsers = mutableListOf<User>()
    var params = PaginationParams(skip = 0, limit = 50)

    do {
        val response = MyApi.Users.All.list { paginate(params) }
        val users = response.body()
        allUsers.addAll(users)
        params = params.nextPage
    } while (users.hasMore(params))

    return allUsers
}
```

### Request Interceptors

```kotlin
// Add global interceptors
InterceptorRegistry.register(CommonInterceptors.deviceId("device-123"))
InterceptorRegistry.register(CommonInterceptors.requestId())
InterceptorRegistry.register(CommonInterceptors.customHeader("X-App-Version", "1.0.0"))

// All requests now include these headers automatically
val response = MyApi.Users.ById(1).get()

// Clear when done
InterceptorRegistry.clear()
```

### File Upload

```kotlin
suspend fun uploadAvatar(imageBytes: ByteArray): ResponseWrapper<UploadResult> {
    val endpoint = Endpoint(client = client, url = "https://api.example.com/avatar")

    return endpoint.submitFormWithBinaryData<UploadResult> {
        append("description", "Profile photo")
        appendFile("file", "avatar.png", "image/png", imageBytes)
    }
}
```

### Retry with Exponential Backoff

```kotlin
val config = RetryConfig(
    maxAttempts = 3,
    initialDelayMs = 1000,
    backoffMultiplier = 2.0
)

suspend fun fetchWithRetry(): User {
    return retryRequest(config) {
        MyApi.Users.ById(1).get()
    }.unwrapOrThrow()
}
```

## API Reference

### Endpoint

The main class for making HTTP requests.

| Method | Description |
|--------|-------------|
| `get<T>()` | Perform GET request |
| `post<T>()` | Perform POST request |
| `put<T>()` | Perform PUT request |
| `delete<T>()` | Perform DELETE request |
| `patch<T>()` | Perform PATCH request |
| `head<T>()` | Perform HEAD request |
| `options<T>()` | Perform OPTIONS request |
| `submitForm<T>()` | Submit form data |

### ApiInstance

Thread-safe singleton manager for API authentication.

| Method | Description |
|--------|-------------|
| `getInstance()` | Get default instance |
| `getInstance(KClass)` | Get typed instance |
| `register(KClass, factory)` | Register custom instance factory |

### ResponseWrapper

Type-safe response container.

| Property/Method | Description |
|-----------------|-------------|
| `status` | HTTP status code |
| `response` | Raw HttpResponse |
| `body()` | Deserialize response body (throws if not successful) |
| `bodyOrNull()` | Returns body or null on non-2xx |
| `errorBody()` | Returns raw error body string on failure |
| `errorBodyAs<E>()` | Deserialize error body to type |
| `isSuccess()` | Returns true for 2xx status codes |
| `isClientError()` | Returns true for 4xx status codes |
| `isServerError()` | Returns true for 5xx status codes |

### ApiError

Sealed class hierarchy for typed error handling.

| Type | Description |
|------|-------------|
| `NetworkError` | General network failure |
| `TimeoutError` | Request timeout |
| `ConnectionError` | Connection refused/failed |
| `BadRequest` | HTTP 400 |
| `Unauthorized` | HTTP 401 |
| `Forbidden` | HTTP 403 |
| `NotFound` | HTTP 404 |
| `Conflict` | HTTP 409 |
| `PayloadTooLarge` | HTTP 413 |
| `TooManyRequests` | HTTP 429 |
| `ServerError` | HTTP 5xx |
| `ServiceUnavailable` | HTTP 503 |
| `DeserializationError` | JSON parsing failure |
| `HttpError` | Generic HTTP error |

Use `StatusCodeMapper.fromStatusCode(code)` to map status codes to errors.

### Result Extensions

Functional extensions for ResponseWrapper.

| Method | Description |
|--------|-------------|
| `toResult()` | Convert to `Result<T>` |
| `unwrapOrThrow()` | Body or throw ApiError |
| `getOrNull()` | Body or null on failure |
| `getOrDefault(default)` | Body or default value |
| `getOrElse { }` | Body or compute fallback |
| `map { }` | Transform successful body |
| `onSuccess { }` | Execute on 2xx only |
| `onFailure { }` | Execute on non-2xx only |

### BearerToken

Helper class for token management.

| Property/Method | Description |
|-----------------|-------------|
| `toString()` | Returns "Bearer {token}" |
| `rawToken` | Token without Bearer prefix |
| `BearerToken.fromRaw(token)` | Create from raw token string |
| `ApiInstance.setBearerToken(token)` | Set token with Bearer prefix |
| `ApiInstance.clearToken()` | Remove the current token |

### Pagination

Helpers for paginated API requests.

| Class/Method | Description |
|--------------|-------------|
| `PaginationParams(skip, limit)` | Skip/limit pagination |
| `PageParams(page, limit)` | Page-based pagination |
| `HttpRequestBuilder.paginate(params)` | Add pagination to request |
| `List<T>.hasMore(params)` | Check if more results exist |
| `PaginatedResponse<T>` | Wrapper with pagination metadata |

### Request Interceptors

Global request interceptor system.

| Class/Method | Description |
|--------------|-------------|
| `RequestInterceptor` | Fun interface for interceptors |
| `InterceptorRegistry.register(interceptor)` | Add global interceptor |
| `InterceptorRegistry.unregister(interceptor)` | Remove interceptor |
| `InterceptorRegistry.clear()` | Remove all interceptors |
| `CommonInterceptors.customHeader(name, value)` | Add custom header |
| `CommonInterceptors.deviceId(id)` | Add X-Device-ID header |
| `CommonInterceptors.requestId()` | Add X-Request-ID header |

### Multipart Forms

DSL for file uploads and multipart form data.

| Method | Description |
|--------|-------------|
| `Endpoint.submitFormWithBinaryData()` | Submit multipart form |
| `FormDataBuilder.append(key, value)` | Add text field |
| `FormDataBuilder.appendFile(key, filename, contentType, bytes)` | Add file |

### Retry Mechanism

Configurable retry with exponential backoff.

| Class/Method | Description |
|--------------|-------------|
| `RetryConfig(maxAttempts, initialDelayMs, backoffMultiplier, retryOn)` | Configuration |
| `withRetry(config) { }` | Execute with retry |
| `Throwable.isRetryable()` | Check if error is retryable |
| `retryRequest(config) { }` | Retry ResponseWrapper operations |

## Requirements

- **Kotlin**: 2.2.0+
- **Ktor**: 3.2.2+
- **Android**: minSdk 26, compileSdk 36
- **JVM**: Java 21

## Building from Source

```bash
# Clone the repository
git clone https://github.com/rgryta/Ktor-Utils.git
cd Ktor-Utils

# Build the library
make build

# Run tests
make test

# Publish to Maven Local
make publish
```

## Contributing

Contributions are welcome! Please follow these guidelines:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## Versioning

This project uses [Semantic Versioning](https://semver.org/):

- **MAJOR** version for incompatible API changes
- **MINOR** version for new functionality (backwards compatible)
- **PATCH** version for bug fixes

Current version: **1.3.0**

## License

This project is licensed under the **Apache License 2.0** - see the [LICENSE](LICENSE) file for details.

## Author

**Radoslaw Gryta**

- GitHub: [@rgryta](https://github.com/rgryta)
- Email: radek.gryta@gmail.com

## Links

- [Repository](https://github.com/rgryta/Ktor-Utils)
- [Issues](https://github.com/rgryta/Ktor-Utils/issues)
- [Releases](https://github.com/rgryta/Ktor-Utils/releases)
- [GitHub Packages](https://github.com/rgryta/Ktor-Utils/packages)

## Changelog

### [1.3.0] - 2026-01-29
- Enhanced ResponseWrapper with error body access methods
- Added ApiError sealed class hierarchy for typed error handling
- Added Result extensions (toResult, getOrNull, map, onSuccess, etc.)
- Added BearerToken helper class with setBearerToken/clearToken extensions
- Added pagination helpers (PaginationParams, PageParams)
- Added request interceptor system with InterceptorRegistry
- Added multipart form support with FormDataBuilder DSL
- Added retry mechanism with exponential backoff

### [1.1.1] - 2025-07-25
- Allow `HttpRequestBuilder.()` DSL (more flexible) instead of forcing `HeaderBuilder.()`

### [1.1.0] - 2025-02-26
- Support for multiple custom `ApiInstances`

### [1.0.1] - 2025-02-25
- Allow custom `ApiInstance` companion implementations

### [1.0.0] - 2025-02-19
- Initial stable release with Endpoint, ApiInstance, and ResponseWrapper
