package de.felixlf.gradingscale2.entities.features.weightedgradecalculator

import de.felixlf.gradingscale2.entities.models.Grade
import de.felixlf.gradingscale2.entities.models.GradeScale
import de.felixlf.gradingscale2.entities.models.GradeScaleNameAndId
import de.felixlf.gradingscale2.entities.models.weightedgrade.WeightedGrade
import kotlinx.collections.immutable.persistentListOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class WeightCalculatorUIStateTest {

    private val testGradeScaleName = "Test Grade Scale"
    private val testGradeScaleId = "test-scale-id"
    private val testGrades = persistentListOf(
        Grade(namedGrade = "A", percentage = 0.9, idOfGradeScale = testGradeScaleId, nameOfScale = testGradeScaleName, uuid = "UUID-A"),
        Grade(namedGrade = "B", percentage = 0.8, idOfGradeScale = testGradeScaleId, nameOfScale = testGradeScaleName, uuid = "UUID-B"),
        Grade(namedGrade = "C", percentage = 0.7, idOfGradeScale = testGradeScaleId, nameOfScale = testGradeScaleName, uuid = "UUID-C"),
        Grade(namedGrade = "D", percentage = 0.6, idOfGradeScale = testGradeScaleId, nameOfScale = testGradeScaleName, uuid = "UUID-D"),
        Grade(namedGrade = "F", percentage = 0.5, idOfGradeScale = testGradeScaleId, nameOfScale = testGradeScaleName, uuid = "UUID-F"),
    )
    private val testGradeScale = GradeScale(
        id = testGradeScaleId,
        gradeScaleName = testGradeScaleName,
        grades = testGrades,
        totalPoints = 10.0,
    )
    private val testGradeScaleNameAndId = GradeScaleNameAndId(
        name = testGradeScaleName,
        id = testGradeScaleId,
    )

    @Test
    fun `isLoading should return true when gradeScaleNameAndIds is empty`() {
        // Arrange
        val state = WeightCalculatorUIState(
            gradeScaleNameAndIds = persistentListOf(),
            selectedGradeScale = null,
            grades = persistentListOf(),
            selectedGrade = null,
        )

        // Assert
        assertTrue(state.isLoading)
    }

    @Test
    fun `isLoading should return false when gradeScaleNameAndIds is not empty`() {
        // Arrange
        val state = WeightCalculatorUIState(
            gradeScaleNameAndIds = persistentListOf(testGradeScaleNameAndId),
            selectedGradeScale = null,
            grades = persistentListOf(),
            selectedGrade = null,
        )

        // Assert
        assertFalse(state.isLoading)
    }

    @Test
    fun `weightedGradeSummary should be null when no grade scale is selected`() {
        // Arrange
        val state = WeightCalculatorUIState(
            gradeScaleNameAndIds = persistentListOf(testGradeScaleNameAndId),
            selectedGradeScale = null,
            grades = persistentListOf(
                WeightedGrade(percentage = 0.85, weight = 2.0, uuid = "UUID1"),
            ),
            selectedGrade = null,
        )

        // Assert
        assertNull(state.weightedGradeSummary)
    }

    @Test
    fun `weightedGradeSummary should be null when grades list is empty`() {
        // Arrange
        val state = WeightCalculatorUIState(
            gradeScaleNameAndIds = persistentListOf(testGradeScaleNameAndId),
            selectedGradeScale = testGradeScale,
            grades = persistentListOf(),
            selectedGrade = null,
        )

        // Assert
        assertNull(state.weightedGradeSummary)
    }

    @Test
    fun `weightedGradeSummary should calculate correct values for a single grade`() {
        // Arrange
        val grade = WeightedGrade(percentage = 0.85, weight = 2.0, uuid = "UUID1")
        val state = WeightCalculatorUIState(
            gradeScaleNameAndIds = persistentListOf(testGradeScaleNameAndId),
            selectedGradeScale = testGradeScale,
            grades = persistentListOf(grade),
            selectedGrade = null,
        )

        // Assert
        assertNotNull(state.weightedGradeSummary)
        with(state.weightedGradeSummary!!) {
            assertEquals("B", totalGradeName) // 85% should be B
            assertEquals("85 %", weightedPercentage)
            assertEquals("1.7", earnedPoints) // 0.85 * 2.0
            assertEquals("2", totalPoints)
        }
    }

    @Test
    fun `weightedGradeSummary should calculate correct values for multiple grades`() {
        // Arrange
        val grades = persistentListOf(
            WeightedGrade(percentage = 0.9, weight = 1.0, uuid = "UUID1"), // 0.9 points
            WeightedGrade(percentage = 0.8, weight = 2.0, uuid = "UUID2"), // 1.6 points
            WeightedGrade(percentage = 0.7, weight = 3.0, uuid = "UUID3"), // 2.1 points
        )
        val state = WeightCalculatorUIState(
            gradeScaleNameAndIds = persistentListOf(testGradeScaleNameAndId),
            selectedGradeScale = testGradeScale,
            grades = grades,
            selectedGrade = null,
        )

        // Total points = 0.9 + 1.6 + 2.1 = 4.6
        // Total weight = 1.0 + 2.0 + 3.0 = 6.0
        // Weighted percentage = 4.6 / 6.0 = 0.766... which should round to 76.7%
        // 76.7% falls between C (70%) and B (80%), so should be C

        // Assert
        assertNotNull(state.weightedGradeSummary)
        with(state.weightedGradeSummary!!) {
            assertEquals("C", totalGradeName) 
            assertEquals("76.67 %", weightedPercentage)
            assertEquals("4.6", earnedPoints)
            assertEquals("6", totalPoints)
        }
    }

    @Test
    fun `weightedGrades should be empty when no grade scale is selected`() {
        // Arrange
        val state = WeightCalculatorUIState(
            gradeScaleNameAndIds = persistentListOf(testGradeScaleNameAndId),
            selectedGradeScale = null,
            grades = persistentListOf(
                WeightedGrade(percentage = 0.85, weight = 2.0, uuid = "UUID1"),
            ),
            selectedGrade = null,
        )

        // Assert
        assertTrue(state.weightedGrades.isEmpty())
    }

    @Test
    fun `weightedGrades should be empty when grades list is empty`() {
        // Arrange
        val state = WeightCalculatorUIState(
            gradeScaleNameAndIds = persistentListOf(testGradeScaleNameAndId),
            selectedGradeScale = testGradeScale,
            grades = persistentListOf(),
            selectedGrade = null,
        )

        // Assert
        assertTrue(state.weightedGrades.isEmpty())
    }

    @Test
    fun `weightedGrades should map grades to named grades correctly`() {
        // Arrange
        val grades = persistentListOf(
            WeightedGrade(percentage = 0.95, weight = 1.0, uuid = "UUID1"),
            WeightedGrade(percentage = 0.85, weight = 2.0, uuid = "UUID2"),
            WeightedGrade(percentage = 0.75, weight = 3.0, uuid = "UUID3"),
            WeightedGrade(percentage = 0.65, weight = 4.0, uuid = "UUID4"),
            WeightedGrade(percentage = 0.55, weight = 5.0, uuid = "UUID5"),
        )
        val state = WeightCalculatorUIState(
            gradeScaleNameAndIds = persistentListOf(testGradeScaleNameAndId),
            selectedGradeScale = testGradeScale,
            grades = grades,
            selectedGrade = null,
        )

        // Assert
        assertEquals(5, state.weightedGrades.size)
        
        // Check each grade was mapped to the correct named grade
        val expectedNames = listOf("A", "B", "C", "D", "F")
        for (i in grades.indices) {
            val weightedGradeWithName = state.weightedGrades[i]
            assertEquals(grades[i], weightedGradeWithName.grade)
            assertEquals(expectedNames[i], weightedGradeWithName.name)
            assertEquals(grades[i].percentage, weightedGradeWithName.percentage)
            assertEquals(grades[i].percentage * grades[i].weight, weightedGradeWithName.relativeWeight)
        }
    }

    @Test
    fun `weightedGrades should handle edge case percentages correctly`() {
        // Arrange
        val grades = persistentListOf(
            WeightedGrade(percentage = 1.0, weight = 1.0, uuid = "UUID1"), // 100% - should be A
            WeightedGrade(percentage = 0.0, weight = 2.0, uuid = "UUID2"), // 0% - should be F (lowest grade)
        )
        val state = WeightCalculatorUIState(
            gradeScaleNameAndIds = persistentListOf(testGradeScaleNameAndId),
            selectedGradeScale = testGradeScale,
            grades = grades,
            selectedGrade = null,
        )

        // Assert
        assertEquals(2, state.weightedGrades.size)
        assertEquals("A", state.weightedGrades[0].name) // 100% is A
        assertEquals("F", state.weightedGrades[1].name) // 0% defaults to F
    }
}
