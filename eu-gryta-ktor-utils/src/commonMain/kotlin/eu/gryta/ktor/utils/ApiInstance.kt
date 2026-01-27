package eu.gryta.ktor.utils

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.reflect.KClass

/**
 * A thread-safe singleton manager for API authentication tokens.
 *
 * ApiInstance provides a centralized mechanism for managing authentication tokens
 * across your application. It supports multiple instances for different APIs,
 * each with its own independent token state.
 *
 * ## Basic Usage
 * ```kotlin
 * // Set token on default instance
 * ApiInstance.getInstance().token = "Bearer your-jwt-token"
 *
 * // Token will be automatically used by Endpoint requests
 * ```
 *
 * ## Multiple API Instances
 * ```kotlin
 * class SecondaryApiInstance : ApiInstance() {
 *     companion object {
 *         init {
 *             register(SecondaryApiInstance::class) { SecondaryApiInstance() }
 *         }
 *     }
 * }
 *
 * // Use different tokens for different APIs
 * ApiInstance.getInstance().token = "Token for primary API"
 * ApiInstance.getInstance(SecondaryApiInstance::class).token = "Token for secondary API"
 * ```
 *
 * @see Endpoint for using ApiInstance with HTTP requests
 */
open class ApiInstance {
    /**
     * The authentication token for this API instance.
     *
     * When set, this token will be automatically added as the Authorization header
     * in requests made through [Endpoint] that are associated with this instance.
     *
     * The token's [toString] method is called to generate the header value,
     * so you can use String directly or a custom token class.
     *
     * Set to `null` to disable automatic token injection.
     */
    var token: Any? = null

    companion object {
        private val lock = Mutex()
        private val _instances = mutableMapOf<KClass<*>, ApiInstance>()
        private val _factories = mutableMapOf<KClass<*>, () -> ApiInstance>()

        /**
         * Retrieves the default [ApiInstance].
         *
         * This is a convenience method equivalent to `getInstance(ApiInstance::class)`.
         *
         * @return The default ApiInstance singleton.
         */
        suspend fun getInstance(): ApiInstance {
            return getInstance(ApiInstance::class)
        }

        /**
         * Retrieves the [ApiInstance] for the specified class.
         *
         * If no instance exists for the class, one will be created using the
         * registered factory function. The instance is cached for subsequent calls.
         *
         * @param T The ApiInstance subclass type.
         * @param kClass The KClass of the desired ApiInstance type.
         * @return The ApiInstance singleton of the specified type.
         * @throws IllegalArgumentException if no factory is registered for the class.
         * @throws IllegalStateException if the created instance is not of the expected type.
         */
        suspend fun <T : ApiInstance> getInstance(kClass: KClass<T>): T {
            return lock.withLock {
                if (kClass == ApiInstance::class) {
                    _factories.getOrPut(kClass) { { ApiInstance() } }
                }

                val instance = _instances[kClass]
                    ?: _factories[kClass]?.invoke()?.also { _instances[kClass] = it }
                    ?: throw IllegalArgumentException("No factory registered for $kClass")

                if (kClass.isInstance(instance)) {
                    @Suppress("UNCHECKED_CAST")
                    instance as T
                } else {
                    throw IllegalStateException("Instance of ${instance::class} is not of type $kClass")
                }
            }
        }

        /**
         * Registers a factory function for creating instances of a custom ApiInstance subclass.
         *
         * Call this method in the companion object initializer of your ApiInstance subclass
         * to enable automatic instance creation.
         *
         * @param T The ApiInstance subclass type.
         * @param kClass The KClass to register the factory for.
         * @param factory A function that creates new instances of the class.
         */
        fun <T : ApiInstance> register(kClass: KClass<T>, factory: () -> T) {
            _factories[kClass] = factory
        }

        init {
            register(ApiInstance::class) { ApiInstance() }
        }
    }
}
