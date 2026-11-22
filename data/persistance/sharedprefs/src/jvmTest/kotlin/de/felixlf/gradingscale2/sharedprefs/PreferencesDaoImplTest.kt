package de.felixlf.gradingscale2.sharedprefs

import io.github.xxfast.kstore.Codec
import io.github.xxfast.kstore.storeOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
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
}
