package de.felixlf.gradingscale2.sharedprefs

import io.github.xxfast.kstore.file.storeOf
import kotlinx.io.files.Path
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun getPlatformPreferencesModule(): Module = module {
    single<PreferenceStore> {
        val path = androidContext().filesDir.absolutePath
        val filePath = "$path/preferences.json"
        storeOf(file = Path(filePath), default = AppPreferences.default)
    }
}
