package de.felixlf.gradingscale2.entities.features.list.upsertgradedialog

import de.felixlf.gradingscale2.entities.models.Grade
import de.felixlf.gradingscale2.entities.models.GradeScale
import kotlinx.collections.immutable.persistentListOf
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class UpsertGradeUIStateTest {

    @Test
    fun isSaveButtonEnabled_withValidData_returnsTrue() {
        val grade = Grade(namedGrade = "A", percentage = 0.9, nameOfScale = "Scale1", uuid = "1234")
        val gradeScale = GradeScale(id = "1", gradeScaleName = "Scale1", totalPoints = 10.0, persistentListOf(grade))
        val state = UpsertGradeUIState(
            name = "A",
            percentage = "90",
            grade = grade,
            gradeScale = gradeScale,
            selectedGrade = grade,
        )
        assertTrue(state.isSaveButtonEnabled)
    }

    @Test
    fun isSaveButtonEnabled_withInvalidPercentage_returnsFalse() {
        val grade = Grade(namedGrade = "A", percentage = 0.9, nameOfScale = "Scale1", uuid = "1234")
        val gradeScale = GradeScale(id = "1", gradeScaleName = "Scale1", totalPoints = 10.0, persistentListOf(grade))
        val state = UpsertGradeUIState(
            name = "A",
            percentage = "110",
            grade = grade,
            gradeScale = gradeScale,
            selectedGrade = grade,
        )
        assertFalse(state.isSaveButtonEnabled)
    }

    @Test
    fun isSaveButtonEnabled_withDuplicateName_returnsFalse() {
        val grade = Grade(namedGrade = "A", percentage = 0.9, nameOfScale = "Scale1", uuid = "1234")
        val gradeScale = GradeScale(id = "1", gradeScaleName = "Scale1", totalPoints = 10.0, persistentListOf(grade))
        val state = UpsertGradeUIState(
            name = "A",
            percentage = "90",
            grade = grade,
            gradeScale = gradeScale,
            selectedGrade = null,
        )
        assertFalse(state.isSaveButtonEnabled)
    }

    @Test
    fun isSaveNewButtonEnabled_withValidData_returnsTrue() {
        val grade = Grade(namedGrade = "A", percentage = 0.9, nameOfScale = "Scale1", uuid = "1234")
        val gradeScale = GradeScale(id = "1", gradeScaleName = "Scale1", totalPoints = 10.0, persistentListOf(grade))
        val state = UpsertGradeUIState(
            name = "B",
            percentage = "80",
            grade = grade,
            gradeScale = gradeScale,
            selectedGrade = null,
        )
        assertTrue(state.isSaveNewButtonEnabled)
    }

    @Test
    fun isSaveNewButtonEnabled_withDuplicatePercentage_returnsFalse() {
        val grade = Grade(namedGrade = "A", percentage = 0.9, nameOfScale = "Scale1", uuid = "1234")
        val gradeScale = GradeScale(id = "1", gradeScaleName = "Scale1", totalPoints = 10.0, persistentListOf(grade))
        val state = UpsertGradeUIState(
            name = "B",
            percentage = "90",
            grade = grade,
            gradeScale = gradeScale,
            selectedGrade = null,
        )
        assertFalse(state.isSaveNewButtonEnabled)
    }

    @Test
    fun error_withInvalidPercentage_returnsInvalidPercentageError() {
        val grade = Grade(namedGrade = "A", percentage = 0.9, nameOfScale = "Scale1", uuid = "1234")
        val gradeScale = GradeScale(id = "1", gradeScaleName = "Scale1", totalPoints = 10.0, persistentListOf(grade))
        val state = UpsertGradeUIState(
            name = "A",
            percentage = "110",
            grade = grade,
            gradeScale = gradeScale,
            selectedGrade = grade,
        )
        assertTrue(state.error.contains(UpsertGradeUIState.Error.INVALID_PERCENTAGE))
    }

    @Test
    fun error_withDuplicateName_returnsDuplicateNameError() {
        val grade = Grade(namedGrade = "A", percentage = 0.9, nameOfScale = "Scale1", uuid = "1234")
        val gradeScale = GradeScale(id = "1", gradeScaleName = "Scale1", totalPoints = 10.0, persistentListOf(grade))
        val state = UpsertGradeUIState(
            name = "A",
            percentage = "90",
            grade = grade,
            gradeScale = gradeScale,
            selectedGrade = null,
        )
        assertTrue(state.error.contains(UpsertGradeUIState.Error.DUPLICATE_NAME))
    }
}
