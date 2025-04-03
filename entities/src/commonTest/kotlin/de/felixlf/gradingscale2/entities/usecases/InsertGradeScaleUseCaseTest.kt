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

class InsertGradeScaleUseCaseTest {
    
    private lateinit var fakeRepository: FakeGradeScaleRepository
    private lateinit var useCase: InsertGradeScaleUseCaseImpl
    
    @BeforeTest
    fun setup() {
        fakeRepository = FakeGradeScaleRepository()
        useCase = InsertGradeScaleUseCaseImpl(fakeRepository)
    }
    
    @Test
    fun `test insert grade scale into empty repository`() = runTest {
        // Given
        val gradeScaleName = "New Scale"
        val defaultGradeName = "C"
        
        // When
        val result = useCase(gradeScaleName, defaultGradeName)
        
        // Then
        assertTrue(result.isSome())
        result.fold(
            ifEmpty = { /* Already checked with assertTrue above */ },
            ifSome = { id: String -> assertEquals("1", id) } // First ID should be 1 since repository is empty
        )
        
        // Verify repository state
        fakeRepository.getGradeScales().test {
            val scales = awaitItem()
            assertEquals(1, scales.size)
            
            val newScale = scales.first()
            assertEquals("New Scale", newScale.gradeScaleName)
            assertEquals(1, newScale.grades.size)
            assertEquals("C", newScale.grades[0].namedGrade)
            assertEquals(0.5, newScale.grades[0].percentage)
            
            cancelAndConsumeRemainingEvents()
        }
    }
    
    @Test
    fun `test insert grade scale with existing scales in repository`() = runTest {
        // Given
        val existingScale = GradeScale(
            id = "5",
            gradeScaleName = "Existing Scale",
            totalPoints = 10.0,
            grades = persistentListOf(
                Grade(
                    nameOfScale = "Existing Scale",
                    namedGrade = "A",
                    percentage = 0.9,
                    idOfGradeScale = "5",
                    uuid = "uuid1"
                )
            )
        )
        fakeRepository.addGradeScale(existingScale)
        
        val gradeScaleName = "New Scale"
        val defaultGradeName = "C"
        
        // When
        val result = useCase(gradeScaleName, defaultGradeName)
        
        // Then
        assertTrue(result.isSome())
        result.fold(
            ifEmpty = { /* Already checked with assertTrue above */ },
            ifSome = { id: String -> assertEquals("6", id) } // Next ID should be 6 (max ID + 1)
        )
        
        // Verify repository state
        fakeRepository.getGradeScales().test {
            val scales = awaitItem()
            assertEquals(2, scales.size)
            
            val newScale = scales.find { it.gradeScaleName == "New Scale" }
            assertTrue(newScale != null)
            assertEquals("6", newScale?.id)
            assertEquals(1, newScale?.grades?.size)
            
            cancelAndConsumeRemainingEvents()
        }
    }
    
    @Test
    fun `test insert grade scale when repository fails`() = runTest {
        // Given
        fakeRepository.shouldFail = true
        val gradeScaleName = "New Scale"
        val defaultGradeName = "C"
        
        // When
        val result = useCase(gradeScaleName, defaultGradeName)
        
        // Then
        assertTrue(result.isNone())
        
        // Verify repository state is unchanged
        fakeRepository.getGradeScales().test {
            val scales = awaitItem()
            assertEquals(0, scales.size)
            cancelAndConsumeRemainingEvents()
        }
    }
}
