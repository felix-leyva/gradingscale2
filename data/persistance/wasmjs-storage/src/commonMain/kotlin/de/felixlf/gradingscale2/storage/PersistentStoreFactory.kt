package de.felixlf.gradingscale2.storage

import io.github.xxfast.kstore.Codec
import io.github.xxfast.kstore.KStore
import io.github.xxfast.kstore.storeOf
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json

/**
 * Configuration for persistent storage.
 */
data class StorageConfig(
    val enableLogging: Boolean = true,
    val clearOnError: Boolean = true,
    val json: Json = defaultJson,
) {
    companion object {
        /**
         * Default JSON configuration for storage.
         */
        val defaultJson = Json {
            encodeDefaults = true
            ignoreUnknownKeys = true
            isLenient = true
            prettyPrint = false // Minimize storage size
        }
    }
}

/**
 * Factory for creating persistent KStore instances.
 */
object PersistentStoreFactory {

    /**
     * Creates a persistent KStore using platform-specific storage.
     *
     * @param default The default value when no data is stored
     * @param serializer The serializer for the data type
     * @param storageKey The unique key for storage
     * @param config Storage configuration options
     * @return A KStore instance with persistence
     */
    inline fun <reified T : Any> create(
        default: T,
        serializer: KSerializer<T>,
        storageKey: String,
        config: StorageConfig = StorageConfig(),
    ): KStore<T> {
        return storeOf(
            default = default,
            codec = WasmJsStorageCodec(
                serializer = serializer,
                storageKey = storageKey,
                config = config,
            ),
        )
    }
}

/**
 * Codec implementation for WasmJS persistent storage.
 */
class WasmJsStorageCodec<T : Any>(
    private val serializer: KSerializer<T>,
    private val storageKey: String,
    private val config: StorageConfig = StorageConfig(),
) : Codec<T> {

    override suspend fun encode(value: T?) {
        try {
            if (value != null) {
                val jsonString = config.json.encodeToString(serializer, value)
                WasmJsStorage.setItem(storageKey, jsonString)
                if (config.enableLogging) {
                    println("✅ Persisted $storageKey (${jsonString.length} chars)")
                }
            } else {
                WasmJsStorage.removeItem(storageKey)
                if (config.enableLogging) {
                    println("🗑️ Removed $storageKey from storage")
                }
            }
        } catch (e: Exception) {
            if (config.enableLogging) {
                println("❌ Failed to encode $storageKey: ${e.message}")
            }
            throw e
        }
    }

    override suspend fun decode(): T? {
        return try {
            val jsonString = WasmJsStorage.getItem(storageKey)
            when {
                jsonString.isNullOrEmpty() -> {
                    if (config.enableLogging) {
                        println("ℹ️ No data found for $storageKey")
                    }
                    null
                }
                else -> {
                    val decoded = config.json.decodeFromString(serializer, jsonString)
                    if (config.enableLogging) {
                        println("✅ Loaded $storageKey (${jsonString.length} chars)")
                    }
                    decoded
                }
            }
        } catch (e: Exception) {
            if (config.enableLogging) {
                println("❌ Failed to decode $storageKey: ${e.message}")
            }
            if (config.clearOnError) {
                if (config.enableLogging) {
                    println("🔧 Clearing corrupted data")
                }
                WasmJsStorage.removeItem(storageKey)
            }
            null
        }
    }
}
