package de.felixlf.gradingscale2.sharedprefs

import de.felixlf.gradingscale2.storage.PersistentStoreFactory
import de.felixlf.gradingscale2.storage.StorageUtils
import org.koin.core.module.Module
import org.koin.dsl.module
import org.koin.plugin.module.dsl.create

private fun providePreferenceStore(): PreferenceStore =
    PersistentStoreFactory.create(
        default = AppPreferences.default,
        serializer = AppPreferences.serializer(),
        storageKey = StorageUtils.Keys.PREFERENCES,
    )

/**
 * Platform-specific preferences module for WasmJS.
 * Uses browser localStorage for persistence via the shared storage module.
 */
actual fun getPlatformPreferencesModule(): Module = module {
    single { create(::providePreferenceStore) }
}
