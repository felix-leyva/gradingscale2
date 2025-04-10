package de.felixlf.gradingscale2.entities.usecases

import app.cash.turbine.test
import de.felixlf.gradingscale2.entities.models.Grade
import de.felixlf.gradingscale2.entities.repositories.FakeGradesRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class UpsertGradeUseCaseTest {

    private lateinit var fakeRepository: FakeGradesRepository
    private lateinit var useCase: UpsertGradeUseCaseImpl

    @BeforeTest
    fun setup() {
        fakeRepository = FakeGradesRepository()
        useCase = UpsertGradeUseCaseImpl(fakeRepository)

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
    fun `test insert new grade`() = runTest {
        // Given
        val newGrade = Grade(
            nameOfScale = "Test Scale",
            namedGrade = "B",
            percentage = 0.8,
            idOfGradeScale = "1",
            uuid = "uuid2",
        )

        // When
        val result = useCase(newGrade)

        // Then
        assertTrue(result.isSome())

        // Verify repository state
        fakeRepository.getGradeById("uuid2").test {
            val grade = awaitItem()
            assertEquals("B", grade?.namedGrade)
            assertEquals(0.8, grade?.percentage)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `test update existing grade`() = runTest {
        // Given
        val updatedGrade = Grade(
            nameOfScale = "Updated Scale Name",
            namedGrade = "A+",
            percentage = 0.95,
            idOfGradeScale = "1",
            uuid = "uuid1", // Same UUID as existing grade
        )

        // When
        val result = useCase(updatedGrade)

        // Then
        assertTrue(result.isSome())

        // Verify repository state
        fakeRepository.getGradeById("uuid1").test {
            val grade = awaitItem()
            assertEquals("A+", grade?.namedGrade)
            assertEquals(0.95, grade?.percentage)
            assertEquals("Updated Scale Name", grade?.nameOfScale)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `test upsert when repository fails`() = runTest {
        // Given
        fakeRepository.shouldFail = true
        val newGrade = Grade(
            nameOfScale = "Test Scale",
            namedGrade = "B",
            percentage = 0.8,
            idOfGradeScale = "1",
            uuid = "uuid2",
        )

        // When
        val result = useCase(newGrade)

        // Then
        assertTrue(result.isNone())

        // Verify repository state is unchanged - new grade not added
        fakeRepository.getGradeById("uuid2").test {
            val grade = awaitItem()
            assertNull(grade)
            cancelAndConsumeRemainingEvents()
        }
    }
}
