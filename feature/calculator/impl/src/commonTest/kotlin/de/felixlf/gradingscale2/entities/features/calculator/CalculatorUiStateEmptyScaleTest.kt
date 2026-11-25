package de.felixlf.gradingscale2.entities.features.calculator

import de.felixlf.gradingscale2.entities.models.GradeScale
import kotlinx.collections.immutable.persistentListOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull

/**
 * Split out of the former DomainValidationTest: this state lives in :feature:calculator:impl,
 * so its assertions have to live here too.
 */
class CalculatorUiStateEmptyScaleTest {

    @Test
    fun gradeScaleWithNoGradesHasNoGradeResult() {
        val emptyScale = GradeScale("e1", "Empty", 10.0, persistentListOf())

        val calcState = GradeScaleCalculatorUIState(
            selectedGradeScale = emptyScale,
            currentPercentage = 0.5,
            totalPoints = 10.0,
            gradeScalesNamesWithId = persistentListOf(),
        )

        assertNull(calcState.currentGrade)
        assertEquals("", calcState.gradeName)
        assertFalse(calcState.hasGradeResult)
    }
}
