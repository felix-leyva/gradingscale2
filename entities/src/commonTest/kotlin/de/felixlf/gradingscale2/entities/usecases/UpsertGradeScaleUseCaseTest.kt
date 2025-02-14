package de.felixlf.gradingscale2.entities.usecases

import de.felixlf.gradingscale2.entities.repositories.GradeScaleRepositoryImpl
import de.felixlf.gradingscale2.entities.repositories.MockGradeScaleDao
import de.felixlf.gradingscale2.entities.util.MockGradeScalesGenerator
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UpsertGradeScaleUseCaseTest {
    private val dao: MockGradeScaleDao = MockGradeScaleDao()
    private lateinit var repo: GradeScaleRepositoryImpl
    private lateinit var upsertGradeScaleUseCase: InsertGradeScaleUseCaseImpl

    @BeforeTest
    fun setup() {
        dao.success = true
        dao.gradeScales.value = MockGradeScalesGenerator().gradeScales
        repo = GradeScaleRepositoryImpl(gradeScaleDao = dao)
        upsertGradeScaleUseCase = InsertGradeScaleUseCaseImpl(gradeScaleRepository = repo)
    }

    @Test
    fun `upsertGradeScale should return success with the id of the new gradeScale which was added`() = runTest {
        // Given
        val gradeScaleName = "Test"
        val defaultGradeName = "A"

        // When
        val result = upsertGradeScaleUseCase(gradeScaleName = gradeScaleName, defaultGradeName = defaultGradeName)

        // Then
        assertTrue(result.isSome())
        assertEquals("2", result.getOrNull())
    }

    @Test
    fun `upsertGradeScale should return failure`() = runTest {
        // Given
        val gradeScaleName = "Test"
        val defaultGradeName = "A"
        dao.success = false

        // When
        val result = upsertGradeScaleUseCase(gradeScaleName = gradeScaleName, defaultGradeName = defaultGradeName)

        // Then
        assertTrue(result.isNone())
    }
}
