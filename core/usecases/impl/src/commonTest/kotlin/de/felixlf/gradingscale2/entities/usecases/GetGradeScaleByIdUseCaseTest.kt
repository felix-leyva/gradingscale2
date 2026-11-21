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
import kotlin.test.assertNull

class GetGradeScaleByIdUseCaseTest {

    private lateinit var fakeRepository: FakeGradeScaleRepository
    private lateinit var useCase: GetGradeScaleByIdUseCaseImpl

    @BeforeTest
    fun setup() {
        fakeRepository = FakeGradeScaleRepository()
        useCase = GetGradeScaleByIdUseCaseImpl(fakeRepository)

        // Add test data
        val testGradeScale = GradeScale(
            id = "1",
            gradeScaleName = "Test Scale",
            totalPoints = 10.0,
            grades = persistentListOf(
                Grade(
                    nameOfScale = "Test Scale",
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
    fun `test get existing grade scale by id`() = runTest {
        // Given existing grade scale with ID "1"

        // When/Then
        useCase("1").test {
            val gradeScale = awaitItem()
            assertEquals("1", gradeScale?.id)
            assertEquals("Test Scale", gradeScale?.gradeScaleName)
            assertEquals(10.0, gradeScale?.totalPoints)
            assertEquals(1, gradeScale?.grades?.size)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `test get non-existing grade scale by id`() = runTest {
        // Given: No grade scale with ID "999"

        // When/Then
        useCase("999").test {
            val gradeScale = awaitItem()
            assertNull(gradeScale, "Should return null for non-existent ID")
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `test get grade scale after update`() = runTest {
        // Given
        val updatedGradeScale = GradeScale(
            id = "1",
            gradeScaleName = "Updated Test Scale",
            totalPoints = 15.0,
            grades = persistentListOf(
                Grade(
                    nameOfScale = "Updated Test Scale",
                    namedGrade = "A",
                    percentage = 0.9,
                    idOfGradeScale = "1",
                    uuid = "uuid1",
                ),
            ),
        )

        // When
        fakeRepository.upsertGradeScale(updatedGradeScale)

        // Then
        useCase("1").test {
            val gradeScale = awaitItem()
            assertEquals("Updated Test Scale", gradeScale?.gradeScaleName)
            assertEquals(15.0, gradeScale?.totalPoints)
            cancelAndConsumeRemainingEvents()
        }
    }
}
