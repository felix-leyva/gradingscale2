package de.felixlf.gradingscale2.entities.features.calculator

import de.felixlf.gradingscale2.entities.models.Grade
import de.felixlf.gradingscale2.entities.models.GradeScale
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GradeScaleCalculatorUIStateTest {

    private val testScaleId = "test-scale-456"
    private val testScaleName = "Standard College Scale"

    private val testGrades = persistentListOf(
        Grade(namedGrade = "A", percentage = 0.90, idOfGradeScale = testScaleId, nameOfScale = testScaleName, uuid = "uuid-a"),
        Grade(namedGrade = "B", percentage = 0.80, idOfGradeScale = testScaleId, nameOfScale = testScaleName, uuid = "uuid-b"),
        Grade(namedGrade = "C", percentage = 0.70, idOfGradeScale = testScaleId, nameOfScale = testScaleName, uuid = "uuid-c"),
        Grade(namedGrade = "D", percentage = 0.60, idOfGradeScale = testScaleId, nameOfScale = testScaleName, uuid = "uuid-d"),
        Grade(namedGrade = "F", percentage = 0.00, idOfGradeScale = testScaleId, nameOfScale = testScaleName, uuid = "uuid-f"),
    )

    private val testGradeScale = GradeScale(
        id = testScaleId,
        gradeScaleName = testScaleName,
        totalPoints = 100.0,
        grades = testGrades,
    )

    private val scalesNamesWithId = persistentListOf(
        GradeScaleCalculatorUIState.GradeScaleNameWithId(gradeScaleName = testScaleName, gradeScaleId = testScaleId),
        GradeScaleCalculatorUIState.GradeScaleNameWithId(gradeScaleName = "Secondary Scale", gradeScaleId = "sec-id"),
    )

    @Test
    fun nullGradeScale_derivesEmptyDefaults() {
        val state = GradeScaleCalculatorUIState(
            selectedGradeScale = null,
            currentPercentage = null,
            totalPoints = null,
            gradeScalesNamesWithId = scalesNamesWithId,
        )

        assertFalse(state.hasSelectedGradeScale)
        assertNull(state.selectedGradeScaleId)
        assertEquals("", state.selectedGradeScaleName)
        assertFalse(state.hasGradeResult)
        assertEquals("", state.gradeName)
        assertNull(state.earnedPoints)
        assertEquals("", state.earnedPointsString)
        assertEquals(10.0, state.totalPointsVal)
        assertEquals("", state.totalPointsString)
        assertEquals(0.0, state.currentPercentageVal)
        assertEquals(0.0, state.percentage100)
        assertEquals("", state.percentageString)
        assertEquals("- %", state.percentageFormattedWithSymbol)
        assertTrue(state.availableGradeNames.isEmpty())
        assertEquals(2, state.gradeScaleItems.size)
        assertEquals(testScaleId, state.gradeScaleItems.first().id)
    }

    @Test
    fun validGradeScaleWithInputs_derivesCorrectGradeAndPoints() {
        val state = GradeScaleCalculatorUIState(
            selectedGradeScale = testGradeScale,
            currentPercentage = 0.85,
            totalPoints = 100.0,
            gradeScalesNamesWithId = scalesNamesWithId,
        )

        assertTrue(state.hasSelectedGradeScale)
        assertEquals(testScaleId, state.selectedGradeScaleId)
        assertEquals(testScaleName, state.selectedGradeScaleName)
        assertTrue(state.hasGradeResult)
        assertEquals("B", state.gradeName)
        assertEquals(85.0, state.earnedPoints)
        assertEquals("85", state.earnedPointsString)
        assertEquals(100.0, state.totalPointsVal)
        assertEquals("100", state.totalPointsString)
        assertEquals(0.85, state.currentPercentageVal)
        assertEquals(85.0, state.percentage100)
        assertEquals("85", state.percentageString)
        assertEquals(5, state.availableGradeNames.size)
        assertEquals("A", state.availableGradeNames.first())
    }

    @Test
    fun steppers_deriveCorrectlyWithinBounds() {
        val midState = GradeScaleCalculatorUIState(
            selectedGradeScale = testGradeScale,
            currentPercentage = 0.50,
            totalPoints = 100.0,
            gradeScalesNamesWithId = scalesNamesWithId,
        )
        // 50 points out of 100
        assertTrue(midState.canIncrementPoints)
        assertTrue(midState.canDecrementPoints)
        assertEquals(51.0, midState.nextHigherPoints)
        assertEquals(49.0, midState.nextLowerPoints)
        assertEquals(50.5, midState.nextHalfHigherPoints)
        assertEquals(49.5, midState.nextHalfLowerPoints)

        // At upper boundary: 100 points
        val maxState = GradeScaleCalculatorUIState(
            selectedGradeScale = testGradeScale,
            currentPercentage = 1.0,
            totalPoints = 100.0,
            gradeScalesNamesWithId = scalesNamesWithId,
        )
        assertFalse(maxState.canIncrementPoints)
        assertTrue(maxState.canDecrementPoints)
        assertEquals(100.0, maxState.nextHigherPoints) // clamped
        assertEquals(99.0, maxState.nextLowerPoints)
        assertEquals(100.0, maxState.nextHalfHigherPoints) // clamped

        // At lower boundary: 0 points
        val zeroState = GradeScaleCalculatorUIState(
            selectedGradeScale = testGradeScale,
            currentPercentage = 0.0,
            totalPoints = 100.0,
            gradeScalesNamesWithId = scalesNamesWithId,
        )
        assertTrue(zeroState.canIncrementPoints)
        assertFalse(zeroState.canDecrementPoints)
        assertEquals(1.0, zeroState.nextHigherPoints)
        assertEquals(0.0, zeroState.nextLowerPoints) // clamped
        assertEquals(0.0, zeroState.nextHalfLowerPoints) // clamped
    }
}
