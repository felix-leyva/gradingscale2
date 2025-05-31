package de.felixlf.gradingscale2.storage

/**
 * iOS implementation - throws exceptions as this should only be used in WasmJS.
 */
actual object WasmJsStorage {
    actual fun getItem(key: String): String? =
        throw UnsupportedOperationException("WasmJsStorage is only for WasmJS platform")

    actual fun setItem(key: String, value: String) =
        throw UnsupportedOperationException("WasmJsStorage is only for WasmJS platform")

    actual fun removeItem(key: String) =
        throw UnsupportedOperationException("WasmJsStorage is only for WasmJS platform")

    actual fun clear() =
        throw UnsupportedOperationException("WasmJsStorage is only for WasmJS platform")
}
