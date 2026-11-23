package de.felixlf.gradingscale2.entities.models

import de.felixlf.gradingscale2.entities.features.calculator.GradeScaleCalculatorUIState
import de.felixlf.gradingscale2.entities.features.weightedgradecalculator.WeightCalculatorUIState
import de.felixlf.gradingscale2.entities.features.weightedgradecalculator.dialog.WeightedGradeDialogUIState
import de.felixlf.gradingscale2.entities.models.weightedgrade.WeightedGrade
import kotlinx.collections.immutable.persistentListOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DomainValidationTest {

    @Test
    fun gradeScaleEmptyGradesSafeLookupsAndUiStatesDoNotCrash() {
        val emptyScale = GradeScale("e1", "Empty", 10.0, persistentListOf())

        assertNull(emptyScale.gradeByPercentageOrNull(0.5))
        assertNull(emptyScale.gradeByPointsOrNull(5.0))
        assertNull(emptyScale.nameByPercentageOrNull(0.5))
        assertEquals("", emptyScale.nameByPercentage(0.5))

        val calcState = GradeScaleCalculatorUIState(
            selectedGradeScale = emptyScale,
            currentPercentage = 0.5,
            totalPoints = 10.0,
            gradeScalesNamesWithId = persistentListOf(),
        )
        assertNull(calcState.currentGrade)
        assertEquals("", calcState.gradeName)
        assertFalse(calcState.hasGradeResult)

        val weightedState = WeightCalculatorUIState(
            gradeScaleNameAndIds = persistentListOf(GradeScaleNameAndId("Empty", "e1")),
            selectedGradeScale = emptyScale,
            grades = persistentListOf(WeightedGrade(0.8, 1.0, "w1")),
            selectedGrade = null,
        )
        assertEquals("", weightedState.weightedGradeSummary?.totalGradeName)
        assertEquals("", weightedState.weightedGrades.single().name)

        val dialogState = WeightedGradeDialogUIState(
            gradeScale = emptyScale,
            percentage = 0.8,
            weight = 1.0,
        )
        assertEquals("", dialogState.gradeNameString)
    }

    @Test
    fun gradeScaleRejectsNonFiniteTotalPointsAndHandlesNaNInputs() {
        listOf(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NaN).forEach { invalidTotal ->
            assertFailsWith<IllegalArgumentException> {
                GradeScale("bad", "Bad", invalidTotal, persistentListOf())
            }
        }

        val scale = GradeScale(
            id = "s1",
            gradeScaleName = "Valid",
            totalPoints = 10.0,
            grades = persistentListOf(
                Grade(namedGrade = "A", percentage = 0.9, idOfGradeScale = "s1", nameOfScale = "Valid", uuid = "g1"),
                Grade(namedGrade = "F", percentage = 0.0, idOfGradeScale = "s1", nameOfScale = "Valid", uuid = "g2"),
            ),
        )
        assertEquals("F", scale.gradeByPercentageOrNull(Double.NaN)?.namedGrade)
        assertEquals("F", scale.gradeByPointsOrNull(Double.NaN)?.namedGrade)
        assertEquals("F", scale.gradeByPointsOrNull(Double.POSITIVE_INFINITY)?.namedGrade)
    }

    @Test
    fun weightedGradeRejectsNegativeAndNonFiniteWeightsAndPercentages() {
        listOf(-1.0, Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY).forEach { badWeight ->
            assertFailsWith<IllegalArgumentException> {
                WeightedGrade(percentage = 0.5, weight = badWeight, uuid = "w-bad")
            }
        }
        listOf(Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY).forEach { badPct ->
            assertFailsWith<IllegalArgumentException> {
                WeightedGrade(percentage = badPct, weight = 1.0, uuid = "w-bad-pct")
            }
        }

        val scale = GradeScale(
            id = "s1",
            gradeScaleName = "Valid",
            totalPoints = 10.0,
            grades = persistentListOf(
                Grade(namedGrade = "A", percentage = 0.9, idOfGradeScale = "s1", nameOfScale = "Valid", uuid = "g1"),
                Grade(namedGrade = "F", percentage = 0.0, idOfGradeScale = "s1", nameOfScale = "Valid", uuid = "g2"),
            ),
        )
        val overflowState = WeightCalculatorUIState(
            gradeScaleNameAndIds = persistentListOf(GradeScaleNameAndId("Valid", "s1")),
            selectedGradeScale = scale,
            grades = persistentListOf(
                WeightedGrade(percentage = 0.8, weight = Double.MAX_VALUE, uuid = "w1"),
                WeightedGrade(percentage = 0.6, weight = Double.MAX_VALUE, uuid = "w2"),
            ),
            selectedGrade = null,
        )
        val summary = overflowState.weightedGradeSummary!!
        assertTrue(summary.weightedPercentageDouble.isFinite())
        assertTrue(summary.weightedPercentageDouble in 0.0..1.0)
        assertTrue(summary.weightedPercentage.endsWith("%"))
    }
}
