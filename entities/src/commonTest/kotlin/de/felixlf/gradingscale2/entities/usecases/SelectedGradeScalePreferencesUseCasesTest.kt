package de.felixlf.gradingscale2.entities.usecases

import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import de.felixlf.gradingscale2.entities.repositories.PreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SelectedGradeScalePreferencesUseCasesTest {

    private class FakePreferencesRepository : PreferencesRepository {
        val flow = MutableStateFlow<String?>(null)
        var shouldFail = false
        var savedId: String? = null

        override fun getLastSelectedGradeScaleId(): Flow<String?> = flow

        override suspend fun setLastSelectedGradeScaleId(gradeScaleId: String): Option<Unit> {
            savedId = gradeScaleId
            return if (shouldFail) None else {
                flow.value = gradeScaleId
                Some(Unit)
            }
        }
    }

    private val repository = FakePreferencesRepository()

    @Test
    fun getLastSelectedGradeScaleIdUseCase_retrievesFirstFromRepository() = runTest {
        val useCase = GetLastSelectedGradeScaleIdUseCaseImpl(repository)
        repository.flow.value = "my-scale"

        val result = useCase()
        assertEquals("my-scale", result)
    }

    @Test
    fun setLastSelectedGradeScaleIdUseCase_delegatesToRepository() = runTest {
        val useCase = SetLastSelectedGradeScaleIdUseCaseImpl(repository)

        val result = useCase("new-scale")
        assertEquals(Some(Unit), result)
        assertEquals("new-scale", repository.savedId)

        repository.shouldFail = true
        val failedResult = useCase("fail-scale")
        assertTrue(failedResult.isNone())
    }
}
