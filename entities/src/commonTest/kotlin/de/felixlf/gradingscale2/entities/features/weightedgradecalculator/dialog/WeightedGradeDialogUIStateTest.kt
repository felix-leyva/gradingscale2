package de.felixlf.gradingscale2.entities.features.weightedgradecalculator.dialog

import de.felixlf.gradingscale2.entities.models.Grade
import de.felixlf.gradingscale2.entities.models.GradeScale
import kotlinx.collections.immutable.persistentListOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedGradeDialogUIStateTest {

    // Test data
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

    @Test
    fun initialStateWithNullValues() {
        // Arrange
        val uiState = WeightedGradeDialogUIState(
            gradeScale = null,
            percentage = null,
            weight = null,
        )

        // Assert
        assertTrue(uiState.grades.isEmpty())
        assertEquals("", uiState.gradeNameString)
        assertEquals("", uiState.percentageString)
        assertEquals("", uiState.weightString)
        assertEquals("", uiState.relativeWeightString)
    }

    @Test
    fun initialStateWithGradeScaleOnly() {
        // Arrange
        val uiState = WeightedGradeDialogUIState(
            gradeScale = testGradeScale,
            percentage = null,
            weight = null,
        )

        // Assert
        assertEquals(testGrades, uiState.grades)
        assertEquals("", uiState.gradeNameString)
        assertEquals("", uiState.percentageString)
        assertEquals("", uiState.weightString)
        assertEquals("", uiState.relativeWeightString)
    }

    @Test
    fun initialStateWithPercentageOnly() {
        // Arrange
        val uiState = WeightedGradeDialogUIState(
            gradeScale = null,
            percentage = 0.85,
            weight = null,
        )

        // Assert
        assertTrue(uiState.grades.isEmpty())
        assertEquals("", uiState.gradeNameString) // No grade scale to determine grade name
        assertEquals("85", uiState.percentageString) // 85% formatted
        assertEquals("", uiState.weightString)
        assertEquals("", uiState.relativeWeightString) // Need both percentage and weight
    }

    @Test
    fun initialStateWithWeightOnly() {
        // Arrange
        val uiState = WeightedGradeDialogUIState(
            gradeScale = null,
            percentage = null,
            weight = 2.5,
        )

        // Assert
        assertTrue(uiState.grades.isEmpty())
        assertEquals("", uiState.gradeNameString)
        assertEquals("", uiState.percentageString)
        assertEquals("2.5", uiState.weightString)
        assertEquals("", uiState.relativeWeightString) // Need both percentage and weight
    }

    @Test
    fun initialStateWithAllValues() {
        // Arrange
        val uiState = WeightedGradeDialogUIState(
            gradeScale = testGradeScale,
            percentage = 0.85,
            weight = 2.5,
        )

        // Assert
        assertEquals(testGrades, uiState.grades)
        assertEquals("B", uiState.gradeNameString) // 85% is a B in our test scale
        assertEquals("85", uiState.percentageString) // 85%
        assertEquals("2.5", uiState.weightString)
        assertEquals("2.13", uiState.relativeWeightString) // 0.85 * 2.5 = 2.125, rounded to 2.13
    }

    @Test
    fun percentageAtGradeBoundaryValues() {
        // Test each grade boundary
        assertGradeNameForPercentage(0.9, "A")
        assertGradeNameForPercentage(0.85, "B") // Between A and B, should be B
        assertGradeNameForPercentage(0.8, "B")
        assertGradeNameForPercentage(0.75, "C") // Between B and C, should be C
        assertGradeNameForPercentage(0.7, "C")
        assertGradeNameForPercentage(0.65, "D") // Between C and D, should be D
        assertGradeNameForPercentage(0.6, "D")
        assertGradeNameForPercentage(0.55, "F") // Between D and F, should be F
        assertGradeNameForPercentage(0.5, "F")
        assertGradeNameForPercentage(0.45, "F") // Below F, should still be F
    }

    @Test
    fun percentageFormattingTests() {
        // Test different percentage values
        assertPercentageFormat(0.0, "0")
        assertPercentageFormat(0.5, "50")
        assertPercentageFormat(0.75, "75")
        assertPercentageFormat(0.999, "99.9")
        assertPercentageFormat(1.0, "100")
    }

    @Test
    fun weightFormattingTests() {
        // Test different weight values
        assertWeightFormat(0.0, "0")
        assertWeightFormat(1.0, "1")
        assertWeightFormat(1.5, "1.5")
        assertWeightFormat(2.0, "2")
        assertWeightFormat(2.25, "2.25")
        assertWeightFormat(10.0, "10")
    }

    @Test
    fun relativeWeightCalculationTests() {
        // Test relative weight calculations
        assertRelativeWeight(0.8, 2.0, "1.6") // 80% of 2.0
        assertRelativeWeight(0.9, 3.0, "2.7") // 90% of 3.0
        assertRelativeWeight(0.75, 4.0, "3") // 75% of 4.0
        assertRelativeWeight(0.5, 5.0, "2.5") // 50% of 5.0
    }

    @Test
    fun roundingTests() {
        // Test rounding behavior
        assertPercentageFormat(0.333, "33.3") // Should round to 1 decimal place
        assertWeightFormat(1.666, "1.67") // Should round to 2 decimal places
        assertRelativeWeight(0.333, 1.0, "0.33") // Should round properly
    }

    @Test
    fun edgeCaseTests() {
        // Test values at or beyond the expected ranges
        assertPercentageFormat(0.0, "0")
        assertPercentageFormat(1.0, "100")

        // Values outside 0-1 range should be handled appropriately
        val state = WeightedGradeDialogUIState(
            gradeScale = testGradeScale,
            percentage = 1.5, // Above 1.0
            weight = 2.0,
        )
        assertEquals("A", state.gradeNameString) // Should use the highest grade (coerced to 1.0)
        assertEquals("150", state.percentageString) // Shows actual value

        // Negative weights
        val negativeWeightState = WeightedGradeDialogUIState(
            gradeScale = testGradeScale,
            percentage = 0.8,
            weight = -1.0,
        )
        assertEquals("-1", negativeWeightState.weightString)
        assertEquals("-0.8", negativeWeightState.relativeWeightString)
    }

    // Helper methods for testing
    private fun assertGradeNameForPercentage(percentage: Double, expectedGradeName: String) {
        val uiState = WeightedGradeDialogUIState(
            gradeScale = testGradeScale,
            percentage = percentage,
            weight = null,
        )
        assertEquals(
            expectedGradeName,
            uiState.gradeNameString,
            "Expected grade name for $percentage to be $expectedGradeName, but was ${uiState.gradeNameString}",
        )
    }

    private fun assertPercentageFormat(percentage: Double, expectedString: String) {
        val uiState = WeightedGradeDialogUIState(
            gradeScale = null,
            percentage = percentage,
            weight = null,
        )
        assertEquals(
            expectedString,
            uiState.percentageString,
            "Expected percentage format for $percentage to be $expectedString, but was ${uiState.percentageString}",
        )
    }

    private fun assertWeightFormat(weight: Double, expectedString: String) {
        val uiState = WeightedGradeDialogUIState(
            gradeScale = null,
            percentage = null,
            weight = weight,
        )
        assertEquals(
            expectedString,
            uiState.weightString,
            "Expected weight format for $weight to be $expectedString, but was ${uiState.weightString}",
        )
    }

    private fun assertRelativeWeight(percentage: Double, weight: Double, expectedString: String) {
        val uiState = WeightedGradeDialogUIState(
            gradeScale = null,
            percentage = percentage,
            weight = weight,
        )
        assertEquals(
            expectedString,
            uiState.relativeWeightString,
            "Expected relative weight for $percentage × $weight to be $expectedString, but was ${uiState.relativeWeightString}",
        )
    }
}
