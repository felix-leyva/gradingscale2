package de.felixlf.gradingscale2.entities.usecases

import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import de.felixlf.gradingscale2.entities.models.weightedgrade.WeightedGrade
import de.felixlf.gradingscale2.entities.repositories.WeightedGradesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedGradesUseCasesTest {

    private class FakeWeightedGradesRepository : WeightedGradesRepository {
        val gradesFlow = MutableStateFlow<List<WeightedGrade>>(emptyList())
        var shouldFail = false
        var lastUpserted: WeightedGrade? = null
        var lastDeletedId: String? = null

        override fun getAllWeightedGrades(): Flow<List<WeightedGrade>> = gradesFlow

        override suspend fun upsertWeightedGrade(weightedGrade: WeightedGrade): Option<Long> {
            lastUpserted = weightedGrade
            return if (shouldFail) None else Some(10L)
        }

        override suspend fun deleteWeightedGrade(weightedGradeId: String): Option<Unit> {
            lastDeletedId = weightedGradeId
            return if (shouldFail) None else Some(Unit)
        }
    }

    private val repository = FakeWeightedGradesRepository()

    @Test
    fun getAllWeightedGradesUseCase_returnsImmutableListFromRepository() = runTest {
        val useCase = GetAllWeightedGradesUseCaseImpl(repository)
        val sample = listOf(
            WeightedGrade(percentage = 0.5, weight = 1.0, uuid = "g1"),
            WeightedGrade(percentage = 0.8, weight = 2.0, uuid = "g2"),
        )
        repository.gradesFlow.value = sample

        val result = useCase().first()
        assertEquals(sample, result)
    }

    @Test
    fun upsertWeightedGradeUseCase_delegatesToRepository() = runTest {
        val useCase = UpsertWeightedGradeUseCaseImpl(repository)
        val grade = WeightedGrade(percentage = 0.9, weight = 1.0, uuid = "g3")

        val result = useCase(grade)
        assertEquals(Some(10L), result)
        assertEquals(grade, repository.lastUpserted)

        repository.shouldFail = true
        val failedResult = useCase(grade)
        assertTrue(failedResult.isNone())
    }

    @Test
    fun deleteWeightedGradeUseCase_delegatesToRepository() = runTest {
        val useCase = DeleteWeightedGradeUseCaseImpl(repository)

        val result = useCase("del-id")
        assertEquals(Some(Unit), result)
        assertEquals("del-id", repository.lastDeletedId)

        repository.shouldFail = true
        val failedResult = useCase("del-id")
        assertTrue(failedResult.isNone())
    }
}
