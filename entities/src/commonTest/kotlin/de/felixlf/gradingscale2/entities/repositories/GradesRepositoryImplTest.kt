package de.felixlf.gradingscale2.entities.repositories

import app.cash.turbine.test
import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import de.felixlf.gradingscale2.entities.daos.GradesDao
import de.felixlf.gradingscale2.entities.models.Grade
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GradesRepositoryImplTest {

    private lateinit var mockDao: FakeGradesDao
    private lateinit var repository: GradesRepositoryImpl

    @BeforeTest
    fun setup() {
        mockDao = FakeGradesDao()
    }

    @Test
    fun `getAllGradesFromGradeScale returns grades for a grade scale`() = runTest {
        repository = GradesRepositoryImpl(mockDao, this)

        // Given
        val gradeScaleId = "1"
        val grade1 = Grade(
            namedGrade = "A",
            percentage = 0.9,
            idOfGradeScale = gradeScaleId,
            nameOfScale = "Test Scale",
            uuid = "uuid1",
        )
        val grade2 = Grade(
            namedGrade = "B",
            percentage = 0.8,
            idOfGradeScale = gradeScaleId,
            nameOfScale = "Test Scale",
            uuid = "uuid2",
        )
        mockDao.addGrade(grade1)
        mockDao.addGrade(grade2)

        // When/Then
        repository.getAllGradesFromGradeScale(gradeScaleId).test {
            val grades = awaitItem()
            assertEquals(2, grades.size)
            assertTrue(grades.any { it.namedGrade == "A" })
            assertTrue(grades.any { it.namedGrade == "B" })
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `getAllGradesFromGradeScale returns empty list for non-existing grade scale`() = runTest {
        // Given
        repository = GradesRepositoryImpl(mockDao, this)
        val nonExistingGradeScaleId = "999"

        // When/Then
        repository.getAllGradesFromGradeScale(nonExistingGradeScaleId).test {
            val grades = awaitItem()
            assertTrue(grades.isEmpty())
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `getGradeById returns grade when it exists`() = runTest {
        // Given
        repository = GradesRepositoryImpl(mockDao, this)
        val gradeId = "uuid1"
        val grade = Grade(
            namedGrade = "A",
            percentage = 0.9,
            idOfGradeScale = "1",
            nameOfScale = "Test Scale",
            uuid = gradeId,
        )
        mockDao.addGrade(grade)

        // When/Then
        repository.getGradeById(gradeId).test {
            val result = awaitItem()
            assertEquals("A", result?.namedGrade)
            assertEquals(0.9, result?.percentage)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `getGradeById returns null for non-existing grade`() = runTest {
        // Given
        repository = GradesRepositoryImpl(mockDao, this)
        val nonExistingGradeId = "non-existing"

        // When/Then
        repository.getGradeById(nonExistingGradeId).test {
            val result = awaitItem()
            assertNull(result)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `upsertGrade delegates to DAO and returns its result`() = runTest {
        // Given
        repository = GradesRepositoryImpl(mockDao, this)
        val grade = Grade(
            namedGrade = "A",
            percentage = 0.9,
            idOfGradeScale = "1",
            nameOfScale = "Test Scale",
            uuid = "uuid1",
        )
        mockDao.success = true

        // When
        val result = repository.upsertGrade(grade)

        // Then
        assertTrue(result is Some)
        assertEquals(1, mockDao.grades.size)
        assertEquals("A", mockDao.grades.first().namedGrade)
    }

    @Test
    fun `upsertGrade returns None when DAO fails`() = runTest {
        // Given
        repository = GradesRepositoryImpl(mockDao, this)
        val grade = Grade(
            namedGrade = "A",
            percentage = 0.9,
            idOfGradeScale = "1",
            nameOfScale = "Test Scale",
            uuid = "uuid1",
        )
        mockDao.success = false

        // When
        val result = repository.upsertGrade(grade)

        // Then
        assertTrue(result is None)
        assertEquals(0, mockDao.grades.size)
    }

    @Test
    fun `deleteGrade delegates to DAO and returns its result`() = runTest {
        // Given
        repository = GradesRepositoryImpl(mockDao, this)
        val gradeId = "uuid1"
        val grade = Grade(
            namedGrade = "A",
            percentage = 0.9,
            idOfGradeScale = "1",
            nameOfScale = "Test Scale",
            uuid = gradeId,
        )
        mockDao.addGrade(grade)
        mockDao.success = true

        // When
        val result = repository.deleteGrade(gradeId)

        // Then
        assertTrue(result is Some)
        assertEquals(0, mockDao.grades.size)
    }

    @Test
    fun `deleteGrade returns None when DAO fails`() = runTest {
        // Given
        repository = GradesRepositoryImpl(mockDao, this)
        val gradeId = "uuid1"
        val grade = Grade(
            namedGrade = "A",
            percentage = 0.9,
            idOfGradeScale = "1",
            nameOfScale = "Test Scale",
            uuid = gradeId,
        )
        mockDao.addGrade(grade)
        mockDao.success = false

        // When
        val result = repository.deleteGrade(gradeId)

        // Then
        assertTrue(result is None)
        assertEquals(1, mockDao.grades.size)
    }

    @Test
    fun `deleteGrade returns None for non-existing grade`() = runTest {
        // Given
        repository = GradesRepositoryImpl(mockDao, this)
        val nonExistingGradeId = "non-existing"
        mockDao.success = true

        // When
        val result = repository.deleteGrade(nonExistingGradeId)

        // Then
        assertTrue(result is None)
    }
}

/**
 * A simple fake implementation of GradesDao for testing.
 */
class FakeGradesDao : GradesDao {
    val grades = mutableListOf<Grade>()
    var success = true

    override fun getAllGradesFromGradeScale(gradeScaleId: String) = flow {
        emit(grades.filter { it.idOfGradeScale == gradeScaleId })
    }

    override fun getGradeById(gradeId: String) = flow {
        emit(grades.find { it.uuid == gradeId })
    }

    override suspend fun upsertGrade(grade: Grade): Option<Long> {
        if (!success) return None

        val existingIndex = grades.indexOfFirst { it.uuid == grade.uuid }
        if (existingIndex >= 0) {
            grades[existingIndex] = grade
        } else {
            grades.add(grade)
        }
        return Some(1)
    }

    override suspend fun deleteGrade(gradeId: String): Option<Unit> {
        if (!success) return None

        val removed = grades.removeAll { it.uuid == gradeId }
        return if (removed) Some(Unit) else None
    }

    fun addGrade(grade: Grade) {
        grades.add(grade)
    }
}
