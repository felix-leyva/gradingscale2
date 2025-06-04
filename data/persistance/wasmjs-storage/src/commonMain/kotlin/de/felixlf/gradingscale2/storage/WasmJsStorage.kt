package de.felixlf.gradingscale2.storage

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
