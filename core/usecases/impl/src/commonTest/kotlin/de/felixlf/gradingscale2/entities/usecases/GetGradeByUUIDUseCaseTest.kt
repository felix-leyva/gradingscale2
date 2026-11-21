package de.felixlf.gradingscale2.entities.usecases

import app.cash.turbine.test
import de.felixlf.gradingscale2.entities.models.Grade
import de.felixlf.gradingscale2.entities.repositories.FakeGradesRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class GetGradeByUUIDUseCaseTest {

    private lateinit var fakeRepository: FakeGradesRepository
    private lateinit var useCase: GetGradeByUUIDUseCaseImpl

    @BeforeTest
    fun setup() {
        fakeRepository = FakeGradesRepository()
        useCase = GetGradeByUUIDUseCaseImpl(fakeRepository)

        // Add test data
        val testGrade = Grade(
            nameOfScale = "Test Scale",
            namedGrade = "A",
            percentage = 0.9,
            idOfGradeScale = "1",
            uuid = "uuid1",
        )
        fakeRepository.addGrade(testGrade)
    }

    @Test
    fun `test get existing grade by UUID`() = runTest {
        // Given existing grade with UUID "uuid1"

        // When/Then
        useCase("uuid1").test {
            val grade = awaitItem()
            assertEquals("uuid1", grade?.uuid)
            assertEquals("A", grade?.namedGrade)
            assertEquals(0.9, grade?.percentage)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `test get non-existing grade by UUID`() = runTest {
        // Given: No grade with UUID "non-existent"

        // When/Then
        useCase("non-existent").test {
            val grade = awaitItem()
            assertNull(grade, "Should return null for non-existent UUID")
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `test get grade after update`() = runTest {
        // Given
        val updatedGrade = Grade(
            nameOfScale = "Test Scale",
            namedGrade = "A+",
            percentage = 0.95,
            idOfGradeScale = "1",
            uuid = "uuid1",
        )

        // When
        fakeRepository.upsertGrade(updatedGrade)

        // Then
        useCase("uuid1").test {
            val grade = awaitItem()
            assertEquals("A+", grade?.namedGrade)
            assertEquals(0.95, grade?.percentage)
            cancelAndConsumeRemainingEvents()
        }
    }
}
