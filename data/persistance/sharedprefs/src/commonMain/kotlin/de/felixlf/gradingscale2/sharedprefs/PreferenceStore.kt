package de.felixlf.gradingscale2.sharedprefs

import io.github.xxfast.kstore.Codec
import io.github.xxfast.kstore.KStore
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.Json

typealias PreferenceStore = KStore<AppPreferences>

internal val preferencesJson: Json = Json {
    encodeDefaults = true
    ignoreUnknownKeys = true
    isLenient = true
}

internal class ResilientCodec<T : Any>(
    private val delegate: Codec<T>,
    private val clearOnDecodeError: Boolean = true,
) : Codec<T> {
    override suspend fun encode(value: T?) {
        delegate.encode(value)
    }

    override suspend fun decode(): T? = try {
        delegate.decode()
    } catch (e: CancellationException) {
        throw e
    } catch (_: Exception) {
        if (clearOnDecodeError) {
            runCatching { delegate.encode(null) }
        }
        null
    }
}
