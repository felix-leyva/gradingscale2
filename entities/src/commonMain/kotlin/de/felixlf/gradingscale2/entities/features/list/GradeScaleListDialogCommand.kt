package de.felixlf.gradingscale2.entities.features.list

import gradingscale2.entities.generated.resources.Res
import gradingscale2.entities.generated.resources.gradescale_list_menu_add_new_grade
import gradingscale2.entities.generated.resources.gradescale_list_menu_add_new_grade_scale
import gradingscale2.entities.generated.resources.gradescale_list_menu_edit_grade_scale
import gradingscale2.entities.generated.resources.gradescale_list_menu_help
import org.jetbrains.compose.resources.StringResource

sealed class GradeScaleListDialogCommand(val menuText: StringResource? = null) {
    data class EditGradeScale(val gradeScaleId: String) : GradeScaleListDialogCommand(Res.string.gradescale_list_menu_edit_grade_scale)

    data object AddNewGradeScale : GradeScaleListDialogCommand(Res.string.gradescale_list_menu_add_new_grade_scale)

    data class EditCurrentGrade(val gradeId: String) : GradeScaleListDialogCommand()

    data class AddNewGradeInCurrentGradeScale(val gradeScaleId: String) :
        GradeScaleListDialogCommand(Res.string.gradescale_list_menu_add_new_grade)

    data object Help : GradeScaleListDialogCommand(Res.string.gradescale_list_menu_help)
}
