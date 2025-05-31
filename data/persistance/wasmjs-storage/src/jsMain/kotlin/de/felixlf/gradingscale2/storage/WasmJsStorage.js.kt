package de.felixlf.gradingscale2.storage

/**
 * External declaration for browser localStorage API.
 */
private external object localStorage {
    fun getItem(key: String): String?
    fun setItem(key: String, value: String)
    fun removeItem(key: String)
    fun clear()
}

/**
 * JS implementation using browser localStorage (same as WasmJS).
 */
actual object WasmJsStorage {
    actual fun getItem(key: String): String? = localStorage.getItem(key)

    actual fun setItem(key: String, value: String) = localStorage.setItem(key, value)

    actual fun removeItem(key: String) = localStorage.removeItem(key)

    actual fun clear() = localStorage.clear()
}
