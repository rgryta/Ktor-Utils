package eu.gryta.ktor.utils

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

open class ApiInstance {
    var token: Any? = null

    companion object {
        private var _instance: ApiInstance? = null
        private val lock = Mutex()

        // Function that works like a property but is a suspending function
        suspend fun getInstance(): ApiInstance {
            return _instance ?: lock.withLock {
                _instance ?: ApiInstance().also { _instance = it }
            }
        }
    }
}
