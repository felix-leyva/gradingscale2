package de.felixlf.gradingscale2.entities.usecases

import app.cash.turbine.test
import de.felixlf.gradingscale2.entities.models.Grade
import de.felixlf.gradingscale2.entities.repositories.FakeGradesRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DeleteGradeUseCaseTest {

    private lateinit var fakeRepository: FakeGradesRepository
    private lateinit var useCase: DeleteGradeUseCaseImpl

    @BeforeTest
    fun setup() {
        fakeRepository = FakeGradesRepository()
        useCase = DeleteGradeUseCaseImpl(fakeRepository)

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
    fun `test delete existing grade`() = runTest {
        // Given existing grade with UUID "uuid1"

        // When
        val result = useCase("uuid1")

        // Then
        assertTrue(result.isSome())

        // Verify repository state
        fakeRepository.getGradeById("uuid1").test {
            val grade = awaitItem()
            assertEquals(null, grade, "Grade should no longer exist in repository")
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `test delete non-existing grade`() = runTest {
        // Given: no grade with UUID "non-existent"

        // When
        val result = useCase("non-existent")

        // Then
        assertTrue(result.isNone())

        // Verify repository state is unchanged
        fakeRepository.getGradeById("uuid1").test {
            val grade = awaitItem()
            assertTrue(grade != null, "Original grade should still exist")
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `test delete when repository fails`() = runTest {
        // Given
        fakeRepository.shouldFail = true

        // When
        val result = useCase("uuid1")

        // Then
        assertTrue(result.isNone())

        // Verify repository state is unchanged
        fakeRepository.getGradeById("uuid1").test {
            val grade = awaitItem()
            assertTrue(grade != null, "Original grade should still exist")
            cancelAndConsumeRemainingEvents()
        }
    }
}
