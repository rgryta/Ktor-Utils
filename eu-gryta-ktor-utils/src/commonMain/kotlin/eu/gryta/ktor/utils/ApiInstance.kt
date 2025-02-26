package eu.gryta.ktor.utils

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.reflect.KClass

open class ApiInstance {
    var token: Any? = null

    companion object {
        private val lock = Mutex()
        private val _instances = mutableMapOf<KClass<*>, ApiInstance>()
        private val _factories = mutableMapOf<KClass<*>, () -> ApiInstance>()

        suspend fun getInstance(): ApiInstance {
            return getInstance(ApiInstance::class)
        }

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

        fun <T : ApiInstance> register(kClass: KClass<T>, factory: () -> T) {
            _factories[kClass] = factory
        }

        init {
            register(ApiInstance::class) { ApiInstance() }
        }
    }
}