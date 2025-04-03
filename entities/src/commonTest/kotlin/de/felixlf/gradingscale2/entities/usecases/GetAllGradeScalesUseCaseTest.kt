package de.felixlf.gradingscale2.entities.usecases

import app.cash.turbine.test
import de.felixlf.gradingscale2.entities.models.Grade
import de.felixlf.gradingscale2.entities.models.GradeScale
import de.felixlf.gradingscale2.entities.repositories.FakeGradeScaleRepository
import de.felixlf.gradingscale2.entities.util.MockGradeScalesGenerator
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetAllGradeScalesUseCaseTest {
    
    private lateinit var fakeRepository: FakeGradeScaleRepository
    private lateinit var mockGenerator: MockGradeScalesGenerator
    private lateinit var useCase: GetAllGradeScalesUseCaseImpl
    
    @BeforeTest
    fun setup() {
        fakeRepository = FakeGradeScaleRepository()
        mockGenerator = MockGradeScalesGenerator(size = 3) // Smaller size for tests
        useCase = GetAllGradeScalesUseCaseImpl(fakeRepository, mockGenerator)
    }
    
    @Test
    fun `test get all grade scales when repository has data`() = runTest {
        // Given
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
        
        // When/Then
        useCase().test {
            val scales = awaitItem()
            assertEquals(1, scales.size)
            assertEquals("Test Scale", scales.first().gradeScaleName)
            cancelAndConsumeRemainingEvents()
        }
    }
    
    @Test
    fun `test get all grade scales when repository is empty - should initialize with mock data`() = runTest {
        // Given - empty repository
        
        // When/Then
        useCase().test {
            val scales = awaitItem()
            
            // Should be populated with mock scales from the generator
            assertEquals(mockGenerator.gradeScales.size, scales.size)
            
            // Verify mock scales are present
            mockGenerator.gradeScaleNames.forEach { name ->
                assertTrue(scales.any { it.gradeScaleName == name })
            }
            
            cancelAndConsumeRemainingEvents()
        }
    }
    
    @Test
    fun `test repository state after getting all grade scales`() = runTest {
        // Given - empty repository
        
        // When
        useCase().test {
            awaitItem() // Consume first emission
            cancelAndConsumeRemainingEvents()
        }
        
        // Then - verify that the repository has been populated
        fakeRepository.getGradeScales().test {
            val scales = awaitItem()
            assertEquals(mockGenerator.gradeScales.size, scales.size)
            cancelAndConsumeRemainingEvents()
        }
    }
}
