package de.felixlf.gradingscale2.entities.models

import de.felixlf.gradingscale2.entities.models.weightedgrade.WeightedGrade
import kotlinx.collections.immutable.persistentListOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

/**
 * Domain-level validation only. The UI-state assertions that used to live here moved
 * next to the states they exercise (feature calculator / weightedcalculator), because
 * after the module split :core:usecases:impl cannot see feature UI state.
 */
class DomainValidationTest {

    @Test
    fun gradeScaleEmptyGradesSafeLookups() {
        val emptyScale = GradeScale("e1", "Empty", 10.0, persistentListOf())

        assertNull(emptyScale.gradeByPercentageOrNull(0.5))
        assertNull(emptyScale.gradeByPointsOrNull(5.0))
        assertNull(emptyScale.nameByPercentageOrNull(0.5))
        assertEquals("", emptyScale.nameByPercentage(0.5))
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
    }
}
