package de.felixlf.gradingscale2.entities.usecases

import de.felixlf.gradingscale2.entities.models.Grade
import de.felixlf.gradingscale2.entities.models.GradeScale
import de.felixlf.gradingscale2.entities.repositories.FakeGradeScaleRepository
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import app.cash.turbine.test

class DeleteGradeScaleUseCaseTest {
    
    private lateinit var fakeRepository: FakeGradeScaleRepository
    private lateinit var useCase: DeleteGradeScaleUseCaseImpl
    
    @BeforeTest
    fun setup() {
        fakeRepository = FakeGradeScaleRepository()
        useCase = DeleteGradeScaleUseCaseImpl(fakeRepository)
        
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
                    uuid = "uuid1"
                )
            )
        )
        fakeRepository.addGradeScale(testGradeScale)
    }
    
    @Test
    fun `test delete existing grade scale`() = runTest {
        // Given existing grade scale with ID "1"
        
        // When
        val result = useCase("1")
        
        // Then
        assertTrue(result.isSome())
        
        // Verify repository state
        fakeRepository.getGradeScales().test {
            val scales = awaitItem()
            assertEquals(0, scales.size, "Repository should be empty after deletion")
            cancelAndConsumeRemainingEvents()
        }
    }
    
    @Test
    fun `test delete non-existing grade scale`() = runTest {
        // Given: no grade scale with ID "999"
        
        // When
        val result = useCase("999")
        
        // Then
        assertTrue(result.isNone())
        
        // Verify repository state is unchanged
        fakeRepository.getGradeScales().test {
            val scales = awaitItem()
            assertEquals(1, scales.size, "Repository should still contain the original grade scale")
            cancelAndConsumeRemainingEvents()
        }
    }
    
    @Test
    fun `test delete when repository fails`() = runTest {
        // Given
        fakeRepository.shouldFail = true
        
        // When
        val result = useCase("1")
        
        // Then
        assertTrue(result.isNone())
        
        // Verify repository state is unchanged
        fakeRepository.getGradeScales().test {
            val scales = awaitItem()
            assertEquals(1, scales.size, "Repository should still contain the original grade scale")
            cancelAndConsumeRemainingEvents()
        }
    }
}
