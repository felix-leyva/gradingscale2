package de.felixlf.gradingscale2.daosimpl

import arrow.core.some
import de.felixlf.gradingscale2.entities.models.weightedgrade.WeightedGrade
import de.felixlf.gradingscale2.store.WeightedGradesStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedGradeDaoStoreImplTest {

    private class TestWeightedGradesStore : WeightedGradesStore {
        private val grades = MutableStateFlow<List<WeightedGrade>>(emptyList())

        override fun getAllWeightedGrades(): Flow<List<WeightedGrade>> = grades

        override suspend fun upsertWeightedGrade(weightedGrade: WeightedGrade) {
            val currentGrades = grades.value.toMutableList()
            val index = currentGrades.indexOfFirst { it.uuid == weightedGrade.uuid }
            if (index != -1) {
                currentGrades[index] = weightedGrade
            } else {
                currentGrades.add(weightedGrade)
            }
            grades.value = currentGrades
        }

        override suspend fun deleteWeightedGrade(weightedGradeId: String): Boolean {
            val currentGrades = grades.value.toMutableList()
            val indexToRemove = currentGrades.indexOfFirst { it.uuid == weightedGradeId }
            return if (indexToRemove != -1) {
                currentGrades.removeAt(indexToRemove)
                grades.value = currentGrades
                true
            } else {
                false
            }
        }
    }

    @Test
    fun `should get all weighted grades`() = runTest {
        // Given
        val store = TestWeightedGradesStore()
        val dao = WeightedGradeDaoStoreImpl(store)
        val grade1 = WeightedGrade(percentage = 0.85, weight = 0.5, uuid = "1")
        val grade2 = WeightedGrade(percentage = 0.92, weight = 0.5, uuid = "2")

        // When
        store.upsertWeightedGrade(grade1)
        store.upsertWeightedGrade(grade2)
        val grades = dao.getAllWeightedGrades().first()

        // Then
        assertEquals(2, grades.size)
        assertTrue(grades.contains(grade1))
        assertTrue(grades.contains(grade2))
    }

    @Test
    fun `should upsert new weighted grade`() = runTest {
        // Given
        val store = TestWeightedGradesStore()
        val dao = WeightedGradeDaoStoreImpl(store)
        val grade = WeightedGrade(percentage = 0.75, weight = 1.0, uuid = "test-id")

        // When
        val result = dao.upsertWeightedGrade(grade)

        // Then
        assertEquals(0L.some(), result)
        val grades = dao.getAllWeightedGrades().first()
        assertEquals(1, grades.size)
        assertEquals(grade, grades.first())
    }

    @Test
    fun `should update existing weighted grade`() = runTest {
        // Given
        val store = TestWeightedGradesStore()
        val dao = WeightedGradeDaoStoreImpl(store)
        val originalGrade = WeightedGrade(percentage = 0.75, weight = 1.0, uuid = "test-id")
        val updatedGrade = WeightedGrade(percentage = 0.85, weight = 1.0, uuid = "test-id")

        // When
        dao.upsertWeightedGrade(originalGrade)
        dao.upsertWeightedGrade(updatedGrade)
        val grades = dao.getAllWeightedGrades().first()

        // Then
        assertEquals(1, grades.size)
        assertEquals(updatedGrade, grades.first())
    }

    @Test
    fun `should delete weighted grade successfully`() = runTest {
        // Given
        val store = TestWeightedGradesStore()
        val dao = WeightedGradeDaoStoreImpl(store)
        val grade = WeightedGrade(percentage = 0.75, weight = 1.0, uuid = "test-id")
        dao.upsertWeightedGrade(grade)

        // When
        val result = dao.deleteWeightedGrade("test-id")

        // Then
        assertTrue(result.isSome())
        val grades = dao.getAllWeightedGrades().first()
        assertEquals(0, grades.size)
    }

    @Test
    fun `should return none when deleting non-existent grade`() = runTest {
        // Given
        val store = TestWeightedGradesStore()
        val dao = WeightedGradeDaoStoreImpl(store)

        // When
        val result = dao.deleteWeightedGrade("non-existent-id")

        // Then
        assertTrue(result.isNone())
    }

    @Test
    fun `should handle multiple grades with different weights`() = runTest {
        // Given
        val store = TestWeightedGradesStore()
        val dao = WeightedGradeDaoStoreImpl(store)
        val grades = listOf(
            WeightedGrade(percentage = 0.90, weight = 0.3, uuid = "1"),
            WeightedGrade(percentage = 0.85, weight = 0.3, uuid = "2"),
            WeightedGrade(percentage = 0.95, weight = 0.4, uuid = "3"),
        )

        // When
        grades.forEach { dao.upsertWeightedGrade(it) }
        val retrievedGrades = dao.getAllWeightedGrades().first()

        // Then
        assertEquals(3, retrievedGrades.size)
        assertEquals(1.0, retrievedGrades.sumOf { it.weight }, 0.001)
    }
}
