package de.felixlf.gradingscale2.storage

import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Serializable
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class WasmJsStorageCodecTest {

    @Serializable
    private data class SamplePayload(val name: String)

    @Test
    fun decode_returns_null_without_throwing_when_both_getItem_and_removeItem_throw_SecurityError_A3_09() = runTest {
        val restrictedStorage = object : KeyValueStorage {
            override fun getItem(key: String): String? {
                throw RuntimeException("SecurityError: Failed to read the 'localStorage' property from 'Window'")
            }

            override fun setItem(key: String, value: String) {
                throw RuntimeException("SecurityError: localStorage access is denied")
            }

            override fun removeItem(key: String) {
                throw RuntimeException("SecurityError: localStorage removeItem is denied")
            }

            override fun clear() {
                throw RuntimeException("SecurityError: localStorage clear is denied")
            }
        }

        val codec = WasmJsStorageCodec(
            serializer = SamplePayload.serializer(),
            storageKey = "test_key",
            config = StorageConfig(enableLogging = false, clearOnError = true),
            storage = restrictedStorage,
        )

        val decoded = codec.decode()
        assertNull(decoded, "A3-09: decode() must return null instead of crashing when removeItem throws SecurityError")
    }

    @Test
    fun decode_clears_corrupted_json_and_returns_null() = runTest {
        val backing = mutableMapOf("test_key" to "{corrupted")
        val inMemoryStorage = object : KeyValueStorage {
            override fun getItem(key: String): String? = backing[key]
            override fun setItem(key: String, value: String) {
                backing[key] = value
            }

            override fun removeItem(key: String) {
                backing.remove(key)
            }

            override fun clear() {
                backing.clear()
            }
        }

        val codec = WasmJsStorageCodec(
            serializer = SamplePayload.serializer(),
            storageKey = "test_key",
            config = StorageConfig(enableLogging = false, clearOnError = true),
            storage = inMemoryStorage,
        )

        assertNull(codec.decode())
        assertEquals(false, backing.containsKey("test_key"))
    }
}
