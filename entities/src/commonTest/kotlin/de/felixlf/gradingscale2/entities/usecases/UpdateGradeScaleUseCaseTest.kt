package de.felixlf.gradingscale2.entities.usecases

import app.cash.turbine.test
import de.felixlf.gradingscale2.entities.models.Grade
import de.felixlf.gradingscale2.entities.models.GradeScale
import de.felixlf.gradingscale2.entities.repositories.FakeGradeScaleRepository
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UpdateGradeScaleUseCaseTest {

    private lateinit var fakeRepository: FakeGradeScaleRepository
    private lateinit var useCase: UpdateGradeScaleUseCaseImpl

    @BeforeTest
    fun setup() {
        fakeRepository = FakeGradeScaleRepository()
        useCase = UpdateGradeScaleUseCaseImpl(fakeRepository)

        // Add test data
        val testGradeScale = GradeScale(
            id = "1",
            gradeScaleName = "Original Scale Name",
            totalPoints = 10.0,
            grades = persistentListOf(
                Grade(
                    nameOfScale = "Original Scale Name",
                    namedGrade = "A",
                    percentage = 0.9,
                    idOfGradeScale = "1",
                    uuid = "uuid1",
                ),
            ),
        )
        fakeRepository.addGradeScale(testGradeScale)
    }

    @Test
    fun `test update existing grade scale name`() = runTest {
        // Given
        val newName = "Updated Scale Name"
        val gradeScaleId = "1"
        val defaultGradeName = "A" // Doesn't matter for this test

        // When
        val result = useCase(newName, gradeScaleId, defaultGradeName)

        // Then
        assertTrue(result.isSome())
        result.fold(
            ifEmpty = { /* Already checked with assertTrue above */ },
            ifSome = { id: String -> assertEquals("1", id) }, // ID should remain the same
        )

        // Verify repository state
        fakeRepository.getGradeScaleById("1").test {
            val scale = awaitItem()
            assertEquals("Updated Scale Name", scale?.gradeScaleName)
            assertEquals(10.0, scale?.totalPoints) // Should remain unchanged
            assertEquals(1, scale?.grades?.size) // Grades should remain unchanged

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `test update non-existing grade scale`() = runTest {
        // Given
        val newName = "New Scale Name"
        val nonExistentId = "999"
        val defaultGradeName = "A"

        // When
        val result = useCase(newName, nonExistentId, defaultGradeName)

        // Then
        assertTrue(result.isNone())

        // Verify repository state (unchanged)
        fakeRepository.getGradeScales().test {
            val scales = awaitItem()
            assertEquals(1, scales.size)
            assertEquals("Original Scale Name", scales.first().gradeScaleName)

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `test update when repository fails`() = runTest {
        // Given
        fakeRepository.shouldFail = true
        val newName = "Updated Scale Name"
        val gradeScaleId = "1"
        val defaultGradeName = "A"

        // When
        val result = useCase(newName, gradeScaleId, defaultGradeName)

        // Then
        assertTrue(result.isNone())

        // Verify repository state (unchanged)
        fakeRepository.getGradeScaleById("1").test {
            val scale = awaitItem()
            assertEquals("Original Scale Name", scale?.gradeScaleName)

            cancelAndConsumeRemainingEvents()
        }
    }
}
