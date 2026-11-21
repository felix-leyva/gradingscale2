package de.felixlf.gradingscale2.entities.features.list.upsertgradedialog

import de.felixlf.gradingscale2.entities.models.Grade
import de.felixlf.gradingscale2.entities.models.GradeScale
import kotlinx.collections.immutable.persistentListOf
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class UpsertGradeUIStateTest {

    private val sampleGrade = Grade(namedGrade = "A", percentage = 0.9, nameOfScale = "Scale1", uuid = "1234")
    private val otherGrade = Grade(namedGrade = "B", percentage = 0.8, nameOfScale = "Scale1", uuid = "5678")
    private val sampleScale = GradeScale(
        id = "1",
        gradeScaleName = "Scale1",
        totalPoints = 10.0,
        grades = persistentListOf(sampleGrade, otherGrade),
    )

    @Test
    fun isSaveButtonEnabled_withValidData_returnsTrue() {
        val state = UpsertGradeUIState(
            name = "A",
            percentage = "90",
            grade = sampleGrade,
            gradeScale = sampleScale,
            selectedGrade = sampleGrade,
        )
        assertTrue(state.isSaveButtonEnabled)
        assertTrue(state.error.isEmpty())
        assertFalse(state.isLoading)
    }

    @Test
    fun isSaveButtonEnabled_whenGradeIsNull_returnsFalse() {
        val state = UpsertGradeUIState(
            name = "C",
            percentage = "70",
            grade = null,
            gradeScale = sampleScale,
            selectedGrade = null,
        )
        assertFalse(state.isSaveButtonEnabled)
        assertTrue(state.isSaveNewButtonEnabled)
    }

    @Test
    fun isSaveButtonEnabled_withInvalidPercentage_returnsFalse() {
        val state = UpsertGradeUIState(
            name = "A",
            percentage = "110",
            grade = sampleGrade,
            gradeScale = sampleScale,
            selectedGrade = sampleGrade,
        )
        assertFalse(state.isSaveButtonEnabled)
        assertTrue(state.error.contains(UpsertGradeUIState.Error.INVALID_PERCENTAGE))
    }

    @Test
    fun isSaveButtonEnabled_withDuplicateName_returnsFalse() {
        val state = UpsertGradeUIState(
            name = "A",
            percentage = "70",
            grade = otherGrade,
            gradeScale = sampleScale,
            selectedGrade = otherGrade, // editing B, but typing A
        )
        assertFalse(state.isSaveButtonEnabled)
        assertTrue(state.error.contains(UpsertGradeUIState.Error.DUPLICATE_NAME))
    }

    @Test
    fun isSaveButtonEnabled_withBlankOrNullNameOrPercentage_returnsFalse() {
        val stateBlankName = UpsertGradeUIState(
            name = "  ",
            percentage = "90",
            grade = sampleGrade,
            gradeScale = sampleScale,
            selectedGrade = sampleGrade,
        )
        assertFalse(stateBlankName.isSaveButtonEnabled)
        assertTrue(stateBlankName.error.contains(UpsertGradeUIState.Error.INVALID_NAME))

        val stateNullName = UpsertGradeUIState(
            name = null,
            percentage = "90",
            grade = sampleGrade,
            gradeScale = sampleScale,
            selectedGrade = sampleGrade,
        )
        assertFalse(stateNullName.isSaveButtonEnabled)

        val stateBlankPercentage = UpsertGradeUIState(
            name = "A",
            percentage = "",
            grade = sampleGrade,
            gradeScale = sampleScale,
            selectedGrade = sampleGrade,
        )
        assertFalse(stateBlankPercentage.isSaveButtonEnabled)

        val stateNullPercentage = UpsertGradeUIState(
            name = "A",
            percentage = null,
            grade = sampleGrade,
            gradeScale = sampleScale,
            selectedGrade = sampleGrade,
        )
        assertFalse(stateNullPercentage.isSaveButtonEnabled)
    }

    @Test
    fun isSaveNewButtonEnabled_withValidData_returnsTrue() {
        val state = UpsertGradeUIState(
            name = "C",
            percentage = "70",
            grade = sampleGrade,
            gradeScale = sampleScale,
            selectedGrade = null,
        )
        assertTrue(state.isSaveNewButtonEnabled)
    }

    @Test
    fun isSaveNewButtonEnabled_withDuplicateNameOrPercentage_returnsFalse() {
        val stateDupName = UpsertGradeUIState(
            name = "A",
            percentage = "70",
            grade = null,
            gradeScale = sampleScale,
            selectedGrade = null,
        )
        assertFalse(stateDupName.isSaveNewButtonEnabled)

        val stateDupPercentage = UpsertGradeUIState(
            name = "C",
            percentage = "90",
            grade = null,
            gradeScale = sampleScale,
            selectedGrade = null,
        )
        assertFalse(stateDupPercentage.isSaveNewButtonEnabled)

        val stateBlankName = UpsertGradeUIState(
            name = "   ",
            percentage = "70",
            grade = null,
            gradeScale = sampleScale,
            selectedGrade = null,
        )
        assertFalse(stateBlankName.isSaveNewButtonEnabled)

        val stateBlankPercentage = UpsertGradeUIState(
            name = "C",
            percentage = "",
            grade = null,
            gradeScale = sampleScale,
            selectedGrade = null,
        )
        assertFalse(stateBlankPercentage.isSaveNewButtonEnabled)
    }

    @Test
    fun percentageBoundaries_validateZeroAndHundredPercent() {
        // Exactly 0% -> valid
        val stateZero = UpsertGradeUIState(
            name = "Zero",
            percentage = "0",
            grade = sampleGrade,
            gradeScale = sampleScale,
            selectedGrade = sampleGrade,
        )
        assertFalse(stateZero.error.contains(UpsertGradeUIState.Error.INVALID_PERCENTAGE))

        // Exactly 100% -> valid
        val stateHundred = UpsertGradeUIState(
            name = "Max",
            percentage = "100",
            grade = sampleGrade,
            gradeScale = sampleScale,
            selectedGrade = sampleGrade,
        )
        assertFalse(stateHundred.error.contains(UpsertGradeUIState.Error.INVALID_PERCENTAGE))

        // Below 0% -> invalid
        val stateBelowZero = UpsertGradeUIState(
            name = "Neg",
            percentage = "-0.1",
            grade = sampleGrade,
            gradeScale = sampleScale,
            selectedGrade = sampleGrade,
        )
        assertTrue(stateBelowZero.error.contains(UpsertGradeUIState.Error.INVALID_PERCENTAGE))

        // Above 100% -> invalid
        val stateAboveHundred = UpsertGradeUIState(
            name = "Above",
            percentage = "100.1",
            grade = sampleGrade,
            gradeScale = sampleScale,
            selectedGrade = sampleGrade,
        )
        assertTrue(stateAboveHundred.error.contains(UpsertGradeUIState.Error.INVALID_PERCENTAGE))
    }

    @Test
    fun editingSelectedGrade_retainingOwnValues_doesNotTriggerDuplicateErrors() {
        val state = UpsertGradeUIState(
            name = "A",
            percentage = "90",
            grade = sampleGrade,
            gradeScale = sampleScale,
            selectedGrade = sampleGrade,
        )
        assertFalse(state.error.contains(UpsertGradeUIState.Error.DUPLICATE_NAME))
        assertFalse(state.error.contains(UpsertGradeUIState.Error.DUPLICATE_PERCENTAGE))
    }

    @Test
    fun editingSelectedGrade_withOtherGradeDuplicatePercentage_triggersDuplicatePercentage() {
        val state = UpsertGradeUIState(
            name = "A",
            percentage = "80", // matches otherGrade percentage (0.8)
            grade = sampleGrade,
            gradeScale = sampleScale,
            selectedGrade = sampleGrade,
        )
        assertTrue(state.error.contains(UpsertGradeUIState.Error.DUPLICATE_PERCENTAGE))
        assertFalse(state.isSaveButtonEnabled)
    }

    @Test
    fun isLoading_onlyTrueWhenBothGradeAndGradeScaleAreNull() {
        val stateBothNull = UpsertGradeUIState(
            name = "A",
            percentage = "90",
            grade = null,
            gradeScale = null,
            selectedGrade = null,
        )
        assertTrue(stateBothNull.isLoading)
        assertFalse(stateBothNull.isSaveButtonEnabled)
        assertFalse(stateBothNull.isSaveNewButtonEnabled)

        val stateGradeOnly = UpsertGradeUIState(
            name = "A",
            percentage = "90",
            grade = sampleGrade,
            gradeScale = null,
            selectedGrade = null,
        )
        assertFalse(stateGradeOnly.isLoading)

        val stateScaleOnly = UpsertGradeUIState(
            name = "A",
            percentage = "90",
            grade = null,
            gradeScale = sampleScale,
            selectedGrade = null,
        )
        assertFalse(stateScaleOnly.isLoading)
    }
}
