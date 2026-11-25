package de.felixlf.gradingscale2.entities.features.weightedgradecalculator

import de.felixlf.gradingscale2.entities.features.weightedgradecalculator.dialog.WeightedGradeDialogUIState
import de.felixlf.gradingscale2.entities.models.Grade
import de.felixlf.gradingscale2.entities.models.GradeScale
import de.felixlf.gradingscale2.entities.models.GradeScaleNameAndId
import de.felixlf.gradingscale2.entities.models.weightedgrade.WeightedGrade
import kotlinx.collections.immutable.persistentListOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Split out of the former DomainValidationTest: these states live in
 * :feature:weightedcalculator:impl, so their assertions have to live here too.
 */
class WeightedUiStateEmptyScaleTest {

    private val emptyScale = GradeScale("e1", "Empty", 10.0, persistentListOf())

    @Test
    fun gradeScaleWithNoGradesProducesBlankNames() {
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
    fun overflowingWeightsStayFiniteAndInRange() {
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
