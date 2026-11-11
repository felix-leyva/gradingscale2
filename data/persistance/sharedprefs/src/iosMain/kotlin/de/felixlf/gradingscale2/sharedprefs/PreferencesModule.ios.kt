package de.felixlf.gradingscale2.sharedprefs

import io.github.xxfast.kstore.file.storeOf
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.io.files.Path
import org.koin.core.module.Module
import org.koin.dsl.module
import org.koin.plugin.module.dsl.create
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask

@OptIn(ExperimentalForeignApi::class)
private fun providePreferenceStore(): PreferenceStore {
    val fileManager: NSFileManager = NSFileManager.defaultManager
    val documentsUrl: NSURL = fileManager.URLForDirectory(
        directory = NSDocumentDirectory,
        appropriateForURL = null,
        create = false,
        inDomain = NSUserDomainMask,
        error = null,
    )!!

    val files = Path("${documentsUrl.path}/preferences.json")
    return storeOf(file = files, default = AppPreferences.default)
}

actual fun getPlatformPreferencesModule(): Module = module {
    single { create(::providePreferenceStore) }
}
