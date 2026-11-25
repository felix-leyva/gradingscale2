package de.felixlf.gradingscale2.sharedprefs

import android.content.Context
import io.github.xxfast.kstore.file.FileCodec
import io.github.xxfast.kstore.storeOf
import kotlinx.io.files.Path
import org.koin.core.module.Module
import org.koin.dsl.module
import org.koin.plugin.module.dsl.create

private fun providePreferenceStore(context: Context): PreferenceStore {
    val path = context.filesDir.absolutePath
    val filePath = "$path/preferences.json"
    return storeOf(
        codec = ResilientCodec(FileCodec(file = Path(filePath), json = preferencesJson)),
        default = AppPreferences.default,
    )
}

actual fun getPlatformPreferencesModule(): Module = module {
    single { create(::providePreferenceStore) }
}
