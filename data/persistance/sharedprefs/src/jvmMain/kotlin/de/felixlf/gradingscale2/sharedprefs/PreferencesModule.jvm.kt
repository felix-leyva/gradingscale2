package de.felixlf.gradingscale2.sharedprefs

import de.felixlf.gradingscale2.BuildConfigs.ORGANIZATION
import de.felixlf.gradingscale2.BuildConfigs.PACKAGE_NAME
import de.felixlf.gradingscale2.BuildConfigs.VERSION_CODE
import io.github.xxfast.kstore.file.FileCodec
import io.github.xxfast.kstore.storeOf
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import net.harawata.appdirs.AppDirsFactory
import org.koin.core.module.Module
import org.koin.dsl.module
import org.koin.plugin.module.dsl.create
import java.io.File

private const val PREFERENCES_FILE_NAME = "preferences.json"

internal fun resolveJvmPreferencesDir(
    unversionedDir: String = AppDirsFactory.getInstance().getUserDataDir(PACKAGE_NAME, null, ORGANIZATION),
    legacyVersionedDir: String = AppDirsFactory.getInstance().getUserDataDir(PACKAGE_NAME, VERSION_CODE, ORGANIZATION),
): String {
    val targetDir = File(unversionedDir)
    val targetFile = File(targetDir, PREFERENCES_FILE_NAME)
    val legacyFile = File(legacyVersionedDir, PREFERENCES_FILE_NAME)
    if (!targetFile.exists() && legacyFile.exists() && targetFile.absolutePath != legacyFile.absolutePath) {
        runCatching {
            targetDir.mkdirs()
            legacyFile.copyTo(targetFile, overwrite = false)
        }
    }
    return unversionedDir
}

internal fun createJvmPreferenceStore(filesDir: String = resolveJvmPreferencesDir()): PreferenceStore {
    val files = Path(filesDir)
    with(SystemFileSystem) { if (!exists(files)) createDirectories(files) }
    val filePath = "$filesDir/$PREFERENCES_FILE_NAME"
    return storeOf(
        codec = ResilientCodec(FileCodec(file = Path(filePath), json = preferencesJson)),
        default = AppPreferences.default,
    )
}

private fun providePreferenceStore(): PreferenceStore = createJvmPreferenceStore()

actual fun getPlatformPreferencesModule(): Module = module {
    single { create(::providePreferenceStore) }
}
