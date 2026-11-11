package de.felixlf.gradingscale2.sharedprefs

import de.felixlf.gradingscale2.BuildConfigs.ORGANIZATION
import de.felixlf.gradingscale2.BuildConfigs.PACKAGE_NAME
import de.felixlf.gradingscale2.BuildConfigs.VERSION_CODE
import io.github.xxfast.kstore.file.storeOf
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import net.harawata.appdirs.AppDirsFactory
import org.koin.core.module.Module
import org.koin.dsl.module
import org.koin.plugin.module.dsl.create

private fun providePreferenceStore(): PreferenceStore {
    val filesDir = AppDirsFactory.getInstance().getUserDataDir(PACKAGE_NAME, VERSION_CODE, ORGANIZATION)
    val files = Path(filesDir)
    with(SystemFileSystem) { if (!exists(files)) createDirectories(files) }
    val filePath = "$filesDir/preferences.json"
    return storeOf(file = Path(filePath), default = AppPreferences.default)
}

actual fun getPlatformPreferencesModule(): Module = module {
    single { create(::providePreferenceStore) }
}
