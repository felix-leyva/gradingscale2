package de.felixlf.gradingscale2.sharedprefs

import io.github.xxfast.kstore.storage.storeOf
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun getPlatformPreferencesModule(): Module = module {
    single<PreferenceStore> {
        storeOf("preferences", default = AppPreferences.default)
    }
}
