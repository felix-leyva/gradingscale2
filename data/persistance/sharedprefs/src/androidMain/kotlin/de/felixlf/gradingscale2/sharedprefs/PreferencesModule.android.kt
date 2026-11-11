package de.felixlf.gradingscale2.sharedprefs

import android.content.Context
import io.github.xxfast.kstore.file.storeOf
import kotlinx.io.files.Path
import org.koin.core.module.Module
import org.koin.dsl.module
import org.koin.plugin.module.dsl.create

private fun providePreferenceStore(context: Context): PreferenceStore {
    val path = context.filesDir.absolutePath
    val filePath = "$path/preferences.json"
    return storeOf(file = Path(filePath), default = AppPreferences.default)
}

actual fun getPlatformPreferencesModule(): Module = module {
    single { create(::providePreferenceStore) }
}
