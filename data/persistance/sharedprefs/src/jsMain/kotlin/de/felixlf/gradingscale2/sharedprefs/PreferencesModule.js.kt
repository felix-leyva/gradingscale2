@file:OptIn(ExperimentalSerializationApi::class)

package de.felixlf.gradingscale2.sharedprefs

import io.github.xxfast.kstore.storage.storeOf
import kotlinx.serialization.ExperimentalSerializationApi
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun getPlatformPreferencesModule(): Module = module {
    single<PreferenceStore> {
        storeOf(
            key = "preferences",
            default = AppPreferences.default,
        )
    }
}
