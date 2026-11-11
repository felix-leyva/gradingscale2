@file:OptIn(ExperimentalSerializationApi::class)

package de.felixlf.gradingscale2.sharedprefs

import io.github.xxfast.kstore.storage.storeOf
import kotlinx.serialization.ExperimentalSerializationApi
import org.koin.core.module.Module
import org.koin.dsl.module
import org.koin.plugin.module.dsl.create

private fun providePreferenceStore(): PreferenceStore =
    storeOf(
        key = "preferences",
        default = AppPreferences.default,
    )

actual fun getPlatformPreferencesModule(): Module = module {
    single { create(::providePreferenceStore) }
}
