package de.felixlf.gradingscale2.entities.repositories

import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import de.felixlf.gradingscale2.entities.daos.WeightedGradeDao
import de.felixlf.gradingscale2.entities.models.weightedgrade.WeightedGrade
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedGradesRepositoryImplTest {

    private class FakeWeightedGradeDao : WeightedGradeDao {
        val gradesFlow = MutableStateFlow<List<WeightedGrade>>(emptyList())
        var shouldFail = false
        var lastUpserted: WeightedGrade? = null
        var lastDeletedId: String? = null

        override fun getAllWeightedGrades(): Flow<List<WeightedGrade>> = gradesFlow

        override suspend fun upsertWeightedGrade(weightedGrade: WeightedGrade): Option<Long> {
            lastUpserted = weightedGrade
            return if (shouldFail) None else Some(1L)
        }

        override suspend fun deleteWeightedGrade(weightedGradeId: String): Option<Unit> {
            lastDeletedId = weightedGradeId
            return if (shouldFail) None else Some(Unit)
        }
    }

    private val dao = FakeWeightedGradeDao()
    private val repository = WeightedGradesRepositoryImpl(dao)

    @Test
    fun getAllWeightedGrades_delegatesToDao() = runTest {
        val sampleGrades = listOf(
            WeightedGrade(percentage = 0.85, weight = 1.0, uuid = "g1"),
            WeightedGrade(percentage = 0.90, weight = 2.0, uuid = "g2"),
        )
        dao.gradesFlow.value = sampleGrades

        val result = repository.getAllWeightedGrades().first()
        assertEquals(sampleGrades, result)
    }

    @Test
    fun upsertWeightedGrade_delegatesToDaoAndReturnsSuccess() = runTest {
        val grade = WeightedGrade(percentage = 0.75, weight = 1.5, uuid = "g3")
        val result = repository.upsertWeightedGrade(grade)

        assertEquals(Some(1L), result)
        assertEquals(grade, dao.lastUpserted)
    }

    @Test
    fun upsertWeightedGrade_returnsNoneOnDaoFailure() = runTest {
        dao.shouldFail = true
        val grade = WeightedGrade(percentage = 0.75, weight = 1.5, uuid = "g3")
        val result = repository.upsertWeightedGrade(grade)

        assertTrue(result.isNone())
        assertEquals(grade, dao.lastUpserted)
    }

    @Test
    fun deleteWeightedGrade_delegatesToDaoAndReturnsSuccess() = runTest {
        val result = repository.deleteWeightedGrade("g1")

        assertEquals(Some(Unit), result)
        assertEquals("g1", dao.lastDeletedId)
    }

    @Test
    fun deleteWeightedGrade_returnsNoneOnDaoFailure() = runTest {
        dao.shouldFail = true
        val result = repository.deleteWeightedGrade("g1")

        assertTrue(result.isNone())
        assertEquals("g1", dao.lastDeletedId)
    }
}
