package de.felixlf.gradingscale2.entities.repositories

import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import de.felixlf.gradingscale2.entities.daos.PreferencesDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PreferencesRepositoryImplTest {

    private class FakePreferencesDao : PreferencesDao {
        val lastSelectedIdFlow = MutableStateFlow<String?>(null)
        var shouldFail = false
        var lastSavedId: String? = null

        override fun getLastSelectedGradeScaleId(): Flow<String?> = lastSelectedIdFlow

        override suspend fun setLastSelectedGradeScaleId(gradeScaleId: String): Option<Unit> {
            lastSavedId = gradeScaleId
            return if (shouldFail) None else {
                lastSelectedIdFlow.value = gradeScaleId
                Some(Unit)
            }
        }
    }

    private val dao = FakePreferencesDao()
    private val repository = PreferencesRepositoryImpl(dao)

    @Test
    fun getLastSelectedGradeScaleId_delegatesToDao() = runTest {
        dao.lastSelectedIdFlow.value = "scale-123"

        val result = repository.getLastSelectedGradeScaleId().first()
        assertEquals("scale-123", result)
    }

    @Test
    fun setLastSelectedGradeScaleId_success() = runTest {
        val result = repository.setLastSelectedGradeScaleId("scale-456")

        assertEquals(Some(Unit), result)
        assertEquals("scale-456", dao.lastSavedId)
        assertEquals("scale-456", repository.getLastSelectedGradeScaleId().first())
    }

    @Test
    fun setLastSelectedGradeScaleId_failure() = runTest {
        dao.shouldFail = true
        val result = repository.setLastSelectedGradeScaleId("scale-789")

        assertTrue(result.isNone())
        assertEquals("scale-789", dao.lastSavedId)
    }
}
