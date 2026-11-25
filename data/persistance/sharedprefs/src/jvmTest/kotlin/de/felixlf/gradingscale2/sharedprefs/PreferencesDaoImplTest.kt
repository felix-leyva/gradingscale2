package de.felixlf.gradingscale2.sharedprefs

import io.github.xxfast.kstore.Codec
import io.github.xxfast.kstore.storeOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PreferencesDaoImplTest {

    @Test
    fun setLastSelectedGradeScaleId_returns_None_when_store_encode_throws() = runTest {
        val failingCodec = object : Codec<AppPreferences> {
            override suspend fun encode(value: AppPreferences?) {
                throw IllegalStateException("Disk full")
            }

            override suspend fun decode(): AppPreferences? = null
        }
        val store = storeOf(default = AppPreferences.default, codec = failingCodec)
        val sut = PreferencesDaoImpl(preferencesStore = store)

        val result = sut.setLastSelectedGradeScaleId("scale-1")

        assertTrue(result.isNone())
    }

    @Test
    fun setLastSelectedGradeScaleId_persists_value_on_success() = runTest {
        var stored: AppPreferences? = null
        val inMemoryCodec = object : Codec<AppPreferences> {
            override suspend fun encode(value: AppPreferences?) {
                stored = value
            }

            override suspend fun decode(): AppPreferences? = stored
        }
        val store = storeOf(default = AppPreferences.default, codec = inMemoryCodec)
        val sut = PreferencesDaoImpl(preferencesStore = store)

        val result = sut.setLastSelectedGradeScaleId("scale-1")

        assertTrue(result.isSome())
        assertEquals("scale-1", sut.getLastSelectedGradeScaleId().first())
    }

    @Test
    fun file_preference_store_ignores_unknown_json_keys_and_recovers_from_corrupted_file_A3_12() = runTest {
        val tempDir = Files.createTempDirectory("prefs-a3-12")
        try {
            val prefsFile = tempDir.resolve("preferences.json").toFile()
            // 1. Unknown JSON key from future version must not crash decoding
            prefsFile.writeText("""{"lastSelectedGradeScaleId":"scale-future","unknownFutureKey":42}""")
            val storeWithUnknownKeys = createJvmPreferenceStore(tempDir.toAbsolutePath().toString())
            val dao1 = PreferencesDaoImpl(storeWithUnknownKeys)
            assertEquals("scale-future", dao1.getLastSelectedGradeScaleId().first())

            // 2. Truncated / corrupted JSON must recover to AppPreferences.default instead of throwing SerializationException
            prefsFile.writeText("{corrupted-json")
            val storeWithCorruptedFile = createJvmPreferenceStore(tempDir.toAbsolutePath().toString())
            val dao2 = PreferencesDaoImpl(storeWithCorruptedFile)
            assertNull(dao2.getLastSelectedGradeScaleId().first())

            // And subsequent writes after corruption recovery must succeed
            assertTrue(dao2.setLastSelectedGradeScaleId("scale-recovered").isSome())
            assertEquals("scale-recovered", dao2.getLastSelectedGradeScaleId().first())
        } finally {
            tempDir.toFile().deleteRecursively()
        }
    }

    @Test
    fun resolveJvmPreferencesDir_uses_unversioned_dir_and_migrates_legacy_versioned_preferences_A3_16() = runTest {
        val tempRoot = Files.createTempDirectory("prefs-a3-16")
        try {
            val unversionedDir = tempRoot.resolve("unversioned").toFile()
            val legacyVersionedDir = tempRoot.resolve("1").toFile().apply { mkdirs() }
            val legacyFile = legacyVersionedDir.resolve("preferences.json")
            legacyFile.writeText("""{"lastSelectedGradeScaleId":"legacy-scale-id"}""")

            val resolvedDir = resolveJvmPreferencesDir(
                unversionedDir = unversionedDir.absolutePath,
                legacyVersionedDir = legacyVersionedDir.absolutePath,
            )
            assertEquals(unversionedDir.absolutePath, resolvedDir, "A3-16: Desktop preferences must use unversioned directory")

            val store = createJvmPreferenceStore(resolvedDir)
            val dao = PreferencesDaoImpl(store)
            assertEquals("legacy-scale-id", dao.getLastSelectedGradeScaleId().first(), "A3-16: Must migrate existing preferences.json from versioned folder")
        } finally {
            tempRoot.toFile().deleteRecursively()
        }
    }
}
