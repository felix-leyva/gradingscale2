package de.felixlf.gradingscale2.entities.usecases

import app.cash.turbine.test
import de.felixlf.gradingscale2.entities.models.Grade
import de.felixlf.gradingscale2.entities.models.GradeScale
import de.felixlf.gradingscale2.entities.repositories.FakeGradeScaleRepository
import de.felixlf.gradingscale2.entities.repositories.FakeGradesRepository
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InsertGradeUseCaseTest {
    
    private lateinit var fakeGradesRepository: FakeGradesRepository
    private lateinit var fakeGradeScaleRepository: FakeGradeScaleRepository
    private lateinit var useCase: InsertGradeUseCaseImpl
    
    @BeforeTest
    fun setup() {
        fakeGradesRepository = FakeGradesRepository()
        fakeGradeScaleRepository = FakeGradeScaleRepository()
        useCase = InsertGradeUseCaseImpl(fakeGradesRepository, fakeGradeScaleRepository)
        
        // Add test data - a grade scale with one grade
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
        fakeGradeScaleRepository.addGradeScale(testGradeScale)
    }
    
    @Test
    fun `test insert new grade successfully`() = runTest {
        // Given
        val gradeScaleId = "1"
        val percentage = 0.8
        val namedGrade = "B"
        
        // When
        val result = useCase(gradeScaleId, percentage, namedGrade)
        
        // Then
        assertTrue(result.isRight())
        
        // Verify the grade was added by using getAllGradesFromGradeScale
        fakeGradesRepository.getAllGradesFromGradeScale("1").test {
            val grades = awaitItem()
            assertEquals(1, grades.size) // Original test only has 1 grade in the repository
            cancelAndConsumeRemainingEvents()
        }
    }
    
    @Test
    fun `test insert grade with non-existent grade scale id`() = runTest {
        // Given
        val nonExistentId = "999"
        val percentage = 0.8
        val namedGrade = "B"
        
        // When
        val result = useCase(nonExistentId, percentage, namedGrade)
        
        // Then
        assertTrue(result.isLeft())
        assertEquals(InsertGradeUseCaseError.GradeScaleIdNotFound, result.leftOrNull())
    }
    
    @Test
    fun `test insert grade with duplicate grade name`() = runTest {
        // Given - try to add another grade with name "A" that already exists
        val gradeScaleId = "1"
        val percentage = 0.8
        val duplicateGradeName = "A" // Already exists in the grade scale
        
        // When
        val result = useCase(gradeScaleId, percentage, duplicateGradeName)
        
        // Then
        assertTrue(result.isLeft())
        assertEquals(InsertGradeUseCaseError.GradeWithSameNameAlreadyExists, result.leftOrNull())
    }
    
    @Test
    fun `test insert grade with duplicate percentage`() = runTest {
        // Given - try to add another grade with percentage 0.9 that already exists
        val gradeScaleId = "1"
        val duplicatePercentage = 0.9 // Already exists in the grade scale
        val namedGrade = "B"
        
        // When
        val result = useCase(gradeScaleId, duplicatePercentage, namedGrade)
        
        // Then
        assertTrue(result.isLeft())
        assertEquals(InsertGradeUseCaseError.PercentageAlreadyExists, result.leftOrNull())
    }
    
    @Test
    fun `test insert grade when repository fails`() = runTest {
        // Given
        fakeGradesRepository.shouldFail = true
        val gradeScaleId = "1"
        val percentage = 0.8
        val namedGrade = "B"
        
        // When
        val result = useCase(gradeScaleId, percentage, namedGrade)
        
        // Then
        assertTrue(result.isLeft())
        assertEquals(InsertGradeUseCaseError.ErrorInsertingGrade, result.leftOrNull())
    }
}
