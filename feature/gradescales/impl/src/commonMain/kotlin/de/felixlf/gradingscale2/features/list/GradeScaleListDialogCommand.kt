package de.felixlf.gradingscale2.features.list

import de.felixlf.gradingscale2.core.ui.resources.Res
import de.felixlf.gradingscale2.core.ui.resources.gradescale_list_menu_add_new_grade
import de.felixlf.gradingscale2.core.ui.resources.gradescale_list_menu_add_new_grade_scale
import de.felixlf.gradingscale2.core.ui.resources.gradescale_list_menu_delete_grade_scale
import de.felixlf.gradingscale2.core.ui.resources.gradescale_list_menu_edit_grade_scale
import de.felixlf.gradingscale2.core.ui.resources.gradescale_list_menu_export_exam_table
import org.jetbrains.compose.resources.StringResource

internal sealed class GradeScaleListDialogCommand(val menuText: StringResource? = null) {
    data class ExportExamTable(val gradeScaleId: String) :
        GradeScaleListDialogCommand(Res.string.gradescale_list_menu_export_exam_table)

    data class EditGradeScale(val gradeScaleId: String) : GradeScaleListDialogCommand(Res.string.gradescale_list_menu_edit_grade_scale)

    data object AddNewGradeScale : GradeScaleListDialogCommand(Res.string.gradescale_list_menu_add_new_grade_scale)

    data class EditCurrentGrade(val gradeId: String) : GradeScaleListDialogCommand()

    data class AddNewGradeInCurrentGradeScale(val gradeScaleId: String) :
        GradeScaleListDialogCommand(Res.string.gradescale_list_menu_add_new_grade)

    data class DeleteGradeScale(val gradeScaleId: String) :
        GradeScaleListDialogCommand(Res.string.gradescale_list_menu_delete_grade_scale)

    // data object Help : GradeScaleListDialogCommand(Res.string.gradescale_list_menu_help)
    // TODO: add a Help Menu and Feature
}
