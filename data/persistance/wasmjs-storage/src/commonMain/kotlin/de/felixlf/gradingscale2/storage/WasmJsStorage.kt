package de.felixlf.gradingscale2.storage

interface KeyValueStorage {
    fun getItem(key: String): String?
    fun setItem(key: String, value: String)
    fun removeItem(key: String)
    fun clear()
}

internal object DefaultKeyValueStorage : KeyValueStorage {
    override fun getItem(key: String): String? = WasmJsStorage.getItem(key)
    override fun setItem(key: String, value: String) = WasmJsStorage.setItem(key, value)
    override fun removeItem(key: String) = WasmJsStorage.removeItem(key)
    override fun clear() = WasmJsStorage.clear()
}

/**
 * Common interface for WasmJS storage operations.
 * This allows for different implementations (localStorage, IndexedDB, etc.)
 */
expect object WasmJsStorage {
    /**
     * Retrieves a value from storage.
     * @param key The storage key
     * @return The stored value or null if not found
     */
    fun getItem(key: String): String?

    /**
     * Stores a value in storage.
     * @param key The storage key
     * @param value The value to store
     */
    fun setItem(key: String, value: String)

    /**
     * Removes a value from storage.
     * @param key The storage key to remove
     */
    fun removeItem(key: String)

    /**
     * Clears all values from storage.
     * Use with caution!
     */
    fun clear()
}
