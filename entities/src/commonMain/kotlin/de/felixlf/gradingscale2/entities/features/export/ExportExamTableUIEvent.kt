package de.felixlf.gradingscale2.entities.features.export

import de.felixlf.gradingscale2.entities.models.GradeScale
import de.felixlf.gradingscale2.entities.models.TableHeaders

/**
 * UI Events/Commands for the Export Exam Table dialog.
 */
sealed interface ExportExamTableUIEvent {
    data class SetGradeScale(val gradeScale: GradeScale) : ExportExamTableUIEvent
    data class SetTotalPoints(val points: Double) : ExportExamTableUIEvent
    data class SetHeaders(val headers: TableHeaders) : ExportExamTableUIEvent
    data object ToggleIncludePercentage : ExportExamTableUIEvent
    data object ToggleShowAsRange : ExportExamTableUIEvent
    data object ToggleSortDescending : ExportExamTableUIEvent
    data object CopyWordTable : ExportExamTableUIEvent
    data object CopyTsvTable : ExportExamTableUIEvent
    data object DismissHandled : ExportExamTableUIEvent
}
