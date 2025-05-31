package de.felixlf.gradingscale2.storage

/**
 * Utility functions for WasmJS storage management.
 */
object StorageUtils {

    /**
     * Known storage keys used by the application.
     */
    object Keys {
        const val GRADE_SCALES = "grading_scale_grade_scales"
        const val WEIGHTED_GRADES = "grading_scale_weighted_grades"
        const val PREFERENCES = "grading_scale_preferences"

        val all = listOf(GRADE_SCALES, WEIGHTED_GRADES, PREFERENCES)
    }

    /**
     * Gets information about storage usage.
     * @return Map of storage keys to their sizes in characters
     */
    fun getStorageInfo(): Map<String, Int> {
        return Keys.all.mapNotNull { key ->
            WasmJsStorage.getItem(key)?.let { data ->
                key to data.length
            }
        }.toMap()
    }

    /**
     * Prints a formatted summary of storage usage.
     */
    fun printStorageInfo() {
        val info = getStorageInfo()
        if (info.isEmpty()) {
            println("📊 No data stored")
        } else {
            println("📊 Storage Usage:")
            var total = 0
            info.forEach { (key, size) ->
                println("  • $key: $size chars")
                total += size
            }
            println("  📦 Total: $total chars")
        }
    }

    /**
     * Clears specific storage keys.
     * @param keys The keys to clear
     */
    fun clearKeys(vararg keys: String) {
        keys.forEach { key ->
            WasmJsStorage.removeItem(key)
            println("🗑️ Cleared $key")
        }
    }

    /**
     * Clears all known application storage.
     */
    fun clearAllAppStorage() {
        clearKeys(*Keys.all.toTypedArray())
        println("🗑️ Cleared all application storage")
    }

    /**
     * Exports all storage data as JSON.
     * Useful for backup functionality.
     * @return JSON string containing all stored data
     */
    fun exportAllData(): String {
        val data = Keys.all.mapNotNull { key ->
            WasmJsStorage.getItem(key)?.let { value ->
                key to value
            }
        }.toMap()

        return StorageConfig.defaultJson.encodeToString(
            kotlinx.serialization.serializer<Map<String, String>>(),
            data,
        )
    }

    /**
     * Imports storage data from JSON.
     * Useful for restore functionality.
     * @param jsonData The JSON data to import
     * @return true if successful, false otherwise
     */
    fun importData(jsonData: String): Boolean {
        return try {
            val data = StorageConfig.defaultJson.decodeFromString(
                kotlinx.serialization.serializer<Map<String, String>>(),
                jsonData,
            )

            data.forEach { (key, value) ->
                WasmJsStorage.setItem(key, value)
            }

            println("✅ Imported ${data.size} storage entries")
            true
        } catch (e: Exception) {
            println("❌ Failed to import data: ${e.message}")
            false
        }
    }

    /**
     * Checks if a storage key exists.
     * @param key The key to check
     * @return true if the key exists
     */
    fun exists(key: String): Boolean {
        return WasmJsStorage.getItem(key) != null
    }

    /**
     * Gets the size of a specific storage key.
     * @param key The key to check
     * @return Size in characters, or 0 if not found
     */
    fun getSize(key: String): Int {
        return WasmJsStorage.getItem(key)?.length ?: 0
    }
}
