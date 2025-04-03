package de.felixlf.gradingscale2.entities.usecases

import arrow.core.Option
import arrow.core.raise.option
import app.cash.turbine.test
import de.felixlf.gradingscale2.entities.models.Grade
import de.felixlf.gradingscale2.entities.models.GradeScale
import de.felixlf.gradingscale2.entities.models.remote.GradeDTO
import de.felixlf.gradingscale2.entities.models.remote.GradeScaleDTO
import de.felixlf.gradingscale2.entities.repositories.FakeGradeScaleRepository
import de.felixlf.gradingscale2.entities.repositories.GradeScaleRepository
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ImportRemoteGradeScaleIntoDbUseCaseTest {
    
    private lateinit var fakeRepository: FakeGradeScaleRepository
    private lateinit var useCase: ImportRemoteGradeScaleIntoDbUseCaseImpl
    
    @BeforeTest
    fun setup() {
        fakeRepository = FakeGradeScaleRepository()
        useCase = ImportRemoteGradeScaleIntoDbUseCaseImpl(fakeRepository)
    }
    
    @Test
    fun `test import of grade scale with unique name`() = runTest {
        // Given
        val remoteGradeScale = GradeScaleDTO(
            gradeScaleName = "TestScale",
            country = "Germany",
            grades = listOf(
                GradeDTO("A", 0.9),
                GradeDTO("B", 0.8),
                GradeDTO("C", 0.7)
            )
        )
        
        // When
        val result = useCase(remoteGradeScale)
        
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
            
            val savedScale = scales.first()
            assertEquals("TestScale", savedScale.gradeScaleName)
            assertEquals(3, savedScale.grades.size)
            
            val gradeNames = savedScale.grades.map { it.namedGrade }
            assertTrue(gradeNames.contains("A"))
            assertTrue(gradeNames.contains("B"))
            assertTrue(gradeNames.contains("C"))
            
            cancelAndConsumeRemainingEvents()
        }
    }
    
    @Test
    fun `test import of grade scale with duplicate name - adds country`() = runTest {
        // Given - Add a grade scale with name "TestScale" first
        val existingScale = GradeScale(
            id = "1",
            gradeScaleName = "TestScale",
            totalPoints = 10.0,
            grades = persistentListOf(
                Grade(
                    nameOfScale = "TestScale",
                    namedGrade = "X",
                    percentage = 0.8,
                    idOfGradeScale = "1",
                    uuid = "uuid1"
                )
            )
        )
        fakeRepository.addGradeScale(existingScale)
        
        // Create remote scale with the same name
        val remoteGradeScale = GradeScaleDTO(
            gradeScaleName = "TestScale",
            country = "France",
            grades = listOf(
                GradeDTO("A", 0.9),
                GradeDTO("B", 0.8)
            )
        )
        
        // When
        val result = useCase(remoteGradeScale)
        
        // Then
        assertTrue(result.isSome())
        
        // Verify repository state
        fakeRepository.getGradeScales().test {
            val scales = awaitItem()
            assertEquals(2, scales.size)
            
            // Find the newly added scale with modified name
            val newScale = scales.find { it.gradeScaleName == "TestScale - France" }
            assertNotNull(newScale)
            assertEquals(2, newScale.grades.size)
            
            cancelAndConsumeRemainingEvents()
        }
    }
    
    @Test
    fun `test import of grade scale with duplicate name and country - adds counter`() = runTest {
        // Given - Add two grade scales with conflicting names
        val existingScale1 = GradeScale(
            id = "1",
            gradeScaleName = "TestScale",
            totalPoints = 10.0,
            grades = persistentListOf(
                Grade(
                    nameOfScale = "TestScale",
                    namedGrade = "X",
                    percentage = 0.8,
                    idOfGradeScale = "1",
                    uuid = "uuid1"
                )
            )
        )
        fakeRepository.addGradeScale(existingScale1)
        
        val existingScale2 = GradeScale(
            id = "2",
            gradeScaleName = "TestScale - France",
            totalPoints = 10.0,
            grades = persistentListOf(
                Grade(
                    nameOfScale = "TestScale - France",
                    namedGrade = "Y",
                    percentage = 0.7,
                    idOfGradeScale = "2",
                    uuid = "uuid2"
                )
            )
        )
        fakeRepository.addGradeScale(existingScale2)
        
        // Create remote scale with the same name and country
        val remoteGradeScale = GradeScaleDTO(
            gradeScaleName = "TestScale",
            country = "France",
            grades = listOf(
                GradeDTO("A", 0.9),
                GradeDTO("B", 0.8)
            )
        )
        
        // When
        val result = useCase(remoteGradeScale)
        
        // Then
        assertTrue(result.isSome())
        
        // Verify repository state
        fakeRepository.getGradeScales().test {
            val scales = awaitItem()
            assertEquals(3, scales.size)
            
            // Find the newly added scale with numbered name
            val newScale = scales.find { it.gradeScaleName == "TestScale - France (1)" }
            assertNotNull(newScale)
            assertEquals(2, newScale.grades.size)
            
            cancelAndConsumeRemainingEvents()
        }
    }
    
    @Test
    fun `test import failure when repository fails`() = runTest {
        // Given
        fakeRepository.shouldFail = true
        val remoteGradeScale = GradeScaleDTO(
            gradeScaleName = "TestScale",
            country = "Germany",
            grades = listOf(
                GradeDTO("A", 0.9),
                GradeDTO("B", 0.8)
            )
        )
        
        // When
        val result = useCase(remoteGradeScale)
        
        // Then
        assertTrue(result.isNone())
        
        // Verify repository state (unchanged)
        fakeRepository.getGradeScales().test {
            val scales = awaitItem()
            assertEquals(0, scales.size)
            
            cancelAndConsumeRemainingEvents()
        }
    }
}
