@file:OptIn(ExperimentalSerializationApi::class)

package de.felixlf.gradingscale2.sharedprefs

import io.github.xxfast.kstore.Codec
import io.github.xxfast.kstore.storeOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import org.koin.core.module.Module
import org.koin.dsl.module

// For WasmJS, we use storeOf with a simple in-memory codec
actual fun getPlatformPreferencesModule(): Module = module {
    single<PreferenceStore> {
        val jsonFormat = Json {
            encodeDefaults = true
            ignoreUnknownKeys = true
            isLenient = true
        }
        
        // In-memory storage for WasmJS
        val memoryStorage = MutableStateFlow<String?>(null)
        
        storeOf(
            default = AppPreferences.default,
            codec = object : Codec<AppPreferences> {
                override suspend fun encode(value: AppPreferences?) {
                    val jsonString = if (value != null) {
                        jsonFormat.encodeToString(AppPreferences.serializer(), value)
                    } else {
                        null
                    }
                    memoryStorage.value = jsonString
                }

                override suspend fun decode(): AppPreferences? {
                    val jsonString = memoryStorage.value
                    return if (jsonString?.isNotEmpty() == true) {
                        jsonFormat.decodeFromString(AppPreferences.serializer(), jsonString)
                    } else {
                        null
                    }
                }
            }
        )
    }
}