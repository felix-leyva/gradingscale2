package de.felixlf.gradingscale2.features.list

sealed interface GradeScaleListDialogCommand {
    data class EditGradeScale(val id: String) : GradeScaleListDialogCommand

    data object AddNewGradeScale : GradeScaleListDialogCommand

    data class EditCurrentGrade(val gradeId: String) : GradeScaleListDialogCommand

    data class AddNewGradeInCurrentGradeScale(val currentGradeScaleId: String) : GradeScaleListDialogCommand
}
